package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

/**
 * Lets you store and edit inventories.
 *
 * @author David Cernat
 */

public class InventoryCommand extends AbstractCommand {
	
    private enum Action { OPEN, COPY, MOVE, SWAP, ADD, REMOVE, KEEP, EXCLUDE, FILL, CLEAR }
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
    	
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                // add Action
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
        	
            else if (!scriptEntry.hasObject("originEntity") &&
        		!scriptEntry.hasObject("originLocation") &&
        		arg.matchesPrefix("origin, o, source, s, items, i, from, f")) {
        		
        		// Is entity
        		if (arg.matchesArgumentType(dEntity.class))
        			scriptEntry.addObject("originEntity", arg.asType(dEntity.class).setPrefix("entity"));
        		// Is location
        		else if (arg.matchesArgumentType(dLocation.class))
        			scriptEntry.addObject("originLocation", arg.asType(dLocation.class).setPrefix("location"));
            }
            
            else if (!scriptEntry.hasObject("destinationEntity") &&
            		 !scriptEntry.hasObject("destinationLocation") &&
            		 arg.matchesPrefix("destination, d, target, to, t")) {
        		
            	// Is entity
            	if (arg.matchesArgumentType(dEntity.class))
            		scriptEntry.addObject("destinationEntity", arg.asType(dEntity.class).setPrefix("entity"));
            	// Is location
            	else if (arg.matchesArgumentType(dLocation.class))
            		scriptEntry.addObject("destinationLocation", arg.asType(dLocation.class).setPrefix("location"));
            }
        }

        // Check to make sure required arguments have been filled
        
        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify an Inventory action!");
        
        if (!scriptEntry.hasObject("destinationEntity") &&
        	!scriptEntry.hasObject("destinationLocation"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "DESTINATION");
    }
    
	@SuppressWarnings("unchecked")
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

		// Get objects
        Action action = (Action) scriptEntry.getObject("action");
		
		dEntity originEntity = (dEntity) scriptEntry.getObject("originEntity");
		dLocation originLocation = (dLocation) scriptEntry.getObject("originLocation");
		
		dEntity destinationEntity = (dEntity) scriptEntry.getObject("destinationEntity");
		dLocation destinationLocation = (dLocation) scriptEntry.getObject("destinationLocation");
		
		dInventory origin = null;
		dInventory destination = null;
		
		if (originLocation != null) {
			
			BlockState blockState = originLocation.getBlock().getState();
			
			if (blockState instanceof InventoryHolder) {
				
				origin = new dInventory(((InventoryHolder) blockState).getInventory());
			}
		}
		else if (originEntity != null) {
			
			LivingEntity entity = originEntity.getLivingEntity();
			
			if (entity instanceof Player) {
				
				origin = new dInventory(((Player) entity).getInventory());
			}
		}
		
		if (destinationLocation != null) {
			
			BlockState blockState = destinationLocation.getBlock().getState();
			
			if (blockState instanceof InventoryHolder) {
				
				destination = new dInventory(((InventoryHolder) blockState).getInventory());
			}
		}
		else if (destinationEntity != null) {
			
			LivingEntity entity = destinationEntity.getLivingEntity();
			
			if (entity instanceof Player) {
				
				destination = new dInventory(((Player) entity).getInventory());
			}
		}
		
		switch (action) {

			// Make the attached player open the destination inventory
			case OPEN:
				scriptEntry.getPlayer().getPlayerEntity().openInventory(destination.getInventory());
				return;
		
			// Turn destination's contents into a copy of origin's
        	case COPY:
        		origin.replace(destination);
        		return;
        		
        	// Copy origin's contents to destination, then empty origin
        	case MOVE:
        		origin.replace(destination);
        		origin.clear();
        		return;
        	
        	// Swap the contents of the two inventories
        	case SWAP:
        		dInventory temp = new dInventory(destination.getInventoryType())
        						  	.add(destination.getContents());
        		origin.replace(destination);
        		temp.replace(origin);
        		return;
        	
        	// Add origin's contents to destination
        	case ADD:
        		destination.add(origin.getContents());
        		return;
        		
        	// Remove origin's contents from destination
        	case REMOVE:
        		destination.remove(origin.getContents());
        		return;
        	
            // Keep only items from the origin's contents in the
        	// destination
            case KEEP:
               	destination.keep(origin.getContents());
               	return;
               	
            // Exclude all items from the origin's contents in the
            // destination
            case EXCLUDE:
               	destination.exclude(origin.getContents());
               	return;
        	
            // Add origin's contents over and over to destination
        	// until destination is full
            case FILL:
            	destination.fill(origin.getContents());
               	return;
        		
            // Clear the content of the destination inventory
            case CLEAR:
            	destination.clear();
            	return;
            
            default:
            	return;
            
		}
		
		
		   
    }
}
package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

/**
 * Modifies blocks based based of single block location.
 * Possibility to do faux animations with blocks.
 * 
 * @author Mason Adkins
 */

public class CopyBlockCommand extends AbstractCommand{

    @Override
	public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {

        Location copy_location = null;
        Location destination = null;
        boolean remove_original = false;

		for (String arg : scriptEntry.getArguments()) {			

            if (aH.matchesLocation(arg))
		    	copy_location = aH.getLocationFrom(arg);

            if (aH.matchesValueArg("to", arg, ArgumentType.Location))
                destination = aH.getLocationFrom(arg);

            if (aH.matchesArg("and_remove", arg))
                remove_original = true;

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        if (copy_location == null || destination == null)
            throw  new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION);

        scriptEntry.addObject("copy_location", copy_location)
                .addObject("destination", destination)
                .addObject("remove_original", remove_original);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
		Location copy_location = (Location) scriptEntry.getObject("copy_location");
        Location destination = (Location) scriptEntry.getObject("destination");
        Boolean remove_original = (Boolean) scriptEntry.getObject("remove_original");

        Block source = copy_location.getBlock();
        Block update = destination.getBlock();

        update.setTypeIdAndData(source.getTypeId(), source.getData(), false);

        if (source instanceof InventoryHolder)
            ((InventoryHolder) update.getState()).getInventory()
                    .setContents(((InventoryHolder) source.getState()).getInventory().getContents());


        // TODO: Account for Noteblock, Skull, Jukebox, Sign

	}
}

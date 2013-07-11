package net.aufdemrand.denizen.scripts.commands.entity;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;

/**
 * Safely removes an NPC.
 * 
 * To be expanded to use multiple NPCs and possibly other entities as well.
 *
 */
public class RemoveCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        EntityType entityType = null;
        String region = null;
    	
    	for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesEntityType(arg)) {
                entityType = aH.getEntityTypeFrom(arg);
                dB.echoDebug("...will remove all '%s'.", arg);
            }
            else if (aH.matchesValueArg("region", arg, ArgumentType.String)) {
            	region = aH.getStringFrom(arg);
                dB.echoDebug("...in region " + region);
            }
    	}
    	
    	scriptEntry.addObject("entityType", entityType);
    	scriptEntry.addObject("region", region);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        
    	EntityType entityType = (EntityType) scriptEntry.getObject("entityType");
    	String region = (String) scriptEntry.getObject("region");
    	
    	// If no entity type or region chosen, remove this NPC
    	
    	if (entityType == null && region == null) {
    	
    		scriptEntry.getNPC().getCitizen().destroy();

            dB.echoDebug("...have removed NPC '%s'.", String.valueOf(scriptEntry.getNPC().getCitizen().getId()));
    	}
    	
    	// Else, remove regular entities
    	
    	else {
    		
    		for (World world: Bukkit.getWorlds()) {
    			for (Entity entity : world.getEntities()) {
    				if (region != null) {
    					
    					if (WorldGuardUtilities.inRegion(entity.getLocation(), region)) {
    						
    						if (entityType == null) {
    							entity.remove();
    						}
    						else if (entity.getType().equals(entityType)) {
    							entity.remove();
    						}
    					}
    				}

    				else if (entity.getType().equals(entityType)) {
    						
    					entity.remove();
                	}
    			}
    		}
    	}
    }

}
package net.aufdemrand.denizen.scripts.commands.entity;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

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
    	
    	for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesEntityType(arg)) {
                entityType = aH.getEntityTypeFrom(arg);
                dB.echoDebug("...will remove all '%s'.", arg);
            }
    	}
    	
    	scriptEntry.addObject("entityType", entityType);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        
    	EntityType entityType = (EntityType) scriptEntry.getObject("entityType");
    	
    	// If no entity type chosen, remove this NPC
    	
    	if (entityType == null) {
    	
    		scriptEntry.getNPC().getCitizen().destroy();

            dB.echoDebug("...have removed NPC '%s'.", String.valueOf(scriptEntry.getNPC().getCitizen().getId()));
    	}
    	
    	// Else, remove all entities of this type from all worlds
    	
    	else {
    		
    		for (World world: Bukkit.getWorlds()) {
    		
    			for (Entity entity : world.getEntities()) {
    				if (entity.getType().equals(entityType)) {
    						
    					entity.remove();
                	}
    			}
    		}
    	}
    }

}
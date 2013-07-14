package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Teleports a list of entities to a location.
 *
 * @author David Cernat
 */

public class TeleportCommand extends AbstractCommand {
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields

    	for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
        	
    		if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            
        	else if (!scriptEntry.hasObject("entities")
                	&& arg.matchesPrefix("entity, entities, e, target, targets, t")) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
    		
        	else if (arg.matches("npc")) {
                // Entity arg
                scriptEntry.addObject("npc", true);
            }
        }
    	
    	// Check to make sure required arguments have been filled
        
        if ((!scriptEntry.hasObject("entities")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ENTITIES");
    }
    
	@SuppressWarnings("unchecked")
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
		dLocation location = (dLocation) scriptEntry.getObject("location");
		List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
		
		// If the "npc" argument was used, add the NPC to the list of entities,
		// for compatibility with 0.8 scripts
		if ((Boolean) scriptEntry.getObject("npc") == true) {
			entities.add(scriptEntry.getNPC().getDenizenEntity());
		}
		
        // Report to dB
        dB.report(getName(), aH.debugObj("location", location) +
        					 aH.debugObj("entities", entities.toString()));

		for (dEntity entity : entities) {
	        	
			entity.teleport(location);
	    }
	}
}
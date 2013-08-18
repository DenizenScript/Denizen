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
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.entity.Position;

/**
 * Mounts a player on the NPC if no targets are specified.
 * If targets are specified, mount them on each other in order.
 *
 * @author David Cernat
 */

public class MountCommand extends AbstractCommand {
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            
            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {
                
                scriptEntry.addObject("cancel", "");
            }
            
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
        }
        
        // Use the NPC or player's locations as the location if one is not specified
        
        scriptEntry.defaultObject("location",
                scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getLocation() : null,
                scriptEntry.hasNPC() ? scriptEntry.getNPC().getLocation() : null);
        
        // Check to make sure required arguments have been filled
        
        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ENTITIES");
        
        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        
        dLocation location = (dLocation) scriptEntry.getObject("location");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");            
        boolean cancel = scriptEntry.hasObject("cancel");
        
        // Report to dB
        dB.report(getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                             aH.debugObj("location", location) +
                             aH.debugObj("entities", entities.toString()));

        // Mount or dismount all of the entities
        if (!cancel) {
            
            // Go through all the entities, spawning/teleporting them
            for (dEntity entity : entities) {
                
                if (!entity.isSpawned()) {
                    entity.spawnAt(location);
                }
                else {
                    entity.teleport(location);
                }
            }
            
            Position.mount(Conversion.convert(entities));
        }
        else {
            Position.dismount(Conversion.convert(entities));
        }
    }
}
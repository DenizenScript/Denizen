package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TargetType;

/**
 *
 * Makes NPCs or entities attack a certain entity.
 *
 * @author David Cernat
 *
 */

public class AttackCommand extends AbstractCommand {
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cancel")
                && arg.matches("cancel, stop")) {
                
                scriptEntry.addObject("cancel", "");
            }
            
            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {
                // Entity dList arg
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }
            
            else if (!scriptEntry.hasObject("target")
                     && arg.matchesArgumentType(dEntity.class)
                     && arg.matchesPrefix("target")) {
                // Single entity arg
                scriptEntry.addObject("target", arg.asType(dEntity.class));
            }
        }
        
        // Use the player as the target if one is not specified
        
        scriptEntry.defaultObject("target",
                scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getDenizenEntity() : null);
        
        // Use the NPC as the attacking entity if one is not specified
        
        scriptEntry.defaultObject("entities",
                scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null);

        // Check to make sure required arguments have been filled
        
        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ENTITIES");
        
        if (!scriptEntry.hasObject("target") && !scriptEntry.hasObject("cancel"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "TARGET");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dEntity target = (dEntity) scriptEntry.getObject("target");
        Boolean cancel = scriptEntry.hasObject("cancel");
        
        // Report to dB
        dB.report(getName(), (cancel == true ? aH.debugObj("cancel", cancel) : "") + 
                             aH.debugObj("entities", entities.toString()) +
                             (target != null ? aH.debugObj("target", target) : ""));
        
        // Go through all the entities and make them either attack
        // the target or stop attacking
        
        for (dEntity entity : entities) {
            if (entity.isNPC()) {
                Navigator nav = entity.getNPC().getNavigator();
                
                if (cancel.equals(false)) {
                    nav.setTarget(target.getBukkitEntity(), true);
                }
                else {
                    // Only cancel navigation if the NPC is attacking something
                    if (nav.isNavigating()
                        && nav.getTargetType().equals(TargetType.ENTITY)
                        && nav.getEntityTarget().isAggressive()) {
                        
                        nav.cancelNavigation();
                    }
                }
            }
            else {
                if (cancel.equals(false)) {
                    entity.target(target.getLivingEntity());
                }
                else {
                    entity.target(null);
                }
            }
        }   
    }
}
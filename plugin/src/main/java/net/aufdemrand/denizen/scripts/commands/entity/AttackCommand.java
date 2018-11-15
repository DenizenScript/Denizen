package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TargetType;

import java.util.Arrays;
import java.util.List;

public class AttackCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel", "stop")) {

                scriptEntry.addObject("cancel", "true");
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(dEntity.class)
                    && arg.matchesPrefix("target", "t")) {
                // Single entity arg
                scriptEntry.addObject("target", arg.asType(dEntity.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)
                    && !arg.matchesPrefix("target", "t")) {
                // Entity dList arg
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the player as the target if one is not specified
        if (!scriptEntry.hasObject("target")) {
            scriptEntry.addObject("target", ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity() : null);
        }

        // Use the NPC as the attacking entity if one is not specified
        scriptEntry.defaultObject("entities",
                ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()) : null);

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        if (!scriptEntry.hasObject("target") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify a target!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dEntity target = (dEntity) scriptEntry.getObject("target");
        boolean cancel = scriptEntry.hasObject("cancel");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", "true") : "") +
                    aH.debugObj("entities", entities.toString()) +
                    (target != null ? aH.debugObj("target", target) : ""));
        }

        // Go through all the entities and make them either attack
        // the target or stop attacking

        for (dEntity entity : entities) {
            if (entity.isCitizensNPC()) {
                Navigator nav = entity.getDenizenNPC().getCitizen().getNavigator();

                if (!cancel) {
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
                if (!cancel) {
                    entity.target(target.getLivingEntity());
                }
                else {
                    entity.target(null);
                }
            }
        }
    }
}

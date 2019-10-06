package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TargetType;

import java.util.Arrays;
import java.util.List;

public class AttackCommand extends AbstractCommand {

    // <--[command]
    // @Name Attack
    // @Syntax attack (<entity>|...) (target:<entity>/cancel)
    // @Required 0
    // @Short Makes an entity, or list of entities, attack a target.
    // @Group entity
    //
    // @Description
    // By itself, the 'attack' command will act as an NPC command in the sense that an attached
    // NPC will attack the attached player, or specified target. It can also accept a specified entity,
    // or list of entities, to fulfill the command, just specify a 'fetchable' entity object. This includes
    // player objects (dPlayers) and NPC objects (dNPCs). To specify the target, prefix the entity
    // object with 'target:' or 't:'.
    //
    // To cancel an attack, use the 'cancel' argument instead of specifying a target.
    //
    // @Tags
    // <NPCTag.is_fighting>
    // <NPCTag.attack_strategy>
    // <NPCTag.target_entity>
    //
    // @Usage
    // Use to make an NPC attack a player in an interact script.
    // - attack
    //
    // @Usage
    // Use to make an NPC attack a nearby entity.
    // - attack target:<npc.location.find.living_entities.within[10].random>
    //
    // @Usage
    // Use to make a specific entity attack an entity, including players or npcs.
    // - attack <player.location.find.living_entities.within[10].random> target:<player>
    //
    // @Usage
    // Use to stop an attack
    // - attack n@Herobrine stop
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel", "stop")) {

                scriptEntry.addObject("cancel", "true");
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(EntityTag.class)
                    && arg.matchesPrefix("target", "t")) {
                // Single entity arg
                scriptEntry.addObject("target", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)
                    && !arg.matchesPrefix("target", "t")) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the player as the target if one is not specified
        if (!scriptEntry.hasObject("target")) {
            scriptEntry.addObject("target", Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getDenizenEntity() : null);
        }

        // Use the NPC as the attacking entity if one is not specified
        scriptEntry.defaultObject("entities",
                Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null);

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
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        EntityTag target = (EntityTag) scriptEntry.getObject("target");
        boolean cancel = scriptEntry.hasObject("cancel");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? ArgumentHelper.debugObj("cancel", "true") : "") +
                    ArgumentHelper.debugObj("entities", entities.toString()) +
                    (target != null ? ArgumentHelper.debugObj("target", target) : ""));
        }

        // Go through all the entities and make them either attack
        // the target or stop attacking

        for (EntityTag entity : entities) {
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

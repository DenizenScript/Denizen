package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.TargetType;

import java.util.Collections;
import java.util.List;

public class AttackCommand extends AbstractCommand {

    public AttackCommand() {
        setName("attack");
        setSyntax("attack [<entity>|...] (target:<entity>/cancel)");
        setRequiredArguments(0, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Attack
    // @Syntax attack [<entity>|...] (target:<entity>/cancel)
    // @Required 0
    // @Maximum 2
    // @Short Makes an entity, or list of entities, attack a target.
    // @Group entity
    //
    // @Description
    // The attack command causes a mob entity to attack a target mob entity or player.
    //
    // This technically can be used on an NPC, but it will trigger the Citizens internal punching-pathfinder.
    // This attack mode doesn't work well. If you want NPC combat, consider using Sentinel instead: <@link url https://github.com/mcmonkeyprojects/Sentinel/blob/master/README.md>.
    //
    // To cancel an attack, use the 'cancel' argument instead of specifying a target.
    //
    // @Tags
    // <NPCTag.is_fighting>
    // <NPCTag.attack_strategy>
    // <NPCTag.target_entity>
    //
    // @Usage
    // Use to make the player's target entity attack a nearby entity.
    // - attack <player.target> target:<npc.location.find.living_entities.within[10].random>
    //
    // @Usage
    // Use to make a random nearby entity attack a player.
    // - attack <player.location.find.living_entities.within[10].random> target:<player>
    //
    // @Usage
    // Use to stop an entity from attacking.
    // - attack <[entity]> cancel
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel", "stop")) {
                scriptEntry.addObject("cancel", "true");
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(EntityTag.class)
                    && arg.matchesPrefix("target", "t")) {
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
        if (!scriptEntry.hasObject("target")) {
            scriptEntry.addObject("target", Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getDenizenEntity() : null);
        }
        if (!scriptEntry.hasObject("entities")) {
            scriptEntry.defaultObject("entities", Utilities.entryHasNPC(scriptEntry) ? Collections.singletonList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null);
            if (!scriptEntry.hasObject("entities")) {
                throw new InvalidArgumentsException("Must specify entity/entities!");
            }
        }
        if (!scriptEntry.hasObject("target") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify a target!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        EntityTag target = scriptEntry.getObjectTag("target");
        boolean cancel = scriptEntry.hasObject("cancel");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? db("cancel", "true") : ""), db("entities", entities), db("target", target));
        }
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

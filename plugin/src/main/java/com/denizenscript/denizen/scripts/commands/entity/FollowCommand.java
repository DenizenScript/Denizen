package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class FollowCommand extends AbstractCommand {

    public FollowCommand() {
        setName("follow");
        setSyntax("follow (followers:<entity>|...) (stop/target:<entity>) (lead:<#.#>) (max:<#.#>) (speed:<#.#>) (allow_wander) (no_teleport)");
        setRequiredArguments(0, 7);
        isProcedural = false;
    }

    // <--[command]
    // @Name Follow
    // @Syntax follow (followers:<entity>|...) (stop/target:<entity>) (lead:<#.#>) (max:<#.#>) (speed:<#.#>) (allow_wander) (no_teleport)
    // @Required 0
    // @Maximum 7
    // @Short Causes a list of entities to follow a target.
    // @Group entity
    //
    // @Description
    // Causes a list of entities to follow a target.
    //
    // Specify the list of followers or just one. If no follower is specified, will use the linked NPC.
    //
    // Specify either the target to follow, or 'stop'. If no target is specified, will use the linked player.
    //
    // Use 'speed' to set the movement speed multiplier.
    // Use 'lead' to set how far away the follower will remain from the target (ie, it won't try to get closer than the 'lead' distance).
    // Use 'max' to set the maximum distance between the follower and the target before the follower will automatically start teleporting to keep up.
    // Use 'no_teleport' to disable teleporting when the entity is out of range (instead, the entity will simply give up).
    // Use 'allow_wander' to allow the entity to wander randomly.
    //
    // The 'max' and 'allow_wander' arguments can only be used on non-NPC entities.
    //
    // @Tags
    // <NPCTag.navigator.target_entity> returns the entity the npc is following.
    //
    // @Usage
    // To make an NPC follow the player in an interact script.
    // - follow
    //
    // @Usage
    // To make an NPC stop following.
    // - follow stop
    //
    // @Usage
    // To explicitly make an NPC follow the player.
    // - follow followers:<npc> target:<player>
    //
    // @Usage
    // To make an NPC follow the player, slowly and at distance.
    // - follow speed:0.7 lead:10
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("stop") &&
                    arg.matches("stop")) {
                scriptEntry.addObject("stop", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("lead") &&
                    arg.matchesFloat() &&
                    arg.matchesPrefix("l", "lead")) {
                scriptEntry.addObject("lead", arg.asElement());
            }
            else if (!scriptEntry.hasObject("max") &&
                    arg.matchesFloat() &&
                    arg.matchesPrefix("max")) {
                scriptEntry.addObject("max", arg.asElement());
            }
            else if (!scriptEntry.hasObject("allow_wander") &&
                    arg.matches("allow_wander")) {
                scriptEntry.addObject("allow_wander", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("no_teleport") &&
                    arg.matches("no_teleport")) {
                scriptEntry.addObject("no_teleport", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("speed") &&
                    arg.matchesFloat() &&
                    arg.matchesPrefix("s", "speed")) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities") &&
                    arg.matchesPrefix("followers", "follower") &&
                    arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("target") &&
                    arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("target", arg.asType(EntityTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("target")) {
            if (Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("target", Utilities.getEntryPlayer(scriptEntry).getDenizenEntity());
            }
            else if (!scriptEntry.hasObject("stop")) {
                throw new InvalidArgumentsException("This command requires a linked player!");
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            if (!Utilities.entryHasNPC(scriptEntry)) {
                throw new InvalidArgumentsException("This command requires a linked NPC!");
            }
            else {
                scriptEntry.addObject("entities",
                        new ListTag(Utilities.getEntryNPC(scriptEntry)));
            }
        }
        scriptEntry.defaultObject("stop", new ElementTag(false)).defaultObject("allow_wander", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag stop = scriptEntry.getElement("stop");
        ElementTag lead = scriptEntry.getElement("lead");
        ElementTag maxRange = scriptEntry.getElement("max");
        ElementTag allowWander = scriptEntry.getElement("allow_wander");
        ElementTag speed = scriptEntry.getElement("speed");
        ElementTag noTeleport = scriptEntry.getElement("no_teleport");
        ListTag entities = scriptEntry.getObjectTag("entities");
        EntityTag target = scriptEntry.getObjectTag("target");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), Utilities.getEntryPlayer(scriptEntry), (!stop.asBoolean() ? db("Action", "FOLLOW") : db("Action", "STOP")),
                            lead, noTeleport, maxRange, allowWander, entities, target);
        }
        for (EntityTag entity : entities.filter(EntityTag.class, scriptEntry)) {
            if (entity.isCitizensNPC()) {
                NPCTag npc = entity.getDenizenNPC();
                if (lead != null) {
                    npc.getNavigator().getLocalParameters().distanceMargin(lead.asDouble());
                }
                if (speed != null) {
                    npc.getNavigator().getLocalParameters().speedModifier(speed.asFloat());
                }
                if (noTeleport != null && noTeleport.asBoolean()) {
                    npc.getNavigator().getLocalParameters().stuckAction(null);
                }
                if (stop.asBoolean()) {
                    npc.getNavigator().cancelNavigation();
                }
                else {
                    npc.getNavigator().setTarget(target.getBukkitEntity(), false);
                }
            }
            else {
                if (stop.asBoolean()) {
                    NMSHandler.entityHelper.stopFollowing(entity.getBukkitEntity());
                }
                else {
                    NMSHandler.entityHelper.follow(target.getBukkitEntity(), entity.getBukkitEntity(),
                            speed != null ? speed.asDouble() : 0.3, lead != null ? lead.asDouble() : 5,
                            maxRange != null ? maxRange.asDouble() : 8, allowWander.asBoolean(), noTeleport == null || !noTeleport.asBoolean());
                }
            }
        }
    }
}

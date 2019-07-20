package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class FollowCommand extends AbstractCommand {

    // <--[command]
    // @Name Follow
    // @Syntax follow (followers:<entity>|...) (stop) (lead:<#.#>) (max:<#.#>) (speed:<#.#>) (target:<entity>) (allow_wander)
    // @Required 0
    // @Short Causes a list of entities to follow a target.
    // @Group entity
    //
    // @Description
    // TODO: Document Command Details
    // The 'max' and 'allow_wander' arguments can only be used on non-NPC entities.
    //
    // @Tags
    // <NPCTag.navigator.target_entity> returns the entity the npc is following.
    //
    // @Usage
    // To make an NPC follow the player in an interact script
    // - follow followers:<npc> target:<player>
    // TODO: Document Command Details
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("stop") &&
                    arg.matches("STOP")) {
                scriptEntry.addObject("stop", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("lead") &&
                    arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double) &&
                    arg.matchesPrefix("l", "lead")) {
                scriptEntry.addObject("lead", arg.asElement());
            }
            else if (!scriptEntry.hasObject("max") &&
                    arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double) &&
                    arg.matchesPrefix("max")) {
                scriptEntry.addObject("max", arg.asElement());
            }
            else if (!scriptEntry.hasObject("allow_wander") &&
                    arg.matches("allow_wander")) {
                scriptEntry.addObject("allow_wander", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("speed") &&
                    arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Percentage) &&
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
            else {
                throw new InvalidArgumentsException("This command requires a linked player!");
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            if (!Utilities.entryHasNPC(scriptEntry)) {
                throw new InvalidArgumentsException("This command requires a linked NPC!");
            }
            else {
                scriptEntry.addObject("entities",
                        new ListTag(Utilities.getEntryNPC(scriptEntry).identify()));
            }
        }

        scriptEntry.defaultObject("stop", new ElementTag(false)).defaultObject("allow_wander", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Get objects
        ElementTag stop = scriptEntry.getElement("stop");
        ElementTag lead = scriptEntry.getElement("lead");
        ElementTag maxRange = scriptEntry.getElement("max");
        ElementTag allowWander = scriptEntry.getElement("allow_wander");
        ElementTag speed = scriptEntry.getElement("speed");
        ListTag entities = scriptEntry.getObjectTag("entities");
        EntityTag target = scriptEntry.getObjectTag("target");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    (Utilities.getEntryPlayer(scriptEntry) != null ? Utilities.getEntryPlayer(scriptEntry).debug() : "")
                            + (!stop.asBoolean() ? ArgumentHelper.debugObj("Action", "FOLLOW") : ArgumentHelper.debugObj("Action", "STOP"))
                            + (lead != null ? lead.debug() : "")
                            + (maxRange != null ? maxRange.debug() : "")
                            + allowWander.debug()
                            + entities.debug()
                            + target.debug());
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

                if (stop.asBoolean()) {
                    npc.getNavigator().cancelNavigation();
                }
                else {
                    npc.getNavigator().setTarget(target.getBukkitEntity(), false);
                }
            }
            else {
                if (stop.asBoolean()) {
                    NMSHandler.getInstance().getEntityHelper().stopFollowing(entity.getBukkitEntity());
                }
                else {
                    NMSHandler.getInstance().getEntityHelper().follow(target.getBukkitEntity(), entity.getBukkitEntity(),
                            speed != null ? speed.asDouble() : 0.3, lead != null ? lead.asDouble() : 5,
                            maxRange != null ? maxRange.asDouble() : 8, allowWander.asBoolean());
                }
            }
        }

    }
}

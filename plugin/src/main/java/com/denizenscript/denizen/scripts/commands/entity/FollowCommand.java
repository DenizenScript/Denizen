package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
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
    // <n@npc.navigator.target_entity> returns the entity the npc is following.
    //
    // @Usage
    // To make an NPC follow the player in an interact script
    // - follow followers:<npc> target:<player>
    // TODO: Document Command Details
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            if (!scriptEntry.hasObject("stop") &&
                    arg.matches("STOP")) {
                scriptEntry.addObject("stop", new Element(true));
            }
            else if (!scriptEntry.hasObject("lead") &&
                    arg.matchesPrimitive(aH.PrimitiveType.Double) &&
                    arg.matchesPrefix("l", "lead")) {
                scriptEntry.addObject("lead", arg.asElement());
            }
            else if (!scriptEntry.hasObject("max") &&
                    arg.matchesPrimitive(aH.PrimitiveType.Double) &&
                    arg.matchesPrefix("max")) {
                scriptEntry.addObject("max", arg.asElement());
            }
            else if (!scriptEntry.hasObject("allow_wander") &&
                    arg.matches("allow_wander")) {
                scriptEntry.addObject("allow_wander", new Element(true));
            }
            else if (!scriptEntry.hasObject("speed") &&
                    arg.matchesPrimitive(aH.PrimitiveType.Percentage) &&
                    arg.matchesPrefix("s", "speed")) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities") &&
                    arg.matchesPrefix("followers", "follower") &&
                    arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("target") &&
                    arg.matchesArgumentType(dEntity.class)) {
                scriptEntry.addObject("target", arg.asType(dEntity.class));
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
                        new dList(Utilities.getEntryNPC(scriptEntry).identify()));
            }
        }

        scriptEntry.defaultObject("stop", new Element(false)).defaultObject("allow_wander", new Element(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Get objects
        Element stop = scriptEntry.getElement("stop");
        Element lead = scriptEntry.getElement("lead");
        Element maxRange = scriptEntry.getElement("max");
        Element allowWander = scriptEntry.getElement("allow_wander");
        Element speed = scriptEntry.getElement("speed");
        dList entities = scriptEntry.getdObject("entities");
        dEntity target = scriptEntry.getdObject("target");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    (Utilities.getEntryPlayer(scriptEntry) != null ? Utilities.getEntryPlayer(scriptEntry).debug() : "")
                            + (!stop.asBoolean() ? aH.debugObj("Action", "FOLLOW") : aH.debugObj("Action", "STOP"))
                            + (lead != null ? lead.debug() : "")
                            + (maxRange != null ? maxRange.debug() : "")
                            + allowWander.debug()
                            + entities.debug()
                            + target.debug());
        }

        for (dEntity entity : entities.filter(dEntity.class, scriptEntry)) {
            if (entity.isCitizensNPC()) {
                dNPC npc = entity.getDenizenNPC();
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

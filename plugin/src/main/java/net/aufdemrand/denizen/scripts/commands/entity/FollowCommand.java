package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class FollowCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
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
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                scriptEntry.addObject("target", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity());
            }
            else {
                throw new InvalidArgumentsException("This command requires a linked player!");
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                throw new InvalidArgumentsException("This command requires a linked NPC!");
            }
            else {
                scriptEntry.addObject("entities",
                        new dList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().identify()));
            }
        }

        scriptEntry.defaultObject("stop", new Element(false)).defaultObject("allow_wander", new Element(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
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
                    (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() != null ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().debug() : "")
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

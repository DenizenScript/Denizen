package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.WorldBorder;

import java.util.List;

public class WorldBorderCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("center")
                    && arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("center")) {
                scriptEntry.addObject("center", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("damage")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("damage")) {
                scriptEntry.addObject("damage", arg.asElement());
            }
            else if (!scriptEntry.hasObject("damagebuffer")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("damagebuffer")) {
                scriptEntry.addObject("damagebuffer", arg.asElement());
            }
            else if (!scriptEntry.hasObject("size")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("size")) {
                scriptEntry.addObject("size", arg.asElement());
            }
            else if (!scriptEntry.hasObject("current_size")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("current_size")) {
                scriptEntry.addObject("current_size", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("duration")) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("warningdistance")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && arg.matchesPrefix("warningdistance")) {
                scriptEntry.addObject("warningdistance", arg.asElement());
            }
            else if (!scriptEntry.hasObject("warningtime")
                    && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("warningtime")) {
                scriptEntry.addObject("warningtime", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new Element("true"));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("world") && !scriptEntry.hasObject("players") ) {
            throw new InvalidArgumentsException("Must specify a world or players!");
        }

        if (!scriptEntry.hasObject("center") && !scriptEntry.hasObject("size")
                && !scriptEntry.hasObject("damage") && !scriptEntry.hasObject("damagebuffer")
                && !scriptEntry.hasObject("warningdistance") && !scriptEntry.hasObject("warningtime")
                && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Must specify at least one option!");
        }

        // fill in default arguments if necessary

        scriptEntry.defaultObject("duration", new Duration(0));
        scriptEntry.defaultObject("reset", new Element("false"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dWorld world = (dWorld) scriptEntry.getObject("world");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");
        dLocation center = (dLocation) scriptEntry.getObject("center");
        Element size = scriptEntry.getElement("size");
        Element currSize = scriptEntry.getElement("current_size");
        Element damage = scriptEntry.getElement("damage");
        Element damagebuffer = scriptEntry.getElement("damagebuffer");
        Duration duration = scriptEntry.getdObject("duration");
        Element warningdistance = scriptEntry.getElement("warningdistance");
        Duration warningtime = scriptEntry.getdObject("warningtime");
        Element reset = scriptEntry.getdObject("reset");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), (world != null ? world.debug() : "")
                    + (players != null ? aH.debugList("Player(s)", players) : "")
                    + (center != null ? center.debug() : "")
                    + (size != null ? size.debug() : "")
                    + (currSize != null ? currSize.debug() : "")
                    + (damage != null ? damage.debug() : "")
                    + (damagebuffer != null ? damagebuffer.debug() : "")
                    + (warningdistance != null ? warningdistance.debug() : "")
                    + (warningtime != null ? warningtime.debug() : "")
                    + duration.debug() + reset.debug());

        }

        // Handle client-side world borders
        if (players != null) {
            if (reset.asBoolean()) {
                for (dPlayer player : players) {
                    NMSHandler.getInstance().getPacketHelper().resetWorldBorder(player.getPlayerEntity());
                }
                return;
            }

            WorldBorder wb;
            for (dPlayer player : players) {
                wb = player.getWorld().getWorldBorder();
                NMSHandler.getInstance().getPacketHelper().setWorldBorder(
                        player.getPlayerEntity(),
                        (center != null ? center : wb.getCenter()),
                        (size != null ? size.asDouble() : wb.getSize()),
                        (currSize != null ? currSize.asDouble() : wb.getSize()),
                        duration.getMillis(),
                        (warningdistance != null ? warningdistance.asInt() : wb.getWarningDistance()),
                        (warningtime != null ? warningtime.getSecondsAsInt() : wb.getWarningTime()));
            }
            return;
        }

        WorldBorder worldborder = world.getWorld().getWorldBorder();

        if (reset.asBoolean()) {
            worldborder.reset();
            return;
        }

        if (center != null) {
            worldborder.setCenter(center);
        }

        if (size != null) {
            if (currSize != null) {
                worldborder.setSize(currSize.asDouble());
            }
            worldborder.setSize(size.asDouble(), duration.getSecondsAsInt());
        }

        if (damage != null) {
            worldborder.setDamageAmount(damage.asDouble());
        }

        if (damagebuffer != null) {
            worldborder.setDamageBuffer(damagebuffer.asDouble());
        }

        if (warningdistance != null) {
            worldborder.setWarningDistance(warningdistance.asInt());
        }

        if (warningtime != null) {
            worldborder.setWarningTime(warningtime.getSecondsAsInt());
        }
    }
}

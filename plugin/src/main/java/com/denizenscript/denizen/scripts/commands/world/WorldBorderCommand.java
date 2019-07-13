package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.WorldBorder;

import java.util.List;

public class WorldBorderCommand extends AbstractCommand {

    // <--[command]
    // @Name WorldBorder
    // @Syntax worldborder [<world>/<player>|...] (center:<location>) (size:<#.#>) (current_size:<#.#>) (damage:<#.#>) (damagebuffer:<#.#>) (warningdistance:<#>) (warningtime:<duration>) (duration:<duration>) (reset)
    // @Required 2
    // @Short Modifies a world border.
    // @Group world
    //
    // @Description
    // Modifies the world border of a specified world or a list of players.
    // NOTE: Modifying player world borders is client-side and will reset on death, relog, or other actions.
    // Options are:
    // center: Sets the center of the world border.
    // size: Sets the new size of the world border.
    // current_size: Sets the initial size of the world border when resizing it over a duration.
    // damage: Sets the amount of damage a player takes when outside the world border buffer radius.
    // damagebuffer: Sets the radius a player may safely be outside the world border before taking damage.
    // warningdistance: Causes the screen to be tinted red when the player is within the specified radius from the world border.
    // warningtime: Causes the screen to be tinted red when a contracting world border will reach the player within the specified time.
    // duration: Causes the world border to grow or shrink from its current size to its new size over the specified duration.
    // reset: Resets the world border to its vanilla defaults for a world, or to the current world border for players.
    //
    // @Tags
    // <l@location.is_within_border>
    // <w@world.border_size>
    // <w@world.border_center>
    // <w@world.border_damage>
    // <w@world.border_damage_buffer>
    // <w@world.border_warning_distance>
    // <w@world.border_warning_time>
    //
    // @Usage
    // Use to set the size of a world border.
    // - worldborder <player.location.world> size:4
    //
    // @Usage
    // Use to update a world border's center, and then the size over the course of 10 seconds.
    // - worldborder <def[world]> center:<def[world].spawn_location> size:100 duration:10s
    //
    // @Usage
    // Use to show a client-side world border to the attached player.
    // - worldborder <player> center:<player.location> size:10
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("center")
                    && arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("center")) {
                scriptEntry.addObject("center", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("damage")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("damage")) {
                scriptEntry.addObject("damage", arg.asElement());
            }
            else if (!scriptEntry.hasObject("damagebuffer")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("damagebuffer")) {
                scriptEntry.addObject("damagebuffer", arg.asElement());
            }
            else if (!scriptEntry.hasObject("size")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("size")) {
                scriptEntry.addObject("size", arg.asElement());
            }
            else if (!scriptEntry.hasObject("current_size")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("current_size")) {
                scriptEntry.addObject("current_size", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("duration")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("warningdistance")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)
                    && arg.matchesPrefix("warningdistance")) {
                scriptEntry.addObject("warningdistance", arg.asElement());
            }
            else if (!scriptEntry.hasObject("warningtime")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("warningtime")) {
                scriptEntry.addObject("warningtime", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new ElementTag("true"));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("world") && !scriptEntry.hasObject("players")) {
            throw new InvalidArgumentsException("Must specify a world or players!");
        }

        if (!scriptEntry.hasObject("center") && !scriptEntry.hasObject("size")
                && !scriptEntry.hasObject("damage") && !scriptEntry.hasObject("damagebuffer")
                && !scriptEntry.hasObject("warningdistance") && !scriptEntry.hasObject("warningtime")
                && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Must specify at least one option!");
        }

        // fill in default arguments if necessary

        scriptEntry.defaultObject("duration", new DurationTag(0));
        scriptEntry.defaultObject("reset", new ElementTag("false"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        dWorld world = (dWorld) scriptEntry.getObject("world");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");
        dLocation center = (dLocation) scriptEntry.getObject("center");
        ElementTag size = scriptEntry.getElement("size");
        ElementTag currSize = scriptEntry.getElement("current_size");
        ElementTag damage = scriptEntry.getElement("damage");
        ElementTag damagebuffer = scriptEntry.getElement("damagebuffer");
        DurationTag duration = scriptEntry.getdObject("duration");
        ElementTag warningdistance = scriptEntry.getElement("warningdistance");
        DurationTag warningtime = scriptEntry.getdObject("warningtime");
        ElementTag reset = scriptEntry.getdObject("reset");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), (world != null ? world.debug() : "")
                    + (players != null ? ArgumentHelper.debugList("Player(s)", players) : "")
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

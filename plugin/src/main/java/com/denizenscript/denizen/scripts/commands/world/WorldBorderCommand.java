package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.WorldBorder;

import java.util.List;

public class WorldBorderCommand extends AbstractCommand {

    public WorldBorderCommand() {
        setName("worldborder");
        setSyntax("worldborder [<world>/<player>|...] (center:<location>) (size:<#.#>) (current_size:<#.#>) (damage:<#.#>) (damagebuffer:<#.#>) (warningdistance:<#>) (warningtime:<duration>) (duration:<duration>) (reset)");
        setRequiredArguments(2, 10);
        isProcedural = false;
    }

    // <--[command]
    // @Name WorldBorder
    // @Syntax worldborder [<world>/<player>|...] (center:<location>) (size:<#.#>) (current_size:<#.#>) (damage:<#.#>) (damagebuffer:<#.#>) (warningdistance:<#>) (warningtime:<duration>) (duration:<duration>) (reset)
    // @Required 2
    // @Maximum 10
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
    // <LocationTag.is_within_border>
    // <WorldTag.border_size>
    // <WorldTag.border_center>
    // <WorldTag.border_damage>
    // <WorldTag.border_damage_buffer>
    // <WorldTag.border_warning_distance>
    // <WorldTag.border_warning_time>
    //
    // @Usage
    // Use to set the size of a world border.
    // - worldborder <player.location.world> size:4
    //
    // @Usage
    // Use to update a world border's center, and then the size over the course of 10 seconds.
    // - worldborder <[world]> center:<[world].spawn_location> size:100 duration:10s
    //
    // @Usage
    // Use to show a client-side world border to the attached player.
    // - worldborder <player> center:<player.location> size:10
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("center")
                    && arg.matchesArgumentType(LocationTag.class)
                    && arg.matchesPrefix("center")) {
                scriptEntry.addObject("center", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("damage")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("damage")) {
                scriptEntry.addObject("damage", arg.asElement());
            }
            else if (!scriptEntry.hasObject("damagebuffer")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("damagebuffer")) {
                scriptEntry.addObject("damagebuffer", arg.asElement());
            }
            else if (!scriptEntry.hasObject("size")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("size")) {
                scriptEntry.addObject("size", arg.asElement());
            }
            else if (!scriptEntry.hasObject("current_size")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("current_size")) {
                scriptEntry.addObject("current_size", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("duration")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("warningdistance")
                    && arg.matchesInteger()
                    && arg.matchesPrefix("warningdistance")) {
                scriptEntry.addObject("warningdistance", arg.asElement());
            }
            else if (!scriptEntry.hasObject("warningtime")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("warningtime")) {
                scriptEntry.addObject("warningtime", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new ElementTag("true"));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("world") && !scriptEntry.hasObject("players")) {
            throw new InvalidArgumentsException("Must specify a world or players!");
        }
        if (!scriptEntry.hasObject("center") && !scriptEntry.hasObject("size")
                && !scriptEntry.hasObject("damage") && !scriptEntry.hasObject("damagebuffer")
                && !scriptEntry.hasObject("warningdistance") && !scriptEntry.hasObject("warningtime")
                && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Must specify at least one option!");
        }
        scriptEntry.defaultObject("duration", new DurationTag(0));
        scriptEntry.defaultObject("reset", new ElementTag("false"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        WorldTag world = scriptEntry.getObjectTag("world");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        LocationTag center = scriptEntry.getObjectTag("center");
        ElementTag size = scriptEntry.getElement("size");
        ElementTag currSize = scriptEntry.getElement("current_size");
        ElementTag damage = scriptEntry.getElement("damage");
        ElementTag damagebuffer = scriptEntry.getElement("damagebuffer");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        ElementTag warningdistance = scriptEntry.getElement("warningdistance");
        DurationTag warningtime = scriptEntry.getObjectTag("warningtime");
        ElementTag reset = scriptEntry.getObjectTag("reset");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), world, db("players", players), center, size, currSize, damage, damagebuffer, warningdistance, warningtime, duration, reset);
        }
        if (players != null) {
            if (reset.asBoolean()) {
                for (PlayerTag player : players) {
                    NMSHandler.packetHelper.resetWorldBorder(player.getPlayerEntity());
                }
                return;
            }
            WorldBorder wb;
            for (PlayerTag player : players) {
                wb = player.getWorld().getWorldBorder();
                NMSHandler.packetHelper.setWorldBorder(
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

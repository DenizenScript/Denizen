package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class TimeCommand extends AbstractCommand {

    public TimeCommand() {
        setName("time");
        setSyntax("time ({global}/player) [<time-duration>/reset] (<world>) (reset:<duration>) (freeze)");
        setRequiredArguments(1, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Time
    // @Syntax time ({global}/player) [<time-duration>/reset] (<world>) (reset:<duration>) (freeze)
    // @Required 1
    // @Maximum 5
    // @Short Changes the current time in the minecraft world.
    // @Group world
    //
    // @Description
    // Changes the current time in a world or the time that a player sees the world in.
    // If no world is specified, defaults to the NPCs world. If no NPC is available,
    // defaults to the player's world. If no player is available, an error will be thrown.
    //
    // If 'player' is specified, this will change their personal time.
    // This is separate from the global time, and does not affect other players.
    // When that player logs off, their time will be reset to the global time.
    // Additionally, you may instead specify 'reset' to return the player's time back to global time.
    // If you specify a custom time, optionally specify 'reset:<duration>'
    // to set a time after which the player's time will reset (if not manually changed again before then).
    // Optionally specify 'freeze' to lock a player's time in at the specified time value.
    //
    // @Tags
    // <WorldTag.time>
    // <WorldTag.time.period>
    //
    // @Usage
    // Use to set the time in the NPC or Player's world.
    // - time 500t
    //
    // @Usage
    // Use to make the player see a different time than everyone else.
    // - time player 500t
    //
    // @Usage
    // Use to make the player see a different time than everyone else, with the sun no longer moving.
    // - time player 500t freeze
    //
    // @Usage
    // Use to make the player see a different time than everyone else for the next 5 minutes.
    // - time player 500t reset:5m
    //
    // @Usage
    // Use to make the player see the global time again.
    // - time player reset
    //
    // @Usage
    // Use to set the time in a specific world.
    // - time 500t myworld
    //
    // -->

    private enum Type {GLOBAL, PLAYER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.class)) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else if (!scriptEntry.hasObject("value")
                    && !scriptEntry.hasObject("reset")
                    && !arg.matchesPrefix("reset")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("value", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("value")
                    && !scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("freeze")
                    && arg.matches("freeze")) {
                scriptEntry.addObject("freeze", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("reset_after")
                    && arg.matchesPrefix("reset")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("reset_after", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("value") && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Must specify a value!");
        }
        if (!scriptEntry.hasObject("world")) {
            scriptEntry.addObject("world", Utilities.entryDefaultWorld(scriptEntry, false));
        }
        scriptEntry.defaultObject("type", new ElementTag("GLOBAL"));
    }

    public HashMap<UUID, Integer> resetTasks = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {
        DurationTag value = scriptEntry.getObjectTag("value");
        DurationTag resetAfter = scriptEntry.getObjectTag("reset_after");
        WorldTag world = scriptEntry.getObjectTag("world");
        ElementTag type_element = scriptEntry.getElement("type");
        ElementTag reset = scriptEntry.getElement("reset");
        ElementTag freeze = scriptEntry.getElement("freeze");
        Type type = Type.valueOf(type_element.asString().toUpperCase());
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), type_element, reset, value, freeze, resetAfter, world);
        }
        if (type.equals(Type.GLOBAL)) {
            world.getWorld().setTime(value.getTicks());
        }
        else {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                Debug.echoError("Must have a valid player link!");
            }
            else {
                Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                if (reset != null && reset.asBoolean()) {
                    player.resetPlayerTime();
                }
                else {
                    Integer existingTask = resetTasks.get(player.getUniqueId());
                    if (existingTask != null) {
                        Bukkit.getScheduler().cancelTask(existingTask);
                        resetTasks.remove(player.getUniqueId());
                    }
                    if (freeze == null || !freeze.asBoolean()) {
                        // sets a relative time, so subtract by the world time to keep the command absolute
                        player.setPlayerTime(value.getTicks() - player.getWorld().getTime(), true);
                    }
                    else {
                        player.setPlayerTime(value.getTicks(), false);
                    }
                    if (resetAfter != null) {
                        int newTask = Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), player::resetPlayerTime, resetAfter.getTicks());
                        resetTasks.put(player.getUniqueId(), newTask);
                    }
                }
            }
        }
    }
}

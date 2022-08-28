package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class WeatherCommand extends AbstractCommand {

    public WeatherCommand() {
        setName("weather");
        setSyntax("weather ({global}/player) [sunny/storm/thunder/reset] (<world>) (reset:<duration>)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Weather
    // @Syntax weather ({global}/player) [sunny/storm/thunder/reset] (<world>) (reset:<duration>)
    // @Required 1
    // @Maximum 4
    // @Short Changes the current weather in the minecraft world.
    // @Group world
    //
    // @Description
    // By default, changes the weather in the specified world.
    //
    // If you specify 'player', this will change the weather in that player's view.
    // This is separate from the global weather, and does not affect other players.
    // When that player logs off, their weather will be reset to the global weather.
    // Additionally, you may instead specify 'reset' to return the player's weather back to global weather.
    // If you specify a custom weather, optionally specify 'reset:<duration>'
    // to set a time after which the player's weather will reset (if not manually changed again before then).
    // Note that 'thunder' is no different from 'storm' when used on a single player.
    //
    // @Tags
    // <BiomeTag.downfall_type>
    // <PlayerTag.weather>
    // <WorldTag.has_storm>
    // <WorldTag.weather_duration>
    // <WorldTag.thundering>
    // <WorldTag.thunder_duration>
    //
    // @Usage
    // Use to make the weather sunny.
    // - weather sunny
    //
    // @Usage
    // Use to start a storm in world "cookies".
    // - weather storm cookies
    //
    // @Usage
    // Use to start a storm that's only visible to the attached player.
    // - weather player storm
    //
    // @Usage
    // Use to make the player see a storm for 2 minutes.
    // - weather player storm reset:2m
    //
    // @Usage
    // Use to let the player see the global weather again.
    // - weather player reset
    //
    // -->

    private enum Type {GLOBAL, PLAYER}

    private enum Value {SUNNY, STORM, THUNDER, RESET}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.class)) {
                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else if (!scriptEntry.hasObject("value")
                    && arg.matchesEnum(Value.class)) {
                scriptEntry.addObject("value", arg.asElement());
            }
            else if (!scriptEntry.hasObject("reset_after")
                    && arg.matchesPrefix("reset")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("reset_after", arg.asType(DurationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify a value!");
        }
        scriptEntry.defaultObject("type", Type.GLOBAL);
        scriptEntry.defaultObject("world", Utilities.entryDefaultWorld(scriptEntry, false));
    }

    public HashMap<UUID, Integer> resetTasks = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {
        Value value = Value.valueOf((scriptEntry.getElement("value")).asString().toUpperCase());
        WorldTag world = scriptEntry.getObjectTag("world");
        Type type = (Type) scriptEntry.getObject("type");
        DurationTag resetAfter = scriptEntry.getObjectTag("reset_after");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("type", type.name()),
                    (type.name().equalsIgnoreCase("player") ? db("player", Utilities.getEntryPlayer(scriptEntry)) : ""),
                    (type.name().equalsIgnoreCase("global") ? db("world", world) : ""), resetAfter, db("value", value));
        }
        if (type.equals(Type.GLOBAL)) {
            switch (value) {
                case SUNNY:
                    world.getWorld().setStorm(false);
                    world.getWorld().setThundering(false);
                    break;
                case STORM:
                    world.getWorld().setStorm(true);
                    break;
                case THUNDER:
                    world.getWorld().setStorm(true);
                    world.getWorld().setThundering(true);
                    break;
                case RESET:
                    Debug.echoError("Cannot RESET global weather!");
                    break;
            }
        }
        else {
            Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
            Integer existingTask = resetTasks.get(player.getUniqueId());
            if (existingTask != null) {
                Bukkit.getScheduler().cancelTask(existingTask);
                resetTasks.remove(player.getUniqueId());
            }
            if (value == Value.SUNNY) {
                player.setPlayerWeather(WeatherType.CLEAR);
            }
            else if (value == Value.STORM || value == Value.THUNDER) {
                player.setPlayerWeather(WeatherType.DOWNFALL);
            }
            else if (value == Value.RESET) {
                player.resetPlayerWeather();
            }
            if (resetAfter != null) {
                int newTask = Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), player::resetPlayerWeather, resetAfter.getTicks());
                resetTasks.put(player.getUniqueId(), newTask);
            }
        }
    }
}

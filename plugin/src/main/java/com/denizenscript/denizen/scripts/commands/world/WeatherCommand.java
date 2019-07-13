package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;

public class WeatherCommand extends AbstractCommand {

    // <--[command]
    // @Name Weather
    // @Syntax weather [type:{global}/player] [sunny/storm/thunder] (world:<name>)
    // @Required 1
    // @Short Changes the current weather in the minecraft world.
    // @Group world
    //
    // @Description
    // Changes the weather in the specified world.
    // You can also set weather for the attached player, where that player will experience personal
    // weather that is different from the global weather.
    // Logging off will reset personal weather.
    //
    // @Tags
    // <b@biome.downfall_type>
    // <p@player.weather>
    // <w@world.has_storm>
    // <w@world.weather_duration>
    // <w@world.thundering>
    // <w@world.thunder_duration>
    //
    // @Usage
    // Makes the weather sunny
    // - weather sunny
    //
    // @Usage
    // Makes the weather storm in world "cookies"
    // - weather storm world:cookies
    //
    // @Usage
    // Make the weather storm for the attached player.
    // - weather type:player storm
    //
    // -->

    private enum Type {GLOBAL, PLAYER}

    private enum Value {SUNNY, STORM, THUNDER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("world", arg.asType(dWorld.class));
            }
            else if (!scriptEntry.hasObject("value")
                    && arg.matchesEnum(Value.values())) {
                scriptEntry.addObject("value", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled

        if ((!scriptEntry.hasObject("value"))) {
            throw new InvalidArgumentsException("Must specify a value!");
        }

        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to "world" if necessary
        scriptEntry.defaultObject("world",
                Utilities.entryHasNPC(scriptEntry) ? new dWorld(Utilities.getEntryNPC(scriptEntry).getWorld()) : null,
                Utilities.entryHasPlayer(scriptEntry) ? new dWorld(Utilities.getEntryPlayer(scriptEntry).getWorld()) : null,
                Bukkit.getWorlds().get(0));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        Value value = Value.valueOf(((ElementTag) scriptEntry.getObject("value"))
                .asString().toUpperCase());
        dWorld world = (dWorld) scriptEntry.getObject("world");
        Type type = scriptEntry.hasObject("type") ?
                (Type) scriptEntry.getObject("type") : Type.GLOBAL;

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("type", type.name()) +
                    (type.name().equalsIgnoreCase("player") ?
                            ArgumentHelper.debugObj("player", Utilities.getEntryPlayer(scriptEntry)) : "") +
                    (type.name().equalsIgnoreCase("global") ?
                            ArgumentHelper.debugObj("world", world) : "") +
                    ArgumentHelper.debugObj("value", value));
        }

        switch (value) {
            case SUNNY:
                if (type.equals(Type.GLOBAL)) {
                    world.getWorld().setStorm(false);
                    world.getWorld().setThundering(false);
                }
                else {
                    Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().setPlayerWeather(WeatherType.CLEAR);
                }

                break;

            case STORM:
                if (type.equals(Type.GLOBAL)) {
                    world.getWorld().setStorm(true);
                }
                else {
                    Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().setPlayerWeather(WeatherType.DOWNFALL);
                }

                break;

            case THUNDER:
                // Note: setThundering always creates a storm
                if (type.equals(Type.GLOBAL)) {
                    world.getWorld().setThundering(true);
                }
                else {
                    Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().setPlayerWeather(WeatherType.DOWNFALL);
                }

                break;
        }
    }
}

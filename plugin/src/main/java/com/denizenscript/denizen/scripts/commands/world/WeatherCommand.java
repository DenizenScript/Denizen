package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

public class WeatherCommand extends AbstractCommand {

    // <--[command]
    // @Name Weather
    // @Syntax weather [{global}/player] [sunny/storm/thunder] (<world>)
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
    // <BiomeTag.downfall_type>
    // <PlayerTag.weather>
    // <WorldTag.has_storm>
    // <WorldTag.weather_duration>
    // <WorldTag.thundering>
    // <WorldTag.thunder_duration>
    //
    // @Usage
    // Makes the weather sunny
    // - weather sunny
    //
    // @Usage
    // Makes the weather storm in world "cookies"
    // - weather storm cookies
    //
    // @Usage
    // Make the weather storm for the attached player.
    // - weather player storm
    //
    // -->

    private enum Type {GLOBAL, PLAYER}

    private enum Value {SUNNY, STORM, THUNDER}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", Type.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("world")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("world", arg.asType(WorldTag.class));
            }
            else if (!scriptEntry.hasObject("value")
                    && arg.matchesEnum(Value.values())) {
                scriptEntry.addObject("value", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        if ((!scriptEntry.hasObject("value"))) {
            throw new InvalidArgumentsException("Must specify a value!");
        }

        scriptEntry.defaultObject("type", Type.GLOBAL);

        // If the world has not been specified, try to use the NPC's or player's
        // world, or default to "world" if necessary
        scriptEntry.defaultObject("world",
                Utilities.entryHasNPC(scriptEntry) ? new WorldTag(Utilities.getEntryNPC(scriptEntry).getWorld()) : null,
                Utilities.entryHasPlayer(scriptEntry) ? new WorldTag(Utilities.getEntryPlayer(scriptEntry).getWorld()) : null,
                Bukkit.getWorlds().get(0));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        Value value = Value.valueOf(((ElementTag) scriptEntry.getObject("value")).asString().toUpperCase());
        WorldTag world = scriptEntry.getObjectTag("world");
        Type type = (Type) scriptEntry.getObject("type");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("type", type.name()) +
                    (type.name().equalsIgnoreCase("player") ? ArgumentHelper.debugObj("player", Utilities.getEntryPlayer(scriptEntry)) : "") +
                    (type.name().equalsIgnoreCase("global") ? ArgumentHelper.debugObj("world", world) : "") +
                    ArgumentHelper.debugObj("value", value));
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
                    // Note: setThundering always creates a storm
                    world.getWorld().setThundering(true);
                    break;
            }
        }
        else {
            Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
            if (value == Value.SUNNY) {
                player.setPlayerWeather(WeatherType.CLEAR);
            }
            else {
                player.setPlayerWeather(WeatherType.DOWNFALL);
            }
        }
    }
}

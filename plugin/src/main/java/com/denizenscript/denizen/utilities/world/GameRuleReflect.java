package com.denizenscript.denizen.utilities.world;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.lang.invoke.MethodHandle;

public class GameRuleReflect {

    private static final MethodHandle GAMERULE_VALUES = ReflectionHelper.getMethodHandle(GameRule.class, "values");
    private static final MethodHandle GAMERULE_GET_BY_NAME = ReflectionHelper.getMethodHandle(GameRule.class, "getByName", String.class);
    private static final MethodHandle GAMERULE_GET_NAME = ReflectionHelper.getMethodHandle(GameRule.class, "getName");
    private static final MethodHandle GAMERULE_GET_TYPE = ReflectionHelper.getMethodHandle(GameRule.class, "getType");

    public static final GameRule<Boolean> WEATHER_CYCLE_GAMERULE = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) ? GameRule.ADVANCE_WEATHER : (GameRule<Boolean>) getByName("doWeatherCycle");

    public static GameRule<?>[] values() {
        try {
            return (GameRule<?>[]) GAMERULE_VALUES.invokeExact();
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static GameRule<?> getByName(String name) {
        try {
            return (GameRule<?>) GAMERULE_GET_BY_NAME.invokeExact(name);
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getName(GameRule<?> gameRule) {
        try {
            return (String) GAMERULE_GET_NAME.invokeExact(gameRule);
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getType(GameRule<?> gameRule) {
        try {
            return (Class<?>) GAMERULE_GET_TYPE.invokeExact(gameRule);
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // TODO 1.21.11: certain gamemodes (e.g. max_minecart_speed) behave badly on Spigot, need to test with Paper
    public static <T> T getValue(World world, GameRule<T> gameRule) {
        try {
            return world.getGameRuleValue(gameRule);
        }
        catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Tried to access invalid game rule")) {
                return null;
            }
            throw e;
        }
    }
}

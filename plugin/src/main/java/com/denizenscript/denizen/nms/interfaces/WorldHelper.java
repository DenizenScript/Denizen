package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.objects.BiomeTag;
import org.bukkit.Location;
import org.bukkit.World;

public interface WorldHelper {

    boolean isStatic(World world);

    void setStatic(World world, boolean isStatic);

    float getLocalDifficulty(Location location);

    default Location getNearestBiomeLocation(Location start, BiomeTag biome) {
        throw new UnsupportedOperationException();
    }

    boolean areEnoughSleeping(World world, int percentage);

    boolean areEnoughDeepSleeping(World world, int percentage);

    int getSkyDarken(World world);

    boolean isDay(World world);

    boolean isNight(World world);

    /** for setting the time without firing a CUSTOM TimeSkipEvent */
    void setDayTime(World world, long time);

    void wakeUpAllPlayers(World world);

    /** for clearing weather without ignoring possible raised event results */
    void clearWeather(World world);

    default void setGameTime(World world, long time) {
        throw new UnsupportedOperationException();
    }
}

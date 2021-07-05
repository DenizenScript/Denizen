package com.denizenscript.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface FishingHelper {

    enum CatchType {NONE, DEFAULT, JUNK, TREASURE, FISH}

    ItemStack getResult(FishHook fishHook, CatchType catchType);

    FishHook spawnHook(Location location, Player player);

    default FishHook getHookFrom(Player player) {
        throw new UnsupportedOperationException();
    }

    default void setNibble(FishHook hook, int ticks) {
        throw new UnsupportedOperationException();
    }

    default void setHookTime(FishHook hook, int ticks) {
        throw new UnsupportedOperationException();
    }

    default void setLureTime(FishHook hook, int ticks) {
        throw new UnsupportedOperationException();
    }
}

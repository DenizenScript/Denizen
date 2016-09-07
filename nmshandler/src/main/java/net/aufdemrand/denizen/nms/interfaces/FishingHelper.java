package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.entity.FishHook;
import org.bukkit.inventory.ItemStack;

public interface FishingHelper {

    enum CatchType {NONE, DEFAULT, JUNK, TREASURE, FISH}

    ItemStack getResult(FishHook fishHook, CatchType catchType);
}

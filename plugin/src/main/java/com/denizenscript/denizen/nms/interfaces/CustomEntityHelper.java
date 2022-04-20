package com.denizenscript.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface CustomEntityHelper {

    FakeArrow spawnFakeArrow(Location location);

    ItemProjectile spawnItemProjectile(Location location, ItemStack itemStack);

    FakePlayer spawnFakePlayer(Location location, String name, String skin, String blob, boolean doAdd);
}

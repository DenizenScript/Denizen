package com.denizenscript.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.World;

public interface WorldHelper {

    boolean isStatic(World world);

    void setStatic(World world, boolean isStatic);

    float getLocalDifficulty(Location location);
}

package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.World;

public interface WorldHelper {

    boolean isStatic(World world);

    void setStatic(World world, boolean isStatic);

    void setWorldAccess(World world, WorldAccess worldAccess);

    void removeWorldAccess(World world);
}

package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.interfaces.WorldHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.server.v1_17_R1.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

public class WorldHelperImpl implements WorldHelper {

    @Override
    public boolean isStatic(World world) {
        return ((CraftWorld) world).getHandle().isClientSide;
    }

    @Override
    public void setStatic(World world, boolean isStatic) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_17_R1.World.class, "isClientSide", worldServer, isStatic);
    }

    @Override
    public float getLocalDifficulty(Location location) {
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        DifficultyDamageScaler scaler = ((CraftWorld) location.getWorld()).getHandle().getDamageScaler(pos);
        return scaler.b();
    }
}

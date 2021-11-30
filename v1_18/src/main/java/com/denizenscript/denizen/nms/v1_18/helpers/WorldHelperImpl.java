package com.denizenscript.denizen.nms.v1_18.helpers;

import com.denizenscript.denizen.nms.interfaces.WorldHelper;
import com.denizenscript.denizen.nms.v1_18.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_18.impl.BiomeNMSImpl;
import com.denizenscript.denizen.objects.BiomeTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;

public class WorldHelperImpl implements WorldHelper {

    @Override
    public boolean isStatic(World world) {
        return ((CraftWorld) world).getHandle().isClientSide;
    }

    @Override
    public void setStatic(World world, boolean isStatic) {
        ServerLevel worldServer = ((CraftWorld) world).getHandle();
        ReflectionHelper.setFieldValue(net.minecraft.world.level.Level.class, ReflectionMappingsInfo.Level_isClientSide, worldServer, isStatic);
    }

    @Override
    public float getLocalDifficulty(Location location) {
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        DifficultyInstance scaler = ((CraftWorld) location.getWorld()).getHandle().getCurrentDifficultyAt(pos);
        return scaler.getEffectiveDifficulty();
    }

    @Override
    public Location getNearestBiomeLocation(Location start, BiomeTag biome) {
        BlockPos result = ((CraftWorld) start.getWorld()).getHandle().findNearestBiome(((BiomeNMSImpl) biome.getBiome()).biomeBase, new BlockPos(start.getBlockX(), start.getBlockY(), start.getBlockZ()), 6400, 8);
        if (result == null) {
            return null;
        }
        return new Location(start.getWorld(), result.getX(), result.getY(), result.getZ());
    }
}

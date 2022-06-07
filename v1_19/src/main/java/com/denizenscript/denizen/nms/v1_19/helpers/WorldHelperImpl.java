package com.denizenscript.denizen.nms.v1_19.helpers;

import com.denizenscript.denizen.nms.interfaces.WorldHelper;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_19.impl.BiomeNMSImpl;
import com.denizenscript.denizen.objects.BiomeTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;

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
        Pair<BlockPos, Holder<Biome>> result = ((CraftWorld) start.getWorld()).getHandle()
                .findNearestBiome(b -> b.is(((BiomeNMSImpl) biome.getBiome()).biomeBase.unwrapKey().get()), new BlockPos(start.getBlockX(), start.getBlockY(), start.getBlockZ()), 6400, 8);
        if (result == null || result.getFirst() == null) {
            return null;
        }
        return new Location(start.getWorld(), result.getFirst().getX(), result.getFirst().getY(), result.getFirst().getZ());
    }
}

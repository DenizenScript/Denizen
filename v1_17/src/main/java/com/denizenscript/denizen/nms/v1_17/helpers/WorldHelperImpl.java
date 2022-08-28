package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizen.nms.interfaces.WorldHelper;
import com.denizenscript.denizen.nms.v1_17.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_17.impl.BiomeNMSImpl;
import com.denizenscript.denizen.objects.BiomeTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.PrimaryLevelData;
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

    @Override
    public boolean areEnoughSleeping(World world, int percentage) {
        SleepStatus status = ReflectionHelper.getFieldValue(ServerLevel.class, ReflectionMappingsInfo.ServerLevel_sleepStatus, ((CraftWorld) world).getHandle());
        return status.areEnoughSleeping(percentage);
    }

    @Override
    public boolean areEnoughDeepSleeping(World world, int percentage) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        SleepStatus status = ReflectionHelper.getFieldValue(ServerLevel.class, ReflectionMappingsInfo.ServerLevel_sleepStatus, level);
        return status.areEnoughDeepSleeping(percentage, level.players());
    }

    @Override
    public int getSkyDarken(World world) {
        return ((CraftWorld) world).getHandle().getSkyDarken();
    }

    @Override
    public boolean isDay(World world) {
        return ((CraftWorld) world).getHandle().isDay();
    }

    @Override
    public boolean isNight(World world) {
        return ((CraftWorld) world).getHandle().isNight();
    }

    @Override
    public void setDayTime(World world, long time) {
        ((CraftWorld) world).getHandle().setDayTime(time);
    }

    // net.minecraft.server.level.ServerLevel#wakeUpAllPlayers()
    @Override
    public void wakeUpAllPlayers(World world) {
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        SleepStatus status = ReflectionHelper.getFieldValue(ServerLevel.class, ReflectionMappingsInfo.ServerLevel_sleepStatus, nmsWorld);
        status.removeAllSleepers();
        nmsWorld.getPlayers(LivingEntity::isSleeping).forEach((player) -> player.stopSleepInBed(false, false));
    }

    // net.minecraft.server.level.ServerLevel#stopWeather()
    @Override
    public void clearWeather(World world) {
        PrimaryLevelData data = ((CraftWorld) world).getHandle().E;
        data.setRaining(false);
        if (!data.isRaining()) {
            data.setRainTime(0);
        }
        data.setThundering(false);
        if (!data.isThundering()) {
            data.setThunderTime(0);
        }
    }
}

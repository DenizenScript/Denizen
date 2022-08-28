package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.interfaces.WorldHelper;
import com.denizenscript.denizen.nms.v1_16.impl.BiomeNMSImpl;
import com.denizenscript.denizen.objects.BiomeTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.util.stream.Collectors;

public class WorldHelperImpl implements WorldHelper {

    @Override
    public boolean isStatic(World world) {
        return ((CraftWorld) world).getHandle().isClientSide;
    }

    @Override
    public void setStatic(World world, boolean isStatic) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_16_R3.World.class, "isClientSide", worldServer, isStatic);
    }

    @Override
    public float getLocalDifficulty(Location location) {
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        DifficultyDamageScaler scaler = ((CraftWorld) location.getWorld()).getHandle().getDamageScaler(pos);
        return scaler.b();
    }

    @Override
    public Location getNearestBiomeLocation(Location start, BiomeTag biome) {
        BlockPosition result = ((CraftWorld) start.getWorld()).getHandle().a(((BiomeNMSImpl) biome.getBiome()).biomeBase, new BlockPosition(start.getBlockX(), start.getBlockY(), start.getBlockZ()), 6400, 8);
        if (result == null) {
            return null;
        }
        return new Location(start.getWorld(), result.getX(), result.getY(), result.getZ());
    }

    @Override
    public boolean areEnoughSleeping(World world, int percentage) {
        // percentage is ignored -- prior to 1.17, all players must be sleeping
        return ReflectionHelper.getFieldValue(WorldServer.class, "everyoneSleeping", ((CraftWorld) world).getHandle());
    }

    @Override
    public boolean areEnoughDeepSleeping(World world, int percentage) {
        // percentage is ignored -- prior to 1.17, all players must be sleeping
        return ((CraftWorld) world).getHandle().getPlayers().stream().noneMatch((player) -> {
            return !player.isSpectator() && !player.isDeeplySleeping() && !player.fauxSleeping;
        });
    }

    @Override
    public int getSkyDarken(World world) {
        return ((CraftWorld) world).getHandle().c();
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

    // net.minecraft.server.WorldServer#wakeupPlayers()
    @Override
    public void wakeUpAllPlayers(World world) {
        WorldServer nmsWorld = ((CraftWorld) world).getHandle();
        ReflectionHelper.setFieldValue(WorldServer.class, "everyoneSleeping", nmsWorld, false);
        nmsWorld.getPlayers().stream().filter(EntityLiving::isSleeping).collect(Collectors.toList()).forEach((player) -> player.wakeup(false, false));
    }

    // net.minecraft.server.WorldServer#clearWeather()
    @Override
    public void clearWeather(World world) {
        WorldDataServer data = ((CraftWorld) world).getHandle().worldDataServer;
        data.setStorm(false);
        if (!data.hasStorm()) {
            data.setWeatherDuration(0);
        }
        data.setThundering(false);
        if (!data.isThundering()) {
            data.setThunderDuration(0);
        }
    }
}

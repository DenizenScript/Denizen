package com.denizenscript.denizen.nms.helpers;

import com.denizenscript.denizen.nms.interfaces.WorldAccess;
import com.denizenscript.denizen.nms.interfaces.WorldHelper;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldHelper_v1_12_R1 implements WorldHelper {

    private final Map<World, IWorldAccess> worldAccessMap = new HashMap<>();

    @Override
    public boolean isStatic(World world) {
        return ((CraftWorld) world).getHandle().isClientSide;
    }

    @Override
    public void setStatic(World world, boolean isStatic) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_12_R1.World.class, "isClientSide", worldServer, isStatic);
    }

    @Override
    public void setWorldAccess(World world, final WorldAccess worldAccess) {
        if (worldAccessMap.containsKey(world)) {
            removeWorldAccess(world);
        }
        IWorldAccess nmsWorldAccess = new IWorldAccess() {
            @Override
            public void a(net.minecraft.server.v1_12_R1.World world, BlockPosition blockPosition, IBlockData iBlockData, IBlockData iBlockData1, int i) {
            }

            @Override
            public void a(BlockPosition blockPosition) {
            }

            @Override
            public void a(int i, int i1, int i2, int i3, int i4, int i5) {
            }

            @Override
            public void a(@Nullable EntityHuman entityHuman, SoundEffect soundEffect, SoundCategory soundCategory, double v, double v1, double v2, float v3, float v4) {
            }

            @Override
            public void a(SoundEffect soundEffect, BlockPosition blockPosition) {
            }

            @Override
            public void a(int i, boolean b, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {
            }

            @Override
            public void a(int i, boolean b, boolean b1, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {
            }

            @Override
            public void a(Entity entity) {
            }

            @Override
            public void b(Entity entity) {
                worldAccess.despawn(entity.getBukkitEntity());
            }

            @Override
            public void a(int i, BlockPosition blockPosition, int i1) {
            }

            @Override
            public void a(EntityHuman entityHuman, int i, BlockPosition blockPosition, int i1) {
            }

            @Override
            public void b(int i, BlockPosition blockPosition, int i1) {
            }
        };
        worldAccessMap.put(world, nmsWorldAccess);
        ((CraftWorld) world).getHandle().addIWorldAccess(nmsWorldAccess);
    }

    @Override
    public void removeWorldAccess(World world) {
        if (!worldAccessMap.containsKey(world)) {
            return;
        }
        net.minecraft.server.v1_12_R1.World nmsWorld = ((CraftWorld) world).getHandle();
        List<IWorldAccess> list = ReflectionHelper.getFieldValue(net.minecraft.server.v1_12_R1.World.class, "u", nmsWorld);
        if (list != null) {
            list.remove(worldAccessMap.get(world));
        }
        worldAccessMap.remove(world);
    }
}

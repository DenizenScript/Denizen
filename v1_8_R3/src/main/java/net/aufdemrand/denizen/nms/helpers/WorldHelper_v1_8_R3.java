package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.WorldAccess;
import net.aufdemrand.denizen.nms.interfaces.WorldHelper;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IWorldAccess;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldHelper_v1_8_R3 implements WorldHelper {

    private final Map<World, IWorldAccess> worldAccessMap = new HashMap<World, IWorldAccess>();

    @Override
    public boolean isStatic(World world) {
        return ((CraftWorld) world).getHandle().isClientSide;
    }

    @Override
    public void setStatic(World world, boolean isStatic) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        ReflectionHelper.setFieldValue(net.minecraft.server.v1_8_R3.World.class, "isClientSide", worldServer, isStatic);
    }

    @Override
    public void setWorldAccess(World world, final WorldAccess worldAccess) {
        if (worldAccessMap.containsKey(world)) {
            removeWorldAccess(world);
        }
        IWorldAccess nmsWorldAccess = new IWorldAccess() {
            @Override
            public void a(BlockPosition blockPosition) {
            }

            @Override
            public void b(BlockPosition blockPosition) {
            }

            @Override
            public void a(int i, int i1, int i2, int i3, int i4, int i5) {
            }

            @Override
            public void a(String s, double v, double v1, double v2, float v3, float v4) {
            }

            @Override
            public void a(EntityHuman entityHuman, String s, double v, double v1, double v2, float v3, float v4) {
            }

            @Override
            public void a(int i, boolean b, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {
            }

            @Override
            public void a(Entity entity) {
            }

            @Override
            public void b(Entity entity) {
                worldAccess.despawn(entity.getBukkitEntity());
            }

            @Override
            public void a(String s, BlockPosition blockPosition) {
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
        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        List<IWorldAccess> list = ReflectionHelper.getFieldValue(net.minecraft.server.v1_8_R3.World.class, "u", nmsWorld);
        if (list != null) {
            list.remove(worldAccessMap.get(world));
        }
        worldAccessMap.remove(world);
    }
}

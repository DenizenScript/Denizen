package com.denizenscript.denizen.nms.v1_18.helpers;

import com.denizenscript.denizen.utilities.implementation.DenizenCoreImplementation;
import com.denizenscript.denizen.nms.interfaces.ChunkHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

public class ChunkHelperImpl implements ChunkHelper {

    public final static Field chunkProviderServerThreadField;
    public final static MethodHandle chunkProviderServerThreadFieldSetter;
    public final static Field worldThreadField;
    public final static MethodHandle worldThreadFieldSetter;

    static {
        chunkProviderServerThreadField = ReflectionHelper.getFields(ServerChunkCache.class).getFirstOfType(Thread.class);
        chunkProviderServerThreadFieldSetter = ReflectionHelper.getFinalSetterForFirstOfType(ServerChunkCache.class, Thread.class);
        worldThreadField = ReflectionHelper.getFields(net.minecraft.world.level.Level.class).getFirstOfType(Thread.class);
        worldThreadFieldSetter = ReflectionHelper.getFinalSetterForFirstOfType(net.minecraft.world.level.Level.class, Thread.class);
    }

    public Thread resetServerThread;

    @Override
    public void changeChunkServerThread(World world) {
        if (DenizenCoreImplementation.tagThread == null) {
            return;
        }
        if (resetServerThread != null) {
            return;
        }
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ServerChunkCache provider = nmsWorld.getChunkSource();
        try {
            resetServerThread = (Thread) chunkProviderServerThreadField.get(provider);
            chunkProviderServerThreadFieldSetter.invoke(provider, Thread.currentThread());
            worldThreadFieldSetter.invoke(nmsWorld, Thread.currentThread());
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void restoreServerThread(World world) {
        if (DenizenCoreImplementation.tagThread == null) {
            return;
        }
        if (resetServerThread == null) {
            return;
        }
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ServerChunkCache provider = nmsWorld.getChunkSource();
        try {
            chunkProviderServerThreadFieldSetter.invoke(provider, resetServerThread);
            worldThreadFieldSetter.invoke(nmsWorld, resetServerThread);
            resetServerThread = null;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        Heightmap map = ((CraftChunk) chunk).getHandle().heightmaps.get(Heightmap.Types.MOTION_BLOCKING);
        int[] outputMap = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                outputMap[x * 16 + y] = map.getFirstAvailable(x, y);
            }
        }
        return outputMap;
    }
}

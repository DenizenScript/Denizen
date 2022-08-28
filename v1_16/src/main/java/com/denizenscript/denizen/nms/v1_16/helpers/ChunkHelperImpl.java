package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.interfaces.ChunkHelper;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

public class ChunkHelperImpl implements ChunkHelper {

    public final static Field chunkProviderServerThreadField;
    public final static MethodHandle chunkProviderServerThreadFieldSetter;
    public final static Field worldThreadField;
    public final static MethodHandle worldThreadFieldSetter;

    static {
        chunkProviderServerThreadField = ReflectionHelper.getFields(ChunkProviderServer.class).get("serverThread");
        chunkProviderServerThreadFieldSetter = ReflectionHelper.getFinalSetter(ChunkProviderServer.class, "serverThread");
        worldThreadField = ReflectionHelper.getFields(net.minecraft.server.v1_16_R3.World.class).get("serverThread");
        worldThreadFieldSetter = ReflectionHelper.getFinalSetter(net.minecraft.server.v1_16_R3.World.class, "serverThread");
    }

    public Thread resetServerThread;

    @Override
    public void changeChunkServerThread(World world) {
        if (TagManager.tagThread == null) {
            return;
        }
        if (resetServerThread != null) {
            return;
        }
        WorldServer nmsWorld = ((CraftWorld) world).getHandle();
        ChunkProviderServer provider = nmsWorld.getChunkProvider();
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
        if (TagManager.tagThread == null) {
            return;
        }
        if (resetServerThread == null) {
            return;
        }
        WorldServer nmsWorld = ((CraftWorld) world).getHandle();
        ChunkProviderServer provider = nmsWorld.getChunkProvider();
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
    public void refreshChunkSections(Chunk chunk) {
        // TODO: 1.16: is false for 'Ignore old data' in the packets below good?
        PacketPlayOutMapChunk lowPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 255); // 00000000 11111111
        PacketPlayOutMapChunk highPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65280); // 11111111 00000000
        ChunkCoordIntPair pos = new ChunkCoordIntPair(chunk.getX(), chunk.getZ());
        PlayerChunk playerChunk = ((CraftWorld) chunk.getWorld()).getHandle().getChunkProvider().playerChunkMap.visibleChunks.get(pos.pair());
        if (playerChunk == null) {
            return;
        }
        playerChunk.players.a(pos, false).forEach(player -> {
            player.playerConnection.sendPacket(lowPacket);
            player.playerConnection.sendPacket(highPacket);
        });
    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        HeightMap map = ((CraftChunk) chunk).getHandle().heightMap.get(HeightMap.Type.MOTION_BLOCKING);
        int[] outputMap = new int[256];
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                outputMap[x * 16 + y] = map.a(x, y);
            }
        }
        return outputMap;
    }
}

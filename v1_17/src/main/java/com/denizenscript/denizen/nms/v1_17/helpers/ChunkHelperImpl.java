package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_17.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.interfaces.ChunkHelper;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

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
        if (TagManager.tagThread == null) {
            return;
        }
        if (resetServerThread != null) {
            return;
        }
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ServerChunkCache provider = nmsWorld.getChunkProvider();
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
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ServerChunkCache provider = nmsWorld.getChunkProvider();
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
        ClientboundLevelChunkPacket packet = new ClientboundLevelChunkPacket(((CraftChunk) chunk).getHandle());
        ChunkPos pos = new ChunkPos(chunk.getX(), chunk.getZ());
        ChunkHolder playerChunk = ((CraftWorld) chunk.getWorld()).getHandle().getChunkProvider().chunkMap.l.get(pos.toLong());
        if (playerChunk == null) {
            return;
        }
        playerChunk.playerProvider.getPlayers(pos, false).forEach(player -> {
            player.connection.send(packet);
        });
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

    @Override
    public void setAllBiomes(Chunk chunk, BiomeNMS biome) {
        Biome nmsBiome = ((BiomeNMSImpl) biome).biomeBase;
        LevelChunk nmsChunk = ((CraftChunk) chunk).getHandle();
        ChunkBiomeContainer biomeContainer = nmsChunk.getBiomes();
        for(int x = 0; x < 4; x++) {
            for (int y = 0; y < 64; y++) {
                for (int z = 0; z < 4; z++) {
                    biomeContainer.setBiome(x, y, z, nmsBiome);
                }
            }
        }
        nmsChunk.markUnsaved();
    }
}

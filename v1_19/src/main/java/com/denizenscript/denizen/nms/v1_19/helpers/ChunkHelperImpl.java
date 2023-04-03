package com.denizenscript.denizen.nms.v1_19.helpers;

import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_19.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.interfaces.ChunkHelper;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;

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
        if (TagManager.tagThread == null) {
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
        Heightmap map = ((CraftChunk) chunk).getHandle(ChunkStatus.HEIGHTMAPS).heightmaps.get(Heightmap.Types.MOTION_BLOCKING);
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
        Holder<Biome> nmsBiome = ((BiomeNMSImpl) biome).biomeHolder;
        ChunkAccess nmsChunk = ((CraftChunk) chunk).getHandle(ChunkStatus.BIOMES);
        ChunkPos chunkcoordintpair = nmsChunk.getPos();
        int i = QuartPos.fromBlock(chunkcoordintpair.getMinBlockX());
        int j = QuartPos.fromBlock(chunkcoordintpair.getMinBlockZ());
        LevelHeightAccessor levelheightaccessor = nmsChunk.getHeightAccessorForGeneration();
        for(int k = levelheightaccessor.getMinSection(); k < levelheightaccessor.getMaxSection(); ++k) {
            LevelChunkSection chunksection = nmsChunk.getSection(nmsChunk.getSectionIndexFromSectionY(k));
            PalettedContainer<Holder<Biome>> datapaletteblock = (PalettedContainer<Holder<Biome>>) chunksection.getBiomes();
            datapaletteblock.acquire();
            for(int l = 0; l < 4; ++l) {
                for(int i1 = 0; i1 < 4; ++i1) {
                    for(int j1 = 0; j1 < 4; ++j1) {
                        datapaletteblock.getAndSetUnchecked(l, i1, j1, nmsBiome);
                    }
                }
            }
            datapaletteblock.release();
        }
    }
}

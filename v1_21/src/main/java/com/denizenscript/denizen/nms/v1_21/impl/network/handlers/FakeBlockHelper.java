package com.denizenscript.denizen.nms.v1_21.impl.network.handlers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R5.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.block.CraftBlockType;
import org.bukkit.craftbukkit.v1_21_R5.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftLocation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

public class FakeBlockHelper {

    public static final MethodHandle CHUNKDATA_BLOCK_ENTITIES = ReflectionHelper.getFields(ClientboundLevelChunkPacketData.class).getGetter(ReflectionMappingsInfo.ClientboundLevelChunkPacketData_blockEntitiesData, List.class);
    public static final MethodHandle CHUNKDATA_BUFFER_SETTER = ReflectionHelper.getFields(ClientboundLevelChunkPacketData.class).getSetter(ReflectionMappingsInfo.ClientboundLevelChunkPacketData_buffer, byte[].class);
    public static final Class<?> CHUNKDATA_BLOCKENTITYINFO_CLASS = ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0];
    public static final MethodHandle CHUNKDATA_BLOCK_ENTITY_CREATE = ReflectionHelper.getMethodHandle(CHUNKDATA_BLOCKENTITYINFO_CLASS, ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_create_method, BlockEntity.class);
    public static final MethodHandle CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).getGetter(ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_packedXZ);
    public static final MethodHandle CHUNKDATA_BLOCKENTITYINFO_Y = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).getGetter(ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_y);
    public static final MethodHandle BLOCK_ENTITY_TYPE_VALID_BLOCKS = ReflectionHelper.getFields(BlockEntityType.class).getGetter(ReflectionMappingsInfo.BlockEntityType_validBlocks, Set.class);

    public static final Map<Material, BlockEntityType<?>> MATERIAL_BLOCK_ENTITY_TYPES = new EnumMap<>(Material.class);

    static {
        try {
            for (BlockEntityType<?> nmsBlockEntityType : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
                Set<Block> validBlocks = (Set<Block>) BLOCK_ENTITY_TYPE_VALID_BLOCKS.invokeExact(nmsBlockEntityType);
                for (Block validBlock : validBlocks) {
                    MATERIAL_BLOCK_ENTITY_TYPES.put(CraftBlockType.minecraftToBukkit(validBlock), nmsBlockEntityType);
                }
            }
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static BlockState getNMSState(FakeBlock block) {
        return ((CraftBlockData) block.material.getModernData()).getState();
    }

    public static boolean anyBlocksInSection(List<FakeBlock> blocksInChunk, int y) {
        int minY = SectionPos.sectionToBlockCoord(y);
        int maxY = minY + 16;
        for (FakeBlock block : blocksInChunk) {
            int blockY = block.location.getBlockY();
            if (blockY >= minY && blockY < maxY) {
                return true;
            }
        }
        return false;
    }

    public static Field PAPER_CHUNK_READY;
    public static boolean tryPaperPatch = true;

    public static void copyPacketPaperPatch(ClientboundLevelChunkWithLightPacket newPacket) {
        if (!Denizen.supportsPaper || !tryPaperPatch) {
            return;
        }
        try {
            if (PAPER_CHUNK_READY == null) {
                PAPER_CHUNK_READY = ReflectionHelper.getFields(ClientboundLevelChunkWithLightPacket.class).get("ready");
            }
        }
        catch (Throwable ex) {
            tryPaperPatch = false;
            Debug.echoError("Paper packet patch failed:");
            Debug.echoError(ex);
            return;
        }
        try {
            PAPER_CHUNK_READY.setBoolean(newPacket, true);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static ClientboundLevelChunkWithLightPacket handleMapChunkPacket(World world, ClientboundLevelChunkWithLightPacket originalChunkPacket, int chunkX, int chunkZ, List<FakeBlock> blocksInChunk, FakeBlock.FakeBlockMap fakeBlockMap) throws Throwable {
        ClientboundLevelChunkWithLightPacket copiedChunkPacket = DenizenNetworkManagerImpl.copyPacket(originalChunkPacket, ClientboundLevelChunkWithLightPacket.STREAM_CODEC);
        copyPacketPaperPatch(copiedChunkPacket);
        // A list of block entities sent with the chunk data
        List<Object> blockEntities = (List<Object>) CHUNKDATA_BLOCK_ENTITIES.invoke(copiedChunkPacket.getChunkData());
        LocationTag location = new LocationTag(world, 0, 0, 0);
        ListIterator<Object> blockEntitiesIterator = blockEntities.listIterator();
        while (blockEntitiesIterator.hasNext()) {
            Object blockEnt = blockEntitiesIterator.next();
            int xz = (int) CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ.invoke(blockEnt);
            int y = (int) CHUNKDATA_BLOCKENTITYINFO_Y.invoke(blockEnt);
            int relativeX = SectionPos.sectionRelative(xz >> 4);
            int relativeZ = SectionPos.sectionRelative(xz);
            int x = SectionPos.sectionToBlockCoord(chunkX) + relativeX;
            int z = SectionPos.sectionToBlockCoord(chunkZ) + relativeZ;
            location.setX(x);
            location.setY(y);
            location.setZ(z);
            if (fakeBlockMap.byLocation.containsKey(location)) {
                blockEntitiesIterator.remove();
            }
        }
        // Get the original chunk data to read, and a buf of the same size to write
        FriendlyByteBuf rawChunkData = originalChunkPacket.getChunkData().getReadBuffer();
        FriendlyByteBuf newChunkData = new FriendlyByteBuf(Unpooled.buffer(rawChunkData.readableBytes()));
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();
        int minChunkY = SectionPos.blockToSectionCoord(worldMinY);
        int maxChunkY = SectionPos.blockToSectionCoord(worldMaxY);
        Registry<Biome> biomeRegistry = CraftRegistry.getMinecraftRegistry(Registries.BIOME);
        int blocksLit = 0, skyLit = 0;
        // These are section coords, iterating through every chunk section
        ClientboundLightUpdatePacketData lightData = copiedChunkPacket.getLightData();
        Long2ObjectMap<SectionLightCache> sectionLightCache = new Long2ObjectOpenHashMap<>();
        for (int y = minChunkY; y < maxChunkY; y++) {
            SectionPos sectionPos = SectionPos.of(chunkX, y, chunkZ);
            int sectionIndex = y + Math.abs(minChunkY) + 1;
            boolean hasSky = false, hasBlock = false;
            if (lightData.getBlockYMask().get(sectionIndex)) {
                hasBlock = true;
            }
            if (lightData.getSkyYMask().get(sectionIndex)) {
                hasSky = true;
            }
            int blockCount = rawChunkData.readShort();
            PalettedContainer<BlockState> states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
            states.read(rawChunkData);
            PalettedContainer<Biome> biomes = new PalettedContainer<>(biomeRegistry, biomeRegistry.getValueOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
            biomes.read(rawChunkData);
            if (anyBlocksInSection(blocksInChunk, y)) {
                int minY = SectionPos.sectionToBlockCoord(y);
                int maxY = minY + 16;
                for (FakeBlock block : blocksInChunk) {
                    int blockY = block.location.getBlockY();
                    if (blockY >= minY && blockY < maxY && block.material != null) {
                        int relativeX = SectionPos.sectionRelative(block.location.getBlockX());
                        int relativeY = SectionPos.sectionRelative(blockY);
                        int relativeZ = SectionPos.sectionRelative(block.location.getBlockZ());
                        BlockState oldState = states.get(relativeX, relativeY, relativeZ);
                        BlockState newState = getNMSState(block);
                        if (oldState.isAir() && !newState.isAir()) {
                            blockCount++;
                        }
                        else if (newState.isAir() && !oldState.isAir()) {
                            blockCount--;
                        }
                        states.set(relativeX, relativeY, relativeZ, newState);
                        BlockEntityType<?> nmsBlockEntityType = MATERIAL_BLOCK_ENTITY_TYPES.get(block.material.getMaterial());
                        if (nmsBlockEntityType == null) {
                            continue;
                        }
                        BlockEntity createdBlockEntity = nmsBlockEntityType.create(CraftLocation.toBlockPosition(block.location), newState);
                        createdBlockEntity.setLevel(((CraftWorld) world).getHandle());
                        Object packetBlockEntityData = CHUNKDATA_BLOCK_ENTITY_CREATE.invoke(createdBlockEntity);
                        blockEntities.add(packetBlockEntityData);
                        DataLayer blockLights;
                        if (!hasBlock) {
                            Debug.log(">>>>>>>>>>>>>>>>>>> No light data, adding");
                            blockLights = new DataLayer();
                            lightData.getBlockUpdates().add(blocksLit, blockLights.getData());
                            lightData.getBlockYMask().set(sectionIndex);
                            lightData.getEmptyBlockYMask().clear(sectionIndex);
                            hasBlock = true;
                        }
                        else {
                            blockLights = new DataLayer(lightData.getBlockUpdates().get(blocksLit));
                        }
                        DataLayer skyLights = hasSky ? new DataLayer(lightData.getSkyUpdates().get(skyLit)) : new DataLayer();
                        blockLights.set(relativeX, relativeY, relativeZ, getEstimatedLightLevel(relativeX, relativeY, relativeZ, blockLights, skyLights, lightData, world, sectionPos, sectionLightCache, sectionIndex, blocksLit, skyLit));
                    }
                }
            }
            if (hasBlock) {
                blocksLit++;
            }
            if (hasSky) {
                skyLit++;
            }
            newChunkData.writeShort(blockCount);
            states.write(newChunkData);
            biomes.write(newChunkData);
        }
        CHUNKDATA_BUFFER_SETTER.invoke(copiedChunkPacket.getChunkData(), newChunkData.array());
        return copiedChunkPacket;
    }

    public static final int[][] directions = {
            {0, -1, 0},
            {0, 1, 0},
            {-1, 0, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, 0, 1}
    };

    public static int getEstimatedLightLevel(int relativeX, int relativeY, int relativeZ, DataLayer blockLights, DataLayer skyLights, ClientboundLightUpdatePacketData lightPacket, World world, SectionPos sectionPos, Long2ObjectMap<SectionLightCache> sectionLightsCache, int sectionIndex, int blocksLit, int skyLit) {
        int maxLight = maxLight(relativeX, relativeY, relativeZ, blockLights, skyLights);
        if (maxLight == 15) {
            return 15;
        }
        List<BlockPos> blockLookups = null;
        for (int[] direction : directions) {
            int yOffest = direction[1];
            int neighborX = relativeX + direction[0];
            int neighborY = relativeY + yOffest;
            int neighborZ = relativeZ + direction[2];
            if (coordOutOfSection(neighborX) || coordOutOfSection(neighborZ)) {
                if (blockLookups == null) {
                    blockLookups = new ArrayList<>(2);
                }
                blockLookups.add(new BlockPos(sectionPos.minBlockX() + neighborX, sectionPos.minBlockY() + neighborY, sectionPos.minBlockZ() + neighborZ));
                continue;
            }
            int light;
            if (coordOutOfSection(neighborY)) {
                int adjacentSectionIndex = sectionIndex + yOffest;
                boolean hasSkyLights = lightPacket.getSkyYMask().get(adjacentSectionIndex);
                boolean hasBlockLights = lightPacket.getBlockYMask().get(adjacentSectionIndex);
                if (!hasBlockLights && !hasSkyLights) {
                    Debug.log("No lights in adjacent section");
                    continue;
                }
                int wrappedNeighborY = SectionPos.sectionRelative(neighborY);
                int skyLight = hasSkyLights ? new DataLayer(lightPacket.getSkyUpdates().get(skyLit + yOffest)).get(neighborX, wrappedNeighborY, neighborZ) : 0;
                if (skyLight == 15 || !hasBlockLights) {
                    return skyLight;
                }
                int blockLight = new DataLayer(lightPacket.getBlockUpdates().get(blocksLit + yOffest)).get(neighborX, wrappedNeighborY, neighborZ);
                light = Math.max(skyLight, blockLight);
                Debug.log("Adjacent packet light: " + light);
            }
            else {
                light = maxLight(neighborX, neighborY, neighborZ, blockLights, skyLights);
                Debug.log("Packet light: " + light);
            }
            if (light == 15) {
                return 15;
            }
            if (light > maxLight) {
                maxLight = light;
            }
        }
        if (blockLookups == null) {
            return maxLight;
        }
        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        for (BlockPos blockPos : blockLookups) {
            SectionPos containingSection = SectionPos.of(blockPos);
            SectionLightCache sectionLight = sectionLightsCache.computeIfAbsent(containingSection.asLong(), k -> {
                Debug.log("Getting section to cache");
                LevelLightEngine lightEngine = nmsWorld.getLightEngine();
                DataLayer sectionBlockLights = lightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(containingSection);
                DataLayer sectionSkyLights = lightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(containingSection);
                return new SectionLightCache(sectionBlockLights, sectionSkyLights);
            });
            int light = sectionLight.getLight(blockPos);
            Bukkit.getScheduler().runTaskLater(Denizen.getInstance(), () -> {
                NMSHandler.packetHelper.showDebugTestMarker(Bukkit.getOnlinePlayers().iterator().next(), CraftLocation.toBukkit(blockPos), ColorTag.valueOf("red", null), "", 4000);
            }, 1);
            Debug.log("Block light: " + light);
            if (light == 15) {
                return 15;
            }
            if (light > maxLight) {
                maxLight = light;
            }
        }
        return maxLight;
    }

    public static int maxLight(int relativeX, int relativeY, int relativeZ, DataLayer blockLightData, DataLayer skyLightData) {
        int skyLight = skyLightData.get(relativeX, relativeY, relativeZ);
        if (skyLight == 15) {
            return 15;
        }
        int blockLight = blockLightData.get(relativeX, relativeY, relativeZ);
        return Math.max(skyLight, blockLight);
    }

    public record SectionLightCache(DataLayer blockLights, DataLayer skyLights) {

        public int getLight(BlockPos blockPos) {
            int relativeX = SectionPos.sectionRelative(blockPos.getX());
            int relativeY = SectionPos.sectionRelative(blockPos.getY());
            int relativeZ = SectionPos.sectionRelative(blockPos.getZ());
            int skyLight = skyLights != null ? skyLights.get(relativeX, relativeY, relativeZ) : 0;
            if (skyLight == 15) {
                return skyLight;
            }
            int blockLight = blockLights != null ? blockLights.get(relativeX, relativeY, relativeZ) : 0;
            return Math.max(skyLight, blockLight);
        }
    }

    public static boolean coordOutOfSection(int relativeCoord) {
        return relativeCoord < 0 || relativeCoord > 15;
    }
}

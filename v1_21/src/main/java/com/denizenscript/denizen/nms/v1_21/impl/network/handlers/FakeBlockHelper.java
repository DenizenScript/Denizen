package com.denizenscript.denizen.nms.v1_21.impl.network.handlers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
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
        // A list of ClientboundLevelChunkPacketData$BlockEntityInfo
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
        // Get the original chunk data to read, and a new buf of the same size to write
        FriendlyByteBuf rawChunkData = originalChunkPacket.getChunkData().getReadBuffer();
        FriendlyByteBuf newChunkData = new FriendlyByteBuf(Unpooled.buffer(rawChunkData.readableBytes()));
        final int minChunkY = SectionPos.blockToSectionCoord(world.getMinHeight());
        final int maxChunkY = SectionPos.blockToSectionCoord(world.getMaxHeight());
        Registry<Biome> biomeRegistry = CraftRegistry.getMinecraftRegistry(Registries.BIOME);
        int blocksLit = 0, skyLit = 0;
        ClientboundLightUpdatePacketData lightData = copiedChunkPacket.getLightData();
        Long2ObjectMap<SectionLightCache> sectionLightCache = new Long2ObjectOpenHashMap<>();
        // These are section coords, iterating through every chunk section
        for (int y = minChunkY; y < maxChunkY; y++) {
            // The light data counts up from 0 instead of minChunkY, and has a buffer of 1 extra section above and below the world (hence + 1)
            int sectionIndex = y + Math.abs(minChunkY) + 1;
            boolean hasSkyLight = false, hasBlockLight = false;
            if (lightData.getBlockYMask().get(sectionIndex)) {
                hasBlockLight = true;
            }
            if (lightData.getSkyYMask().get(sectionIndex)) {
                hasSkyLight = true;
            }
            int blockCount = rawChunkData.readShort();
            PalettedContainer<BlockState> states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
            states.read(rawChunkData);
            PalettedContainer<Biome> biomes = new PalettedContainer<>(biomeRegistry, biomeRegistry.getValueOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
            biomes.read(rawChunkData);
            if (anyBlocksInSection(blocksInChunk, y)) {
                SectionPos sectionPos = SectionPos.of(chunkX, y, chunkZ);
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
                        BlockEntityType<?> blockEntityType = MATERIAL_BLOCK_ENTITY_TYPES.get(block.material.getMaterial());
                        if (blockEntityType != null) {
                            BlockEntity createdBlockEntity = blockEntityType.create(CraftLocation.toBlockPosition(block.location), newState);
                            createdBlockEntity.setLevel(((CraftWorld) world).getHandle());
                            Object packetBlockEntityData = CHUNKDATA_BLOCK_ENTITY_CREATE.invoke(createdBlockEntity);
                            blockEntities.add(packetBlockEntityData);
                        }
                        if (!oldState.isSolidRender() || (blockEntityType == null && newState.isSolidRender())) {
                            continue;
                        }
                        BlockLightData estimatedLights = getEstimatedLightLevel(relativeX, relativeY, relativeZ,
                                getLayer(hasBlockLight, lightData.getBlockUpdates(), blocksLit), getLayer(hasSkyLight, lightData.getSkyUpdates(), skyLit),
                                lightData, ((CraftWorld) world).getHandle(), sectionPos, sectionLightCache, sectionIndex, blocksLit, skyLit, fakeBlockMap);
                        if (estimatedLights.block() > 0) {
                            DataLayer blockLights;
                            if (!hasBlockLight) {
                                blockLights = addLayer(lightData.getBlockUpdates(), blocksLit, lightData.getBlockYMask(), lightData.getEmptyBlockYMask(), sectionIndex);
                                hasBlockLight = true;
                            }
                            else {
                                blockLights = new DataLayer(lightData.getBlockUpdates().get(blocksLit));
                            }
                            blockLights.set(relativeX, relativeY, relativeZ, estimatedLights.block());
                            block.lastBlockLight = estimatedLights.block();
                        }
                        if (estimatedLights.sky() > 0) {
                            DataLayer skyLights;
                            if (!hasSkyLight) {
                                skyLights = addLayer(lightData.getSkyUpdates(), skyLit, lightData.getSkyYMask(), lightData.getEmptySkyYMask(), sectionIndex);
                                hasSkyLight = true;
                            }
                            else {
                                skyLights = new DataLayer(lightData.getSkyUpdates().get(skyLit));
                            }
                            skyLights.set(relativeX, relativeY, relativeZ, estimatedLights.sky());
                            block.lastSkyLight = estimatedLights.sky();
                        }
                    }
                }
            }
            if (hasBlockLight) {
                blocksLit++;
            }
            if (hasSkyLight) {
                skyLit++;
            }
            newChunkData.writeShort(blockCount);
            states.write(newChunkData);
            biomes.write(newChunkData);
        }
        CHUNKDATA_BUFFER_SETTER.invokeExact(copiedChunkPacket.getChunkData(), newChunkData.array());
        return copiedChunkPacket;
    }

    public static DataLayer addLayer(List<byte[]> layers, int index, BitSet litSections, BitSet unlitSections, int sectionIndex) {
        DataLayer newLayer = new DataLayer();
        layers.add(index, newLayer.getData());
        litSections.set(sectionIndex);
        unlitSections.clear(sectionIndex);
        return newLayer;
    }

    public static DataLayer getLayer(boolean has, List<byte[]> layers, int index) {
        return has ? new DataLayer(layers.get(index)) : null;
    }

    public record BlockLightData(int sky, int block) {
        public static final BlockLightData MAX_BLOCK_LIGHT = new BlockLightData(0, 15);
        public static final BlockLightData MAX_SKY_LIGHT = new BlockLightData(15, 0);
    }

    public record SectionLightCache(DataLayer blockLights, DataLayer skyLights) {}

    public static final int[][] directions = {
            {0, -1, 0},
            {0, 1, 0},
            {-1, 0, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, 0, 1}
    };

    public static BlockLightData getEstimatedLightLevel(int relativeX, int relativeY, int relativeZ, DataLayer blockLights, DataLayer skyLights, ClientboundLightUpdatePacketData lightData, ServerLevel nmsWorld, SectionPos sectionPos, Long2ObjectMap<SectionLightCache> sectionLightsCache, int sectionIndex, int blocksLit, int skyLit, FakeBlock.FakeBlockMap fakeBlockMap) {
        boolean isSkyBright = nmsWorld.getSkyDarken() == 0;
        int maxSkyLight = getLight(skyLights, relativeX, relativeY, relativeZ);
        if (isSkyBright && maxSkyLight == 15) {
            return BlockLightData.MAX_SKY_LIGHT;
        }
        int maxBlockLight = getLight(blockLights, relativeX, relativeY, relativeZ);
        if (maxBlockLight == 15) {
            return BlockLightData.MAX_BLOCK_LIGHT;
        }
        List<BlockPos> blockLookups = null;
        for (int[] direction : directions) {
            int yOffest = direction[1];
            int neighborX = relativeX + direction[0];
            int neighborY = relativeY + yOffest;
            int neighborZ = relativeZ + direction[2];
            if (posOutOfSection(neighborX) || posOutOfSection(neighborZ)) {
                if (blockLookups == null) {
                    blockLookups = new ArrayList<>(2);
                }
                blockLookups.add(new BlockPos(sectionPos.minBlockX() + neighborX, sectionPos.minBlockY() + neighborY, sectionPos.minBlockZ() + neighborZ));
                continue;
            }
            int blockLight = -1, skyLight = -1;
            if (posOutOfSection(neighborY)) {
                int adjacentSectionIndex = sectionIndex + yOffest;
                boolean hasSkyLight = lightData.getSkyYMask().get(adjacentSectionIndex);
                boolean hasBlockLight = lightData.getBlockYMask().get(adjacentSectionIndex);
                if (!hasBlockLight && !hasSkyLight) {
                    continue;
                }
                int wrappedNeighborY = SectionPos.sectionRelative(neighborY);
                if (hasSkyLight) {
                    skyLight = new DataLayer(lightData.getSkyUpdates().get(skyLit + yOffest)).get(neighborX, wrappedNeighborY, neighborZ);
                    if (isSkyBright && skyLight == 15) {
                        return BlockLightData.MAX_SKY_LIGHT;
                    }
                }
                if (hasBlockLight) {
                    blockLight = new DataLayer(lightData.getBlockUpdates().get(blocksLit + yOffest)).get(neighborX, wrappedNeighborY, neighborZ);
                    if (blockLight == 15) {
                        return BlockLightData.MAX_BLOCK_LIGHT;
                    }
                }
            }
            else {
                skyLight = getLight(skyLights, neighborX, neighborY, neighborZ);
                if (isSkyBright && skyLight == 15) {
                    return BlockLightData.MAX_SKY_LIGHT;
                }
                blockLight = getLight(blockLights, neighborX, neighborY, neighborZ);
                if (blockLight == 15) {
                    return BlockLightData.MAX_BLOCK_LIGHT;
                }
            }
            if (skyLight > maxSkyLight) {
                maxSkyLight = skyLight;
            }
            if (blockLight > maxBlockLight) {
                maxBlockLight = blockLight;
            }
        }
        if (blockLookups == null) {
            return new BlockLightData(maxSkyLight, maxBlockLight);
        }
        for (BlockPos blockPos : blockLookups) {
            SectionPos containingSection = SectionPos.of(blockPos);
            SectionLightCache sectionLight = sectionLightsCache.computeIfAbsent(containingSection.asLong(), k -> {
                LevelLightEngine lightEngine = nmsWorld.getLightEngine();
                DataLayer sectionBlockLights = lightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(containingSection);
                DataLayer sectionSkyLights = lightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(containingSection);
                return new SectionLightCache(sectionBlockLights, sectionSkyLights);
            });
            int skyLight = getLight(sectionLight.skyLights(), blockPos);
            if (isSkyBright && skyLight == 15) {
                return BlockLightData.MAX_SKY_LIGHT;
            }
            int blockLight = getLight(sectionLight.blockLights(), blockPos);
            if (blockLight == 15) {
                return BlockLightData.MAX_BLOCK_LIGHT;
            }
            FakeBlock fakeBlock = fakeBlockMap.byLocation.get(new LocationTag(nmsWorld.getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            if (fakeBlock.lastSkyLight > skyLight) {
                skyLight = fakeBlock.lastSkyLight;
                if (isSkyBright && skyLight == 15) {
                    return BlockLightData.MAX_SKY_LIGHT;
                }
            }
            if (fakeBlock.lastBlockLight > blockLight) {
                blockLight = fakeBlock.lastBlockLight;
                if (blockLight == 15) {
                    return BlockLightData.MAX_BLOCK_LIGHT;
                }
            }
            if (skyLight > maxSkyLight) {
                maxSkyLight = skyLight;
            }
            if (blockLight > maxBlockLight) {
                maxBlockLight = blockLight;
            }
        }
        return new BlockLightData(maxSkyLight, maxBlockLight);
    }

    public static int getLight(DataLayer lightData, BlockPos blockPos) {
        return lightData != null ? lightData.get(SectionPos.sectionRelative(blockPos.getX()), SectionPos.sectionRelative(blockPos.getY()), SectionPos.sectionRelative(blockPos.getZ())) : -1;
    }

    public static int getLight(DataLayer lightData, int relativeX, int relativeY, int relativeZ) {
        return lightData != null ? lightData.get(relativeX, relativeY, relativeZ) : -1;
    }

    public static boolean posOutOfSection(int relativePos) {
        return relativePos < 0 || relativePos > 15;
    }
}

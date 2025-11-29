package com.denizenscript.denizen.nms.v1_21.impl.network.handlers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortObjectPair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R6.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R6.block.CraftBlockType;
import org.bukkit.craftbukkit.v1_21_R6.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R6.util.CraftLocation;

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

    public static final PalettedContainer<BlockState> EMPTY_BLOCKS_CONTAINER = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
    public static final Map<Material, BlockEntityType<?>> MATERIAL_BLOCK_ENTITY_TYPES = new HashMap<>();
    public static final BlockState MAX_LIGHT_LIGHT_BLOCK = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), AIR = Blocks.AIR.defaultBlockState();
    public static final byte[] MAX_LIGHT_SECTION = new DataLayer(15).getData(), MIN_LIGHT_SECTION = new DataLayer(0).getData();

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

    public static Packet<ClientGamePacketListener> handleMapChunkPacket(World world, ClientboundLevelChunkWithLightPacket originalChunkPacket, int chunkX, int chunkZ, Int2ObjectMap<List<FakeBlock>> blocksBySection, FakeBlock.FakeBlockMap fakeBlockMap) throws Throwable {
        ClientboundLevelChunkWithLightPacket copiedChunkPacket = DenizenNetworkManagerImpl.copyPacket(originalChunkPacket, ClientboundLevelChunkWithLightPacket.STREAM_CODEC);
        copyPacketPaperPatch(copiedChunkPacket);
        // TODO pass coord?
        boolean isNaturalLoad = !FakeBlock.scheduled.containsKey(new ChunkCoordinate(chunkX, chunkZ, world.getName()));
        // A list of ClientboundLevelChunkPacketData$BlockEntityInfo
        List<Object> blockEntities = (List<Object>) CHUNKDATA_BLOCK_ENTITIES.invokeExact(copiedChunkPacket.getChunkData());
        LocationTag location = new LocationTag(world, 0, 0, 0);
        ListIterator<Object> blockEntitiesIterator = blockEntities.listIterator();
        while (blockEntitiesIterator.hasNext()) {
            Object blockEnt = blockEntitiesIterator.next();
            int xz = (int) CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ.invoke(blockEnt);
            int y = (int) CHUNKDATA_BLOCKENTITYINFO_Y.invoke(blockEnt);
            int relativeX = SectionPos.sectionRelative(xz >> 4);
            int relativeZ = SectionPos.sectionRelative(xz);
            int x = SectionPos.sectionToBlockCoord(chunkX, relativeX);
            int z = SectionPos.sectionToBlockCoord(chunkZ, relativeZ);
            location.setX(x);
            location.setY(y);
            location.setZ(z);
            if (fakeBlockMap.byLocation.containsKey(location)) {
                blockEntitiesIterator.remove();
            }
        }
        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>(blocksBySection.size() + 1);
        packets.add(copiedChunkPacket);

        // Get the original chunk data to read, and a new buf of the same size to write
        FriendlyByteBuf rawChunkData = originalChunkPacket.getChunkData().getReadBuffer();
        FriendlyByteBuf newChunkData = new FriendlyByteBuf(Unpooled.buffer(rawChunkData.readableBytes()));
        final int minChunkY = SectionPos.blockToSectionCoord(world.getMinHeight());
        final int maxChunkY = SectionPos.blockToSectionCoord(world.getMaxHeight());
        Registry<Biome> biomeRegistry = CraftRegistry.getMinecraftRegistry(Registries.BIOME);
        ClientboundLightUpdatePacketData lightData = copiedChunkPacket.getLightData();
        BitSet blockLightMask = lightData.getBlockYMask(), blockNoLightMask = lightData.getEmptyBlockYMask(), skyLightMask = lightData.getSkyYMask(), skyNoLightMask = lightData.getEmptySkyYMask();
        int blockLitSections = 0, skyLitSections = 0;
        boolean foundFirstSkyLight = false;
        for (int chunkY = minChunkY; chunkY < maxChunkY; chunkY++) {
            int blockCount = rawChunkData.readShort();
            PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
            states.read(rawChunkData);
            PalettedContainer<Holder<Biome>> biomes = new PalettedContainer<>(biomeRegistry.getOrThrow(Biomes.PLAINS), Strategy.createForBiomes(biomeRegistry.asHolderIdMap()));
            biomes.read(rawChunkData);
            List<FakeBlock> fakeBlocksInSection = blocksBySection.get(chunkY);
            // The light data counts up from 0 instead of minChunkY, and has a buffer of 1 extra section above and below the world (hence + 1)
            int sectionIndex = chunkY - minChunkY + 1;
            boolean hasBlockLight = false, hasSkyLight = false;
            if (blockLightMask.get(sectionIndex)) {
                hasBlockLight = true;
            }
            if (skyLightMask.get(sectionIndex)) {
                hasSkyLight = true;
            }
            if (fakeBlocksInSection != null) {
                List<ShortObjectPair<BlockState>> lightPatch = new ArrayList<>();
                DataLayer blockLayer = null, skyLayer = null;
                Debug.log("--------------");
                Debug.log("Has block light: " + hasBlockLight);
                Debug.log("Has sky light: " + hasSkyLight);
                if (hasSkyLight) {
                    skyLightMask.clear(sectionIndex);
                    skyLayer = new DataLayer(lightData.getSkyUpdates().remove(skyLitSections));
                    hasSkyLight = false;
                    if (!foundFirstSkyLight) {
                        Debug.log(">>> Found first sky light");
                    }
                    foundFirstSkyLight = true;
                }
                else {
                    skyNoLightMask.clear(sectionIndex);
                }
                if (isNaturalLoad) {
                    Debug.log(">>> Adding " + (foundFirstSkyLight ? "bright" : "dark") + " sky light");
                    lightData.getSkyUpdates().add(skyLitSections, foundFirstSkyLight ? MAX_LIGHT_SECTION : MIN_LIGHT_SECTION);
                    skyLightMask.set(sectionIndex);
                    hasSkyLight = true;
                    skyLayer = new DataLayer(lightData.getSkyUpdates().get(skyLitSections));
                }
                if (hasBlockLight) {
                    blockLightMask.clear(sectionIndex);
                    blockLayer = new DataLayer(lightData.getBlockUpdates().remove(blockLitSections));
                    hasBlockLight = false;
                }
                else {
                    blockNoLightMask.clear(sectionIndex);
                }
                Debug.log("Post has block light: " + blockLightMask.get(sectionIndex));
                Debug.log("Post has sky light: " + skyLightMask.get(sectionIndex));
                Debug.log("--------------");
                if (blockLayer != null || skyLayer != null) {
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = states.get(x, y, z);
                                boolean isAir = state.isAir();
                                if (isAir && (y == 0 || states.get(x, y - 1, z).isAir())) {
                                    continue;
                                }
                                if (!isAir && (blockLayer == null || blockLayer.get(x, y, z) == 0)) {
                                    continue;
                                }
                                // Based on SectionPos#sectionRelativePos
                                short offset = (short) (x << 8 | z << 4 | y << 0);
                                lightPatch.add(ShortObjectPair.of(offset, isAir ? MAX_LIGHT_LIGHT_BLOCK : AIR));
                                lightPatch.add(ShortObjectPair.of(offset, state));
                                if (!isAir) {
                                    blockCount--;
                                    states.set(x, y, z, AIR);
                                }
                            }
                        }
                    }
                }
                int size = fakeBlocksInSection.size();
                short[] offsets = new short[size];
                BlockState[] statesArr = new BlockState[size];
                for (int blockIndex = 0; blockIndex < size; blockIndex++) {
                    FakeBlock fakeBlock = fakeBlocksInSection.get(blockIndex);
                    int relativeX = SectionPos.sectionRelative(fakeBlock.location.getBlockX());
                    int relativeY = SectionPos.sectionRelative(fakeBlock.location.getBlockY());
                    int relativeZ = SectionPos.sectionRelative(fakeBlock.location.getBlockZ());
                    BlockState oldState = states.get(relativeX, relativeY, relativeZ);
                    BlockState newState = ((CraftBlockData) fakeBlock.material.getModernData()).getState();
                    short sectionRelativePos = SectionPos.sectionRelativePos(CraftLocation.toBlockPosition(fakeBlock.location));
                    offsets[blockIndex] = sectionRelativePos;
                    statesArr[blockIndex] = newState;
                    int maxLight = Math.max(
                            skyLayer != null ? skyLayer.get(relativeX, relativeY, relativeZ) : 0,
                            blockLayer != null ? blockLayer.get(relativeX, relativeY, relativeZ) : 0
                    );
                    if (!oldState.isAir()) {
                        blockCount--;
                        states.set(relativeX, relativeY, relativeZ, Blocks.AIR.defaultBlockState());
                        if (maxLight <= 0 && !newState.isSolidRender()) {
                            lightPatch.add(ShortObjectPair.of(sectionRelativePos, MAX_LIGHT_LIGHT_BLOCK));
                        }
                    }
                }
                SectionPos sectionPos = SectionPos.of(chunkX, chunkY, chunkZ);
                if (!lightPatch.isEmpty()) {
                    int lightCount = lightPatch.size();
                    short[] lightOffsets = new short[lightCount];
                    BlockState[] lightStates = new BlockState[lightCount];
                    for (int i = 0; i < lightCount; i++) {
                        ShortObjectPair<BlockState> pair = lightPatch.get(i);
                        lightOffsets[i] = pair.leftShort();
                        lightStates[i] = pair.right();
                    }
                    packets.add(new ClientboundSectionBlocksUpdatePacket(sectionPos, new ShortArraySet(lightOffsets), lightStates));
                }
                packets.add(new ClientboundSectionBlocksUpdatePacket(sectionPos, new ShortArraySet(offsets), statesArr));
            }
            if (hasBlockLight) {
                blockLitSections++;
            }
            if (hasSkyLight) {
                skyLitSections++;
            }
            newChunkData.writeShort(blockCount);
            if (blockCount > 0) {
                states.write(newChunkData);
            }
            else {
                EMPTY_BLOCKS_CONTAINER.write(newChunkData);
            }
            biomes.write(newChunkData);
        }
        CHUNKDATA_BUFFER_SETTER.invokeExact(copiedChunkPacket.getChunkData(), newChunkData.array());
        return new ClientboundBundlePacket(packets);
    }
}

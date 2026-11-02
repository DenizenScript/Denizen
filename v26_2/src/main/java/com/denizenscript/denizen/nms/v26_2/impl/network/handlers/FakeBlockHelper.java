package com.denizenscript.denizen.nms.v26_2.impl.network.handlers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.v26_2.Handler;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

public class FakeBlockHelper {

    public static final MethodHandle CHUNKDATA_BLOCK_ENTITIES = ReflectionHelper.getFields(ClientboundLevelChunkPacketData.class).getGetter("blockEntitiesData", List.class);
    public static final MethodHandle CHUNKDATA_BUFFER_SETTER = ReflectionHelper.getFields(ClientboundLevelChunkPacketData.class).getSetter("buffer", byte[].class);
    public static final Class<?> CHUNKDATA_BLOCKENTITYINFO_CLASS = ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0];
    public static final MethodHandle CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).getGetter("packedXZ");
    public static final MethodHandle CHUNKDATA_BLOCKENTITYINFO_Y = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).getGetter("y");

    public static final PalettedContainer<BlockState> EMPTY_BLOCKS_CONTAINER = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
    public static final BlockState MAX_LIGHT_BLOCK = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), AIR = Blocks.AIR.defaultBlockState();
    public static final byte[] MAX_LIGHT_SECTION = new DataLayer(15).getData(), MIN_LIGHT_SECTION = new DataLayer(0).getData();

    public static BlockState getNMSState(FakeBlock block) {
        return ((CraftBlockData) block.material.getModernData()).getState();
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

    public static boolean hasLight(DataLayer lightData) {
        if (lightData == null || lightData.isEmpty()) {
            return false;
        }
        // Sometimes Mojang sends "0" for every block instead of an empty layer??
        // Reproduction: fully underground chunk section, place a single torch & fake block, reload chunk
        for (byte value : lightData.getData()) {
            if (value != 0) {
                return true;
            }
        }
        return false;
    }

    public static Packet<ClientGamePacketListener> handleMapChunkPacket(World world, ClientboundLevelChunkWithLightPacket originalChunkPacket, int chunkX, int chunkZ, Int2ObjectMap<List<FakeBlock>> blocksBySection, FakeBlock.FakeBlockMap fakeBlockMap) throws Throwable {
        ClientboundLevelChunkWithLightPacket copiedChunkPacket = DenizenNetworkManagerImpl.copyPacket(originalChunkPacket, ClientboundLevelChunkWithLightPacket.STREAM_CODEC);
        copyPacketPaperPatch(copiedChunkPacket);
        // TODO pass coord?
        boolean isNaturalLoad = !FakeBlock.scheduled.containsKey(new ChunkCoordinate(chunkX, chunkZ, world.getName()));
        // A list of ClientboundLevelChunkPacketData$BlockEntityInfo, every block entity in the chunk
        List<Object> blockEntities = (List<Object>) CHUNKDATA_BLOCK_ENTITIES.invokeExact(copiedChunkPacket.getChunkData());
        LocationTag location = new LocationTag(world, 0, 0, 0);
        ListIterator<Object> blockEntitiesIterator = blockEntities.listIterator();
        // Clear every block entity being replaced by a fake block
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
        // This list will contain at least the (modified) chunk packet, and one "patch" packet for every chunk section
        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>(blocksBySection.size() + 1);
        packets.add(copiedChunkPacket);

        // Get the original chunk data to read, and a new buffer of the same size to write
        FriendlyByteBuf rawChunkData = originalChunkPacket.getChunkData().getReadBuffer();
        FriendlyByteBuf newChunkData = new FriendlyByteBuf(Unpooled.buffer(rawChunkData.readableBytes()));
        final int minChunkY = SectionPos.blockToSectionCoord(world.getMinHeight());
        final int maxChunkY = SectionPos.blockToSectionCoord(world.getMaxHeight());
        Registry<Biome> biomeRegistry = CraftRegistry.getMinecraftRegistry(Registries.BIOME);
        ClientboundLightUpdatePacketData lightData = copiedChunkPacket.getLightData();
        BitSet blockLightMask = lightData.getBlockYMask(), blockNoLightMask = lightData.getEmptyBlockYMask(), skyLightMask = lightData.getSkyYMask(), skyNoLightMask = lightData.getEmptySkyYMask();
        // The list of light data per section only has entries for sections with lighting, need to know how many sections had lighting to know what index to remove
        int blockLitSections = 0, skyLitSections = 0;
        boolean hadFirstSkyLight = false;
        for (int chunkY = minChunkY; chunkY < maxChunkY; chunkY++) {
            int blockCount = rawChunkData.readShort();
            int fluidCount = rawChunkData.readShort();
            PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
            states.read(rawChunkData);
            PalettedContainer<Holder<Biome>> biomes = new PalettedContainer<>(biomeRegistry.getOrThrow(Biomes.PLAINS), Strategy.createForBiomes(biomeRegistry.asHolderIdMap()));
            biomes.read(rawChunkData);
            List<FakeBlock> fakeBlocksInSection = blocksBySection.get(chunkY);
            // The light data counts up from 0 instead of minChunkY, and has a buffer of 1 extra section above and below the world (hence + 1)
            int lightingIndex = chunkY - minChunkY + 1;
            boolean hasBlockLight = blockLightMask.get(lightingIndex), hasSkyLight = skyLightMask.get(lightingIndex);
            boolean hasEffectiveSkyLight = false;
            if (fakeBlocksInSection != null) {
                List<ShortObjectPair<BlockState>> lightPatch = new ArrayList<>();
                DataLayer blockLayer = null, skyLayer = null;
                Debug.log("--------------");
                Debug.log("Has block light: " + hasBlockLight);
                Debug.log("Has sky light: " + hasSkyLight);
                // Chunk packets schedule light updates for the next render tick, delete all lighting data for these sections so that they don't get scheduled and override the lighting from the fake block patches
                // Clears both the "has light" mask and the "has no light" mask to make this section effectively non-existent
                if (hasSkyLight) {
                    skyLightMask.clear(lightingIndex);
                    skyLayer = new DataLayer(lightData.getSkyUpdates().remove(skyLitSections));
                    hasSkyLight = false;
                    if (!hadFirstSkyLight) {
                        Debug.log(">>> Found first sky light");
                    }
                    hasEffectiveSkyLight = hasLight(skyLayer);
                    if (hasEffectiveSkyLight) {
                        hadFirstSkyLight = true;
                    }
                }
                else {
                    skyNoLightMask.clear(lightingIndex);
                }
                if (hasBlockLight) {
                    blockLightMask.clear(lightingIndex);
                    blockLayer = new DataLayer(lightData.getBlockUpdates().remove(blockLitSections));
                    hasBlockLight = false;
                }
                else {
                    blockNoLightMask.clear(lightingIndex);
                }
                // On natural loads the client is receiving the chunk for the first time and has no lighting data for the world around it, making generating lighting for the fake block patches impossible
                // Provide best-effort dark/bright lights on first load based on whether we've reached the first sky lit section
                if (isNaturalLoad) {
                    Debug.log(">>> Adding " + (hadFirstSkyLight ? "bright" : "dark") + " sky light");
                    byte[] newLightData = hadFirstSkyLight ? MAX_LIGHT_SECTION : MIN_LIGHT_SECTION;
                    lightData.getSkyUpdates().add(skyLitSections, newLightData);
                    skyLightMask.set(lightingIndex);
                    hasSkyLight = true;
                    hasEffectiveSkyLight = hadFirstSkyLight;
                    skyLayer = new DataLayer(newLightData);
                }
                Debug.log("Post has block light: " + blockLightMask.get(lightingIndex));
                Debug.log("Post has sky light: " + skyLightMask.get(lightingIndex));
                Debug.log("--------------");
                // We're removing lighting data to allow lighting from the fake block patches, but that means we need to make the client update lighting for the real blocks in the section too (where needed)
                if (hasLight(blockLayer) || hasEffectiveSkyLight) {
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = states.get(x, y, z);
                                boolean isAir = state.isAir();
                                // Air blocks only need an update if they have non-air blocks below them
                                if (isAir && (y == 0 || states.get(x, y - 1, z).isAir())) {
                                    continue;
                                }
                                // Solid blocks only need an update if they had any sort of lighting in the first place
                                if (!isAir && (!hasLight(blockLayer) || blockLayer.get(x, y, z) == 0)) {
                                    continue;
                                }
                                // Based on SectionPos#sectionRelativePos
                                short offset = (short) (x << 8 | z << 4 | y << 0);
                                // "Toggle" the block to update it
                                lightPatch.add(ShortObjectPair.of(offset, isAir ? MAX_LIGHT_BLOCK : AIR));
                                lightPatch.add(ShortObjectPair.of(offset, state));
                                if (!isAir) {
                                    if (state.getFluidState().isEmpty()) {
                                        blockCount--;
                                    }
                                    else {
                                        fluidCount--;
                                    }
                                    // No need to send the block in the chunk packet as it is sent in the lighting patch
                                    states.set(x, y, z, AIR);
                                }
                            }
                        }
                    }
                }
                int fakesCount = fakeBlocksInSection.size();
                short[] offsets = new short[fakesCount];
                BlockState[] statesArr = new BlockState[fakesCount];
                for (int blockIndex = 0; blockIndex < fakesCount; blockIndex++) {
                    FakeBlock fakeBlock = fakeBlocksInSection.get(blockIndex);
                    int relativeX = SectionPos.sectionRelative(fakeBlock.location.getBlockX());
                    int relativeY = SectionPos.sectionRelative(fakeBlock.location.getBlockY());
                    int relativeZ = SectionPos.sectionRelative(fakeBlock.location.getBlockZ());
                    BlockState oldState = states.get(relativeX, relativeY, relativeZ);
                    BlockState newState = ((CraftBlockData) fakeBlock.material.getModernData()).getState();
                    short sectionRelativePos = SectionPos.sectionRelativePos(Handler.toBlockPos(fakeBlock.location));
                    offsets[blockIndex] = sectionRelativePos;
                    statesArr[blockIndex] = newState;
//                    int maxLight = Math.max(
//                            skyLayer != null ? skyLayer.get(relativeX, relativeY, relativeZ) : 0,
//                            blockLayer != null ? blockLayer.get(relativeX, relativeY, relativeZ) : 0
//                    );
                    if (!oldState.isAir()) {
                        if (oldState.getFluidState().isEmpty()) {
                            blockCount--;
                        }
                        else {
                            fluidCount--;
                        }
                        states.set(relativeX, relativeY, relativeZ, AIR);
//                        if (maxLight <= 0 && !newState.isSolidRender()) {
//                            lightPatch.add(ShortObjectPair.of(sectionRelativePos, MAX_LIGHT_BLOCK));
//                        }
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
            // Now that we have the final decision on the section having light or not, update the counters
            if (hasBlockLight) {
                blockLitSections++;
            }
            if (hasSkyLight) {
                skyLitSections++;
            }
            newChunkData.writeShort(blockCount);
            newChunkData.writeShort(fluidCount);
            // A section that has been cleared out seemingly carries more data than a section that never had anything (SimpleBitStorage vs ZeroBitStorage)
            if (blockCount > 0 || fluidCount > 0) {
                states.write(newChunkData);
            }
            else {
                EMPTY_BLOCKS_CONTAINER.write(newChunkData);
            }
            biomes.write(newChunkData);
        }
        // The instance is already in the list, just update the underlying data
        CHUNKDATA_BUFFER_SETTER.invokeExact(copiedChunkPacket.getChunkData(), newChunkData.array());
        return new ClientboundBundlePacket(packets);
    }
}

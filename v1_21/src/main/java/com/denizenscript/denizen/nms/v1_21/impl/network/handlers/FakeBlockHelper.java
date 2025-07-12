package com.denizenscript.denizen.nms.v1_21.impl.network.handlers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R7.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.block.CraftBlockType;
import org.bukkit.craftbukkit.v1_21_R7.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R7.util.CraftLocation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class FakeBlockHelper {

    public static Field CHUNKDATA_BLOCK_ENTITIES = ReflectionHelper.getFields(ClientboundLevelChunkPacketData.class).getFirstOfType(List.class);
    public static Class CHUNKDATA_BLOCKENTITYINFO_CLASS = ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0];
    public static MethodHandle CHUNKDATA_BLOCK_ENTITY_CONSTRUCTOR = ReflectionHelper.getConstructor(ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0], int.class, int.class, BlockEntityType.class, CompoundTag.class);
    public static final MethodHandle CHUNK_DATA_BLOCK_ENTITY_CREATE = ReflectionHelper.getMethodHandle(CHUNKDATA_BLOCKENTITYINFO_CLASS, ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_create_method, BlockEntity.class);
    public static MethodHandle CHUNKDATA_BUFFER_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(ClientboundLevelChunkPacketData.class, byte[].class);
    public static Field CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).get(ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_packedXZ);
    public static Field CHUNKDATA_BLOCKENTITYINFO_Y = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).get(ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_y);
    public static MethodHandle CHUNKPACKET_CHUNKDATA_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(ClientboundLevelChunkWithLightPacket.class, ClientboundLevelChunkPacketData.class);
    public static Constructor<?> PALETTEDCONTAINER_CTOR = Arrays.stream(PalettedContainer.class.getConstructors()).filter(c -> c.getParameterCount() == 3).findFirst().get();
    public static Field BLOCK_ENTITY_TYPE_VALID_BLOCKS = ReflectionHelper.getFields(BlockEntityType.class).get(ReflectionMappingsInfo.BlockEntityType_validBlocks, Set.class);

    public static final Map<Material, BlockEntityType<?>> MATERIAL_BLOCK_ENTITY_TYPES = new EnumMap<>(Material.class);

    static {
        try {
            for (BlockEntityType<?> nmsBlockEntityType : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
                Set<Block> validBlocks = (Set<Block>) BLOCK_ENTITY_TYPE_VALID_BLOCKS.get(nmsBlockEntityType);
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

    public static ClientboundLevelChunkWithLightPacket handleMapChunkPacket(World world, ClientboundLevelChunkWithLightPacket originalChunkPacket, int chunkX, int chunkZ, List<FakeBlock> blocksInChunk, FakeBlock.FakeBlockMap fakeBlockMap) {
        try {
            ClientboundLevelChunkWithLightPacket copiedChunkPacket = DenizenNetworkManagerImpl.copyPacket(originalChunkPacket, ClientboundLevelChunkWithLightPacket.STREAM_CODEC);
            copyPacketPaperPatch(copiedChunkPacket);
            // A list of block entities sent with the chunk data
            List<Object> blockEntities = (List<Object>) CHUNKDATA_BLOCK_ENTITIES.get(copiedChunkPacket.getChunkData());
            LocationTag location = new LocationTag(world, 0, 0, 0);
            ListIterator<Object> blockEntitiesIterator = blockEntities.listIterator();
            while (blockEntitiesIterator.hasNext()) {
                Object blockEnt = blockEntitiesIterator.next();
                int xz = CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ.getInt(blockEnt);
                int y = CHUNKDATA_BLOCKENTITYINFO_Y.getInt(blockEnt);
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
            // These are section coords, iterating through every chunk section
            for (int y = minChunkY; y < maxChunkY; y++) {
                int blockCount = rawChunkData.readShort();
                PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
                states.read(rawChunkData);
                PalettedContainer<Biome> biomes = new PalettedContainer<>(biomeRegistry.getValueOrThrow(Biomes.PLAINS), Strategy.createForBiomes(biomeRegistry));
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
                            Object packetBlockEntityData = CHUNK_DATA_BLOCK_ENTITY_CREATE.invoke(createdBlockEntity);
                            blockEntities.add(packetBlockEntityData);
                        }
                    }
                }
                newChunkData.writeShort(blockCount);
                states.write(newChunkData);
                biomes.write(newChunkData);
            }
            CHUNKDATA_BUFFER_SETTER.invoke(copiedChunkPacket.getChunkData(), newChunkData.array());
            return copiedChunkPacket;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return null;
    }
}

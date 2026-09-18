package com.denizenscript.denizen.nms.v26_3.impl.network.handlers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockStates;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class FakeBlockHelper {

    public static Field CHUNKDATA_BLOCK_ENTITIES = ReflectionHelper.getFields(ClientboundLevelChunkPacketData.class).getFirstOfType(List.class);
    public static MethodHandle CHUNKDATA_CONSTRUCTOR = ReflectionHelper.getConstructor(ClientboundLevelChunkPacketData.class, Map.class, byte[].class, List.class);
    public static MethodHandle CHUNKDATA_BLOCK_ENTITY_CONSTRUCTOR = ReflectionHelper.getConstructor(ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0], byte.class, short.class, BlockEntityType.class, Optional.class);
    public static Class CHUNKDATA_BLOCKENTITYINFO_CLASS = ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0];
    public static Field CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).get("packedXZ");
    public static Field CHUNKDATA_BLOCKENTITYINFO_Y = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).get("y");
    public static Constructor<?> PALETTEDCONTAINER_CTOR = Arrays.stream(PalettedContainer.class.getConstructors()).filter(c -> c.getParameterCount() == 2).findFirst().get();

    public static BlockState getNMSState(FakeBlock block) {
        return ((CraftBlockData) block.material.getModernData()).getState();
    }

    public static boolean anyBlocksInSection(List<FakeBlock> blocks, int y) {
        int minY = y << 4;
        int maxY = (y << 4) + 16;
        for (FakeBlock block : blocks) {
            int blockY = block.location.getBlockY();
            if (blockY >= minY && blockY < maxY) {
                return true;
            }
        }
        return false;
    }

    public static Field PAPER_CHUNK_READY;
    public static boolean tryPaperPatch = true;

    public static void copyPacketPaperPatch(ClientboundLevelChunkWithLightPacket newPacket, ClientboundLevelChunkWithLightPacket oldPacket) {
        if (!Denizen.supportsPaper || !tryPaperPatch) {
            return;
        }
        try {
            if (PAPER_CHUNK_READY == null) {
                PAPER_CHUNK_READY = ClientboundLevelChunkWithLightPacket.class.getDeclaredField("ready");
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

    public static ClientboundLevelChunkWithLightPacket handleMapChunkPacket(World world, ClientboundLevelChunkWithLightPacket originalPacket, int chunkX, int chunkZ, List<FakeBlock> blocks) {
        try {
            FriendlyByteBuf serial = originalPacket.chunkData().getReadBuffer();
            FriendlyByteBuf outputSerial = new FriendlyByteBuf(Unpooled.buffer(serial.readableBytes()));
            List blockEntities = new ArrayList((List) CHUNKDATA_BLOCK_ENTITIES.get(originalPacket.chunkData()));
            for (int i = 0; i < blockEntities.size(); i++) {
                Object blockEnt = blockEntities.get(i);
                byte xz = CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ.getByte(blockEnt);
                int y = CHUNKDATA_BLOCKENTITYINFO_Y.getInt(blockEnt);
                int x = (chunkX << 4) + ((xz >> 4) & 15);
                int z = (chunkZ << 4) + (xz & 15);
                for (FakeBlock block : blocks) {
                    LocationTag loc = block.location;
                    if (loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z && block.material != null) {
                        BlockEntity newBlockEnt = CraftBlockStates.createNewTileEntity(block.material.getMaterial());
                        Object newData = CHUNKDATA_BLOCK_ENTITY_CONSTRUCTOR.invoke(xz, y, newBlockEnt.getType(), Optional.ofNullable(newBlockEnt).map(blockEntity -> blockEntity.getUpdateTag(CraftRegistry.getMinecraftRegistry())));
                        blockEntities.set(i, newData);
                        break;
                    }
                }
            }
            int worldMinY = world.getMinHeight();
            int worldMaxY = world.getMaxHeight();
            int minChunkY = worldMinY >> 4;
            int maxChunkY = worldMaxY >> 4;
            Registry<Biome> biomeRegistry = ((CraftWorld) world).getHandle().registryAccess().lookupOrThrow(Registries.BIOME);
            for (int y = minChunkY; y < maxChunkY; y++) {
                int blockCount = serial.readShort();
                int fluidCount = serial.readShort();
                // reflected constructors as workaround for spigot remapper bug - Mojang "IdMap" became Spigot "IRegistry" but should be "Registry"
                PalettedContainer<BlockState> states = (PalettedContainer<BlockState>) PALETTEDCONTAINER_CTOR.newInstance(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY));
                states.read(serial);
                PalettedContainer<Biome> biomes = (PalettedContainer<Biome>) PALETTEDCONTAINER_CTOR.newInstance(biomeRegistry.getOrThrow(Biomes.PLAINS), Strategy.createForBiomes(biomeRegistry));
                biomes.read(serial);
                if (anyBlocksInSection(blocks, y)) {
                    int minY = y << 4;
                    int maxY = (y << 4) + 16;
                    for (FakeBlock block : blocks) {
                        int blockY = block.location.getBlockY();
                        if (blockY >= minY && blockY < maxY && block.material != null) {
                            int blockX = block.location.getBlockX();
                            int blockZ = block.location.getBlockZ();
                            blockX -= (blockX >> 4) * 16;
                            blockY -= (blockY >> 4) * 16;
                            blockZ -= (blockZ >> 4) * 16;
                            BlockState oldState = states.get(blockX, blockY, blockZ);
                            BlockState newState = getNMSState(block);
                            if (oldState.isAir() && !newState.isAir()) {
                                blockCount++;
                            }
                            else if (newState.isAir() && !oldState.isAir()) {
                                blockCount--;
                            }
                            states.set(blockX, blockY, blockZ, newState);
                        }
                    }
                }
                outputSerial.writeShort(blockCount);
                outputSerial.writeShort(fluidCount);
                states.write(outputSerial);
                biomes.write(outputSerial);
            }
            byte[] outputBytes = outputSerial.array();
            ClientboundLevelChunkPacketData modifiedChunkData = (ClientboundLevelChunkPacketData) CHUNKDATA_CONSTRUCTOR.invokeExact(originalPacket.chunkData().getHeightmaps(), outputBytes, blockEntities);
            ClientboundLevelChunkWithLightPacket duplicateCorePacket = new ClientboundLevelChunkWithLightPacket(chunkX, chunkZ, modifiedChunkData, originalPacket.lightData());
            copyPacketPaperPatch(duplicateCorePacket, originalPacket);
            return duplicateCorePacket;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return null;
    }
}

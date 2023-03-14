package com.denizenscript.denizen.nms.v1_19.impl.network.handlers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class FakeBlockHelper {

    public static Field CHUNKDATA_BLOCK_ENTITIES = ReflectionHelper.getFields(ClientboundLevelChunkPacketData.class).getFirstOfType(List.class);
    public static MethodHandle CHUNKDATA_BUFFER_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(ClientboundLevelChunkPacketData.class, byte[].class);
    public static Class CHUNKDATA_BLOCKENTITYINFO_CLASS = ClientboundLevelChunkPacketData.class.getDeclaredClasses()[0];
    public static Field CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).get(ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_packedXZ);
    public static Field CHUNKDATA_BLOCKENTITYINFO_Y = ReflectionHelper.getFields(CHUNKDATA_BLOCKENTITYINFO_CLASS).get(ReflectionMappingsInfo.ClientboundLevelChunkPacketDataBlockEntityInfo_y);
    public static MethodHandle CHUNKPACKET_CHUNKDATA_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(ClientboundLevelChunkWithLightPacket.class, ClientboundLevelChunkPacketData.class);
    public static Constructor<?> PALETTEDCONTAINER_CTOR = Arrays.stream(PalettedContainer.class.getConstructors()).filter(c -> c.getParameterCount() == 3).findFirst().get();

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

    public static ClientboundLevelChunkWithLightPacket handleMapChunkPacket(World world, ClientboundLevelChunkWithLightPacket originalPacket, int chunkX, int chunkZ, List<FakeBlock> blocks) {
        try {
            ClientboundLevelChunkWithLightPacket duplicateCorePacket = new ClientboundLevelChunkWithLightPacket(DenizenNetworkManagerImpl.copyPacket(originalPacket));
            copyPacketPaperPatch(duplicateCorePacket, originalPacket);
            FriendlyByteBuf copier = new FriendlyByteBuf(Unpooled.buffer());
            originalPacket.getChunkData().write(copier);
            ClientboundLevelChunkPacketData packet = new ClientboundLevelChunkPacketData(copier, chunkX, chunkZ);
            FriendlyByteBuf serial = originalPacket.getChunkData().getReadBuffer();
            FriendlyByteBuf outputSerial = new FriendlyByteBuf(Unpooled.buffer(serial.readableBytes()));
            List blockEntities = new ArrayList((List) CHUNKDATA_BLOCK_ENTITIES.get(originalPacket.getChunkData()));
            CHUNKDATA_BLOCK_ENTITIES.set(packet, blockEntities);
            ListIterator iterator = blockEntities.listIterator();
            while (iterator.hasNext()) {
                Object blockEnt = iterator.next();
                int xz = CHUNKDATA_BLOCKENTITYINFO_PACKEDXZ.getInt(blockEnt);
                int y = CHUNKDATA_BLOCKENTITYINFO_Y.getInt(blockEnt);
                int x = (chunkX << 4) + ((xz >> 4) & 15);
                int z = (chunkZ << 4) + (xz & 15);
                for (FakeBlock block : blocks) {
                    LocationTag loc = block.location;
                    if (loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z && block.material != null) {
                        iterator.remove();
                        break;
                    }
                }
            }
            int worldMinY = world.getMinHeight();
            int worldMaxY = world.getMaxHeight();
            int minChunkY = worldMinY >> 4;
            int maxChunkY = worldMaxY >> 4;
            Registry<Biome> biomeRegistry = ((CraftWorld) world).getHandle().registryAccess().registryOrThrow(Registries.BIOME);
            for (int y = minChunkY; y < maxChunkY; y++) {
                int blockCount = serial.readShort();
                // reflected constructors as workaround for spigot remapper bug - Mojang "IdMap" became Spigot "IRegistry" but should be "Registry"
                PalettedContainer<BlockState> states = (PalettedContainer<BlockState>) PALETTEDCONTAINER_CTOR.newInstance(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                states.read(serial);
                PalettedContainer<Biome> biomes = (PalettedContainer<Biome>) PALETTEDCONTAINER_CTOR.newInstance(biomeRegistry, biomeRegistry.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
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
                states.write(outputSerial);
                biomes.write(outputSerial);
            }
            byte[] outputBytes = outputSerial.array();
            CHUNKDATA_BUFFER_SETTER.invoke(packet, outputBytes);
            CHUNKPACKET_CHUNKDATA_SETTER.invoke(duplicateCorePacket, packet);
            return duplicateCorePacket;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return null;
    }
}

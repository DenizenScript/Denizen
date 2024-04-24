package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.FakeBlockHelper;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FakeBlocksPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundLevelChunkWithLightPacket.class, FakeBlocksPacketHandlers::processShowFakeForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSectionBlocksUpdatePacket.class, FakeBlocksPacketHandlers::processShowFakeForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundBlockUpdatePacket.class, FakeBlocksPacketHandlers::processShowFakeForPacket);
    }

    public static Field SECTIONPOS_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_sectionPos, SectionPos.class);
    public static Field OFFSETARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_positions, short[].class);
    public static Field BLOCKARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_states, BlockState[].class);

    public static Packet<ClientGamePacketListener> processShowFakeForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (FakeBlock.blocks.isEmpty()) {
            return packet;
        }
        try {
            if (packet instanceof ClientboundLevelChunkWithLightPacket) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(networkManager.player.getUUID());
                if (map == null) {
                    return packet;
                }
                int chunkX = ((ClientboundLevelChunkWithLightPacket) packet).getX();
                int chunkZ = ((ClientboundLevelChunkWithLightPacket) packet).getZ();
                ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, networkManager.player.level().getWorld().getName());
                List<FakeBlock> blocks = FakeBlock.getFakeBlocksFor(networkManager.player.getUUID(), chunkCoord);
                if (blocks == null || blocks.isEmpty()) {
                    return packet;
                }
                ClientboundLevelChunkWithLightPacket newPacket = FakeBlockHelper.handleMapChunkPacket(networkManager.player.getBukkitEntity().getWorld(), (ClientboundLevelChunkWithLightPacket) packet, chunkX, chunkZ, blocks);
                return newPacket;
            }
            else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
                FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(networkManager.player.getUUID());
                if (map == null) {
                    return packet;
                }
                SectionPos coord = (SectionPos) SECTIONPOS_MULTIBLOCKCHANGE.get(packet);
                ChunkCoordinate coordinateDenizen = new ChunkCoordinate(coord.getX(), coord.getZ(), networkManager.player.level().getWorld().getName());
                if (!map.byChunk.containsKey(coordinateDenizen)) {
                    return packet;
                }
                ClientboundSectionBlocksUpdatePacket newPacket = ClientboundSectionBlocksUpdatePacket.STREAM_CODEC.decode(DenizenNetworkManagerImpl.copyPacket((ClientboundSectionBlocksUpdatePacket) packet, ClientboundSectionBlocksUpdatePacket.STREAM_CODEC));
                LocationTag location = new LocationTag(networkManager.player.level().getWorld(), 0, 0, 0);
                short[] originalOffsetArray = (short[])OFFSETARRAY_MULTIBLOCKCHANGE.get(newPacket);
                BlockState[] originalDataArray = (BlockState[])BLOCKARRAY_MULTIBLOCKCHANGE.get(newPacket);
                short[] offsetArray = Arrays.copyOf(originalOffsetArray, originalOffsetArray.length);
                BlockState[] dataArray = Arrays.copyOf(originalDataArray, originalDataArray.length);
                OFFSETARRAY_MULTIBLOCKCHANGE.set(newPacket, offsetArray);
                BLOCKARRAY_MULTIBLOCKCHANGE.set(newPacket, dataArray);
                for (int i = 0; i < offsetArray.length; i++) {
                    short offset = offsetArray[i];
                    BlockPos pos = coord.relativeToBlockPos(offset);
                    location.setX(pos.getX());
                    location.setY(pos.getY());
                    location.setZ(pos.getZ());
                    FakeBlock block = map.byLocation.get(location);
                    if (block != null) {
                        dataArray[i] = FakeBlockHelper.getNMSState(block);
                    }
                }
                return newPacket;
            }
            else if (packet instanceof ClientboundBlockUpdatePacket) {
                BlockPos pos = ((ClientboundBlockUpdatePacket) packet).getPos();
                LocationTag loc = new LocationTag(networkManager.player.level().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(networkManager.player.getUUID(), loc);
                if (block != null) {
                    ClientboundBlockUpdatePacket newPacket = new ClientboundBlockUpdatePacket(((ClientboundBlockUpdatePacket) packet).getPos(), FakeBlockHelper.getNMSState(block));
                    return newPacket;
                }
            }
            else if (packet instanceof ClientboundBlockChangedAckPacket) {
                // TODO: 1.19: Can no longer determine what block this packet is for. Would have to track separately? Possibly from the inbound packet rather than the outbound one.
                /*
                ClientboundBlockChangedAckPacket origPack = (ClientboundBlockChangedAckPacket) packet;
                BlockPos pos = origPack.pos();
                LocationTag loc = new LocationTag(player.getLevel().getWorld(), pos.getX(), pos.getY(), pos.getZ());
                FakeBlock block = FakeBlock.getFakeBlockFor(player.getUUID(), loc);
                if (block != null) {
                    ClientboundBlockChangedAckPacket newPacket = new ClientboundBlockChangedAckPacket(origPack.pos(), FakeBlockHelper.getNMSState(block), origPack.action(), false);
                    oldManager.send(newPacket, genericfuturelistener);
                    return true;
                }*/
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return packet;
    }
}

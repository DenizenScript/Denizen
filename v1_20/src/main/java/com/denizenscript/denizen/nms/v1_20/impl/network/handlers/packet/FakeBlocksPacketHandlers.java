package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftLocation;

import java.lang.reflect.Field;
import java.util.*;

public class FakeBlocksPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundLevelChunkWithLightPacket.class, FakeBlocksPacketHandlers::processLevelChunkWithLightPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSectionBlocksUpdatePacket.class, FakeBlocksPacketHandlers::processSectionBlocksUpdatePacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundBlockUpdatePacket.class, FakeBlocksPacketHandlers::processBlockUpdatePacket);
    }

    public static final Field SECTIONPOS_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_sectionPos, SectionPos.class);
    public static final Field OFFSETARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_positions, short[].class);
    public static final Field BLOCKARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_states, BlockState[].class);

    public static BlockState getNMSState(FakeBlock block) {
        return ((CraftBlockData) block.material.getModernData()).getState();
    }

    public static Packet<ClientGamePacketListener> processLevelChunkWithLightPacket(DenizenNetworkManagerImpl networkManager, ClientboundLevelChunkWithLightPacket chunkPacketData) {
        if (FakeBlock.blocks.isEmpty()) {
            return chunkPacketData;
        }
        FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(networkManager.player.getUUID());
        if (map == null) {
            return chunkPacketData;
        }
        int chunkX = chunkPacketData.getX();
        int chunkZ = chunkPacketData.getZ();
        ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, networkManager.player.level().getWorld().getName());
        List<FakeBlock> blocks = FakeBlock.getFakeBlocksFor(networkManager.player.getUUID(), chunkCoord);
        if (blocks == null || blocks.isEmpty()) {
            return chunkPacketData;
        }
        List<Packet<ClientGamePacketListener>> packets = new ArrayList<>();
        packets.add(chunkPacketData);
        Map<SectionPos, List<FakeBlock>> blocksPerSection = new HashMap<>();
        for (FakeBlock fakeBlock : blocks) {
            blocksPerSection.computeIfAbsent(SectionPos.of(CraftLocation.toBlockPosition(fakeBlock.location)), key -> new ArrayList<>()).add(fakeBlock);
        }
        for (Map.Entry<SectionPos, List<FakeBlock>> entry : blocksPerSection.entrySet()) {
            SectionPos sectionPos = entry.getKey();
            List<FakeBlock> fakeBlocks = entry.getValue();
            short[] positionOffsets = new short[fakeBlocks.size()];
            BlockState[] states = new BlockState[fakeBlocks.size()];
            int i = 0;
            for (FakeBlock fakeBlock : fakeBlocks) {
                positionOffsets[i] = SectionPos.sectionRelativePos(CraftLocation.toBlockPosition(fakeBlock.location));
                states[i] = getNMSState(fakeBlock);
                i++;
            }
            packets.add(new ClientboundSectionBlocksUpdatePacket(sectionPos, new ShortArraySet(positionOffsets), states));
        }
        // Alternative:
//        List<Packet<ClientGamePacketListener>> packets = new ArrayList<>(blocks.size() + 1);
//        packets.add(chunkPacketData);
//        for (FakeBlock fakeBlock : blocks) {
//            packets.add(new ClientboundBlockUpdatePacket(CraftLocation.toBlockPosition(fakeBlock.location), FakeBlockHelper.getNMSState(fakeBlock)));
//        }
        return new ClientboundBundlePacket(packets);
    }

    public static ClientboundSectionBlocksUpdatePacket processSectionBlocksUpdatePacket(DenizenNetworkManagerImpl networkManager, ClientboundSectionBlocksUpdatePacket sectionBlocksUpdatePacket) throws Exception {
        if (FakeBlock.blocks.isEmpty()) {
            return sectionBlocksUpdatePacket;
        }
        FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(networkManager.player.getUUID());
        if (map == null) {
            return sectionBlocksUpdatePacket;
        }
        SectionPos coord = (SectionPos) SECTIONPOS_MULTIBLOCKCHANGE.get(sectionBlocksUpdatePacket);
        ChunkCoordinate coordinateDenizen = new ChunkCoordinate(coord.getX(), coord.getZ(), networkManager.player.level().getWorld().getName());
        if (!map.byChunk.containsKey(coordinateDenizen)) {
            return sectionBlocksUpdatePacket;
        }
        Debug.log("Received section blocks update");
        short[] originalOffsetArray = (short[]) OFFSETARRAY_MULTIBLOCKCHANGE.get(sectionBlocksUpdatePacket);
        BlockState[] originalDataArray = (BlockState[]) BLOCKARRAY_MULTIBLOCKCHANGE.get(sectionBlocksUpdatePacket);
        BlockState[] dataArray = Arrays.copyOf(originalDataArray, originalDataArray.length);
        LocationTag location = new LocationTag(networkManager.player.level().getWorld(), 0, 0, 0);
        for (int i = 0; i < originalOffsetArray.length; i++) {
            short offset = originalOffsetArray[i];
            BlockPos pos = coord.relativeToBlockPos(offset);
            location.setX(pos.getX());
            location.setY(pos.getY());
            location.setZ(pos.getZ());
            FakeBlock block = map.byLocation.get(location);
            if (block != null) {
                dataArray[i] = getNMSState(block);
            }
        }
        return new ClientboundSectionBlocksUpdatePacket(coord, new ShortArraySet(originalOffsetArray), dataArray);
    }

    public static ClientboundBlockUpdatePacket processBlockUpdatePacket(DenizenNetworkManagerImpl networkManager, ClientboundBlockUpdatePacket blockUpdatePacket) {
        if (FakeBlock.blocks.isEmpty()) {
            return blockUpdatePacket;
        }
        BlockPos pos = blockUpdatePacket.getPos();
        LocationTag loc = new LocationTag(networkManager.player.level().getWorld(), pos.getX(), pos.getY(), pos.getZ());
        FakeBlock block = FakeBlock.getFakeBlockFor(networkManager.player.getUUID(), loc);
        if (block != null) {
            return new ClientboundBlockUpdatePacket(pos, getNMSState(block));
        }
        return blockUpdatePacket;
    }

    public static ClientboundBlockChangedAckPacket processBlockChangedAckPacket(DenizenNetworkManagerImpl networkManager, ClientboundBlockChangedAckPacket blockChangedAckPacket) {
        if (FakeBlock.blocks.isEmpty()) {
            return blockChangedAckPacket;
        }
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
        return blockChangedAckPacket;
    }
}

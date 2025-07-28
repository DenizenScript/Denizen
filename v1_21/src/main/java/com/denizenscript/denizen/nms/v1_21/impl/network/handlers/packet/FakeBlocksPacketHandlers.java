package com.denizenscript.denizen.nms.v1_21.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_21.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_21.impl.network.handlers.FakeBlockHelper;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.ChunkCoordinate;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FakeBlocksPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundLevelChunkWithLightPacket.class, FakeBlocksPacketHandlers::processLevelChunkWithLightPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSectionBlocksUpdatePacket.class, FakeBlocksPacketHandlers::processSectionBlocksUpdatePacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundBlockUpdatePacket.class, FakeBlocksPacketHandlers::processBlockUpdatePacket);
    }

    public static Field SECTIONPOS_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_sectionPos, SectionPos.class);
    public static Field OFFSETARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_positions, short[].class);
    public static Field BLOCKARRAY_MULTIBLOCKCHANGE = ReflectionHelper.getFields(ClientboundSectionBlocksUpdatePacket.class).get(ReflectionMappingsInfo.ClientboundSectionBlocksUpdatePacket_states, BlockState[].class);

    public static ClientboundLevelChunkWithLightPacket processLevelChunkWithLightPacket(DenizenNetworkManagerImpl networkManager, ClientboundLevelChunkWithLightPacket chunkPacket) throws Throwable {
        if (FakeBlock.blocks.isEmpty()) {
            return chunkPacket;
        }
        FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(networkManager.player.getUUID());
        if (map == null) {
            return chunkPacket;
        }
        int chunkX = chunkPacket.getX();
        int chunkZ = chunkPacket.getZ();
        ChunkCoordinate chunkCoord = new ChunkCoordinate(chunkX, chunkZ, networkManager.player.level().getWorld().getName());
        List<FakeBlock> blocks = FakeBlock.getFakeBlocksFor(networkManager.player.getUUID(), chunkCoord);
        if (blocks == null || blocks.isEmpty()) {
            return chunkPacket;
        }
        return FakeBlockHelper.handleMapChunkPacket(networkManager.player.getBukkitEntity().getWorld(), chunkPacket, chunkX, chunkZ, blocks, map);
    }

    public static ClientboundSectionBlocksUpdatePacket processSectionBlocksUpdatePacket(DenizenNetworkManagerImpl networkManager, ClientboundSectionBlocksUpdatePacket sectionUpdatePacket) throws IllegalAccessException {
        if (FakeBlock.blocks.isEmpty()) {
            return sectionUpdatePacket;
        }
        FakeBlock.FakeBlockMap map = FakeBlock.blocks.get(networkManager.player.getUUID());
        if (map == null) {
            return sectionUpdatePacket;
        }
        SectionPos coord = (SectionPos) SECTIONPOS_MULTIBLOCKCHANGE.get(sectionUpdatePacket);
        ChunkCoordinate coordinateDenizen = new ChunkCoordinate(coord.getX(), coord.getZ(), networkManager.player.level().getWorld().getName());
        if (!map.byChunk.containsKey(coordinateDenizen)) {
            return sectionUpdatePacket;
        }
        short[] originalOffsetArray = (short[])OFFSETARRAY_MULTIBLOCKCHANGE.get(sectionUpdatePacket);
        BlockState[] originalStatesArray = (BlockState[])BLOCKARRAY_MULTIBLOCKCHANGE.get(sectionUpdatePacket);
        BlockState[] statesArray = Arrays.copyOf(originalStatesArray, originalStatesArray.length);
        LocationTag location = new LocationTag(networkManager.player.level().getWorld(), 0, 0, 0);
        boolean hasAny = false;
        for (int i = 0; i < originalOffsetArray.length; i++) {
            short offset = originalOffsetArray[i];
            location.setX(coord.relativeToBlockX(offset));
            location.setY(coord.relativeToBlockY(offset));
            location.setZ(coord.relativeToBlockZ(offset));
            FakeBlock block = map.byLocation.get(location);
            if (block != null) {
                statesArray[i] = FakeBlockHelper.getNMSState(block);
                hasAny = true;
            }
        }
        if (!hasAny) {
            return sectionUpdatePacket;
        }
        return new ClientboundSectionBlocksUpdatePacket(coord, new ShortArraySet(originalOffsetArray), statesArray);
    }

    public static ClientboundBlockUpdatePacket processBlockUpdatePacket(DenizenNetworkManagerImpl networkManager, ClientboundBlockUpdatePacket blockUpdatePacket) {
        if (FakeBlock.blocks.isEmpty()) {
            return blockUpdatePacket;
        }
        BlockPos pos = blockUpdatePacket.getPos();
        LocationTag loc = new LocationTag(networkManager.player.level().getWorld(), pos.getX(), pos.getY(), pos.getZ());
        FakeBlock block = FakeBlock.getFakeBlockFor(networkManager.player.getUUID(), loc);
        if (block != null) {
            return new ClientboundBlockUpdatePacket(blockUpdatePacket.getPos(), FakeBlockHelper.getNMSState(block));
        }
        return blockUpdatePacket;
    }

    // TODO: 1.19: Can no longer determine what block this packet is for. Would have to track separately? Possibly from the inbound packet rather than the outbound one.
    /*
    public static ClientboundBlockChangedAckPacket processBlockChangedAckPacket(DenizenNetworkManagerImpl networkManager, ClientboundBlockChangedAckPacket blockChangedAckPacket) {
        if (FakeBlock.blocks.isEmpty()) {
            return blockChangedAckPacket;
        }
        BlockPos pos = blockChangedAckPacket.pos();
        LocationTag loc = new LocationTag(player.getLevel().getWorld(), pos.getX(), pos.getY(), pos.getZ());
        FakeBlock block = FakeBlock.getFakeBlockFor(player.getUUID(), loc);
        if (block != null) {
            ClientboundBlockChangedAckPacket newPacket = new ClientboundBlockChangedAckPacket(blockChangedAckPacket.pos(), FakeBlockHelper.getNMSState(block), blockChangedAckPacket.action(), false);
            oldManager.send(newPacket, genericfuturelistener);
            return true;
        }
    }
    */
}

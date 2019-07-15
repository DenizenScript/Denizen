package com.denizenscript.denizen.nms.impl.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;

import java.util.ArrayList;

public class BlockLight_v1_14_R1 extends BlockLight {

    private BlockLight_v1_14_R1(Location location, long ticks) {
        super(location, ticks);
    }

    public static BlockLight createLight(Location location, int lightLevel, long ticks) {
        location = location.getBlock().getLocation();
        BlockLight blockLight;
        if (lightsByLocation.containsKey(location)) {
            blockLight = lightsByLocation.get(location);
            if (blockLight.removeTask != null) {
                blockLight.removeTask.cancel();
                blockLight.removeTask = null;
            }
            blockLight.reset(true);
            blockLight.removeLater(ticks);
        }
        else {
            blockLight = new BlockLight_v1_14_R1(location, ticks);
            lightsByLocation.put(location, blockLight);
            if (!lightsByChunk.containsKey(blockLight.chunk)) {
                lightsByChunk.put(blockLight.chunk, new ArrayList<>());
            }
            lightsByChunk.get(blockLight.chunk).add(blockLight);
        }
        blockLight.update(lightLevel, true);
        return blockLight;
    }

    @Override
    public void update(int lightLevel, boolean updateChunk) {
        LightEngine lightEngine = ((CraftChunk) chunk).getHandle().e();
        ((LightEngineBlock) lightEngine.a(EnumSkyBlock.BLOCK)).a(((CraftBlock) block).getPosition(), lightLevel);
        if (updateChunk) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), this::sendChunkUpdate, 1);
        }
    }

    public void sendChunkUpdate() {
        LightEngine lightEngine = ((CraftChunk) chunk).getHandle().e();
        PacketPlayOutLightUpdate packet = new PacketPlayOutLightUpdate(((CraftChunk) chunk).getHandle().getPos(), lightEngine);
        ((CraftWorld) chunk.getWorld()).getHandle().getChunkProvider().playerChunkMap
                .a(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()), false).forEach((player) -> {
            player.playerConnection.sendPacket(packet);
        });
    }
}

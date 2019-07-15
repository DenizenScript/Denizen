package com.denizenscript.denizen.nms.impl.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.util.Vector;

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
            Bukkit.getScheduler().scheduleSyncDelayedTask(NMSHandler.getJavaPlugin(), this::sendChunkUpdates, 1);
        }
    }

    public static final Vector[] RELATIVE_CHUNKS = new Vector[] {
            new Vector(-1, 0, 0), new Vector(1, 0, 0), new Vector(0, 0, -1), new Vector(0, 0, 1),
            new Vector(-1, 0, -1), new Vector(-1, 0, 1), new Vector(1, 0, -1), new Vector(1, 0, 1)
    };

    public void sendChunkUpdates() {
        sendChunkUpdate(((CraftChunk) chunk).getHandle());
        for (Vector vec : RELATIVE_CHUNKS) {
            CraftChunk other = (CraftChunk) chunk.getWorld().getChunkAt(chunk.getX() + vec.getBlockX(), chunk.getZ() + vec.getBlockZ());
            sendChunkUpdate(other.getHandle());
        }
    }

    public static void sendChunkUpdate(Chunk chunk) {
        LightEngine lightEngine = chunk.e();
        ChunkCoordIntPair pos = chunk.getPos();
        PacketPlayOutLightUpdate packet = new PacketPlayOutLightUpdate(pos, lightEngine);
        ((WorldServer) chunk.world).getChunkProvider().playerChunkMap.a(pos, false).forEach((player) -> {
            player.playerConnection.sendPacket(packet);
        });
    }
}

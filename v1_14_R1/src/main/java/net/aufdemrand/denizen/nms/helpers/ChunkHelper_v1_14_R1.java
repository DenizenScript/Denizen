package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.ChunkHelper;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

public class ChunkHelper_v1_14_R1 implements ChunkHelper {

    @Override
    public void refreshChunkSections(Chunk chunk) {
        PacketPlayOutMapChunk lowPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 255); // 00000000 11111111
        PacketPlayOutMapChunk highPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65280); // 11111111 00000000
        ChunkCoordIntPair pos = new ChunkCoordIntPair(chunk.getX(), chunk.getZ());
        PlayerChunk playerChunk = ((CraftWorld) chunk.getWorld()).getHandle().getChunkProvider()
                .playerChunkMap.visibleChunks.get(pos.pair());
        if (playerChunk == null) {
            return;
        }
        playerChunk.players.a(pos, false).forEach(player -> {
            player.playerConnection.sendPacket(lowPacket);
            player.playerConnection.sendPacket(highPacket);
        });
    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        // TODO: 1.14 - is this a valid alternative to the removed LIGHT_BLOCKING?
        long[] lightBlocking = ((CraftChunk) chunk).getHandle().heightMap.get(HeightMap.Type.MOTION_BLOCKING).a();
        int[] heightmap = new int[lightBlocking.length];
        for (int i = 0; i < lightBlocking.length; i++) {
            heightmap[i] = (int) lightBlocking[i];
        }
        return heightmap;
    }
}

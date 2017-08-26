package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.ChunkHelper;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

public class ChunkHelper_v1_9_R2 implements ChunkHelper {

    @Override
    public void refreshChunkSections(Chunk chunk) {
        PacketPlayOutMapChunk lowPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 255); // 00000000 11111111
        PacketPlayOutMapChunk highPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65280); // 11111111 00000000
        PlayerChunk playerChunk = ((CraftWorld) chunk.getWorld()).getHandle().getPlayerChunkMap()
                .getChunk(chunk.getX(), chunk.getZ());
        if (playerChunk == null) return;
        for (EntityPlayer player : playerChunk.c) {
            player.playerConnection.sendPacket(lowPacket);
            player.playerConnection.sendPacket(highPacket);
        }
    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        return ((CraftChunk) chunk).getHandle().heightMap;
    }
}

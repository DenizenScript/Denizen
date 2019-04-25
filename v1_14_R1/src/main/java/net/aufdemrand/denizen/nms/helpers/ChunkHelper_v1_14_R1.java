package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.ChunkHelper;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.HeightMap;
import net.minecraft.server.v1_14_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_14_R1.PlayerChunk;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

public class ChunkHelper_v1_14_R1 implements ChunkHelper {

    @Override
    public void refreshChunkSections(Chunk chunk) {
        PacketPlayOutMapChunk lowPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 255); // 00000000 11111111
        PacketPlayOutMapChunk highPacket = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65280); // 11111111 00000000
        PlayerChunk playerChunk = ((CraftWorld) chunk.getWorld()).getHandle().getPlayerChunkMap()
                .getChunk(chunk.getX(), chunk.getZ());
        if (playerChunk == null) {
            return;
        }
        for (EntityPlayer player : playerChunk.players) {
            player.playerConnection.sendPacket(lowPacket);
            player.playerConnection.sendPacket(highPacket);
        }
    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        long[] lightBlocking = ((CraftChunk) chunk).getHandle().heightMap.get(HeightMap.Type.LIGHT_BLOCKING).b();
        int[] heightmap = new int[lightBlocking.length];
        for (int i = 0; i < lightBlocking.length; i++) {
            heightmap[i] = (int) lightBlocking[i];
        }
        return heightmap;
    }
}

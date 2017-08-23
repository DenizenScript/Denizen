package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.ChunkHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ChunkHelper_v1_8_R3 implements ChunkHelper {

    @Override
    public void refreshChunk(Chunk chunk) {

        PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), false, 65535);

        for (Player player : chunk.getWorld().getPlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }

    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        return ((CraftChunk) chunk).getHandle().heightMap;
    }
}

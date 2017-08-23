package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.ChunkHelper;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import static sun.audio.AudioPlayer.player;

public class ChunkHelper_v1_9_R2 implements ChunkHelper {

    @Override
    public void refreshChunk(Chunk chunk) {

        PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535);

        for (Player player : chunk.getWorld().getPlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }

    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        return ((CraftChunk) chunk).getHandle().heightMap;
    }
}

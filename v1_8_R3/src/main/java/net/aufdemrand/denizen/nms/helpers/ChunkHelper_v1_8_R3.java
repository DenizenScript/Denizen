package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.ChunkHelper;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.LongHashMap;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PlayerChunkMap;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ChunkHelper_v1_8_R3 implements ChunkHelper {

    @SuppressWarnings("unchecked")
    public List<EntityPlayer> getPlayersWithChunkLoaded(Chunk chunk) {
        PlayerChunkMap playerChunkMap = ((CraftWorld) chunk.getWorld()).getHandle().getPlayerChunkMap();

        if (!playerChunkMap.isChunkInUse(chunk.getX(), chunk.getZ())) {
            return new ArrayList<EntityPlayer>();
        }

        // Use reflection to grab list of players with the chunk loaded
        try {
            Field fieldMap = PlayerChunkMap.class.getDeclaredField("d");
            fieldMap.setAccessible(true);

            LongHashMap<Object> map = (LongHashMap<Object>) fieldMap.get(playerChunkMap);
            long entry = (long) chunk.getX() + 2147483647L | (long) chunk.getZ() + 2147483647L << 32;
            Object playerChunk = map.getEntry(entry);

            Field fieldPlayers = playerChunk.getClass().getDeclaredField("b");
            fieldPlayers.setAccessible(true);

            return (List<EntityPlayer>) fieldPlayers.get(playerChunk);
        }
        catch (NoSuchFieldException e) {}
        catch (IllegalAccessException e) {}

        return new ArrayList<EntityPlayer>();
    }

    @Override
    public void refreshChunkSections(Chunk chunk) {
        PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), false, 65535);
        for (EntityPlayer player : getPlayersWithChunkLoaded(chunk)) {
            player.playerConnection.sendPacket(packet);
        }
    }

    @Override
    public int[] getHeightMap(Chunk chunk) {
        return ((CraftChunk) chunk).getHandle().heightMap;
    }
}

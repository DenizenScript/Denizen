package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.ChunkHelper;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;

public class ChunkHelper_v1_9_R2 implements ChunkHelper {

    @Override
    public int[] getHeightMap(Chunk chunk) {
        return ((CraftChunk) chunk).getHandle().heightMap;
    }
}

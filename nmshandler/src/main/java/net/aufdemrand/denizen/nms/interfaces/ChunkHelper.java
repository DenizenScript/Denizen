package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Chunk;

public interface ChunkHelper {

    void refreshChunk(Chunk chunk);

    int[] getHeightMap(Chunk chunk);
}

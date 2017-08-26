package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Chunk;

public interface ChunkHelper {

    void refreshChunkSections(Chunk chunk);

    int[] getHeightMap(Chunk chunk);
}

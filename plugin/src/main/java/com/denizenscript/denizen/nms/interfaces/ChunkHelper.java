package com.denizenscript.denizen.nms.interfaces;

import org.bukkit.Chunk;
import org.bukkit.World;

public interface ChunkHelper {

    void refreshChunkSections(Chunk chunk);

    int[] getHeightMap(Chunk chunk);

    default void changeChunkServerThread(World world) {
        // Do nothing by default.
    }

    default void restoreServerThread(World world) {
        // Do nothing by default.
    }
}

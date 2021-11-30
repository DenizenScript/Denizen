package com.denizenscript.denizen.nms.interfaces;

import org.bukkit.Chunk;
import org.bukkit.World;

public interface ChunkHelper {

    default void refreshChunkSections(Chunk chunk) {
        throw new UnsupportedOperationException();
    }

    int[] getHeightMap(Chunk chunk);

    default void changeChunkServerThread(World world) {
        // Do nothing by default.
    }

    default void restoreServerThread(World world) {
        // Do nothing by default.
    }
}

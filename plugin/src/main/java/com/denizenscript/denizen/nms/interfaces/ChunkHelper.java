package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
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

    default void setAllBiomes(Chunk chunk, BiomeNMS biome) {
        throw new UnsupportedOperationException();
    }
}

package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.WorldTag;
import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkCoordinate {

    public final int x;

    public final int z;

    public final String worldName;

    public ChunkCoordinate(int _x, int _z, String _worldName) {
        x = _x;
        z = _z;
        worldName = _worldName;
    }

    public ChunkCoordinate(Location location) {
        x = location.getBlockX() >> 4;
        z = location.getBlockZ() >> 4;
        worldName = location.getWorld().getName();
    }

    public ChunkCoordinate(Chunk chunk) {
        x = chunk.getX();
        z = chunk.getZ();
        worldName = chunk.getWorld().getName();
    }

    @Override
    public int hashCode() {
        return x + z + worldName.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ChunkCoordinate)) {
            return false;
        }
        return equals((ChunkCoordinate) other);
    }

    public boolean equals(ChunkCoordinate other) {
        if (other == null) {
            return false;
        }
        return x == other.x && z == other.z && worldName.equals(other.worldName);
    }

    @Override
    public String toString() {
        return x + "," + z + "," + worldName;
    }

    public ChunkTag getChunk() {
        return new ChunkTag(new WorldTag(worldName), x, z);
    }
}

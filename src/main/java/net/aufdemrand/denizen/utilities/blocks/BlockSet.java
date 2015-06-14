package net.aufdemrand.denizen.utilities.blocks;

import org.bukkit.Location;

import java.util.List;

public interface BlockSet {

    public abstract List<BlockData> getBlocks();

    public abstract void setBlocksDelayed(Location loc, Runnable runme);

    public abstract void setBlocks(Location loc);

    public abstract String toCompressedFormat();
}

package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.nms.interfaces.BlockData;
import org.bukkit.Location;

import java.util.List;

public interface BlockSet {

    public abstract List<BlockData> getBlocks();

    public abstract void setBlocksDelayed(Location loc, Runnable runme, boolean noAir);

    public abstract void setBlocks(Location loc, boolean noAir);

    public abstract String toCompressedFormat();
}

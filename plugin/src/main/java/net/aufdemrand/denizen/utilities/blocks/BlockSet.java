package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.nms.interfaces.BlockData;
import org.bukkit.Location;

import java.util.List;

public interface BlockSet {

    List<BlockData> getBlocks();

    void setBlocksDelayed(Location loc, Runnable runme, boolean noAir);

    void setBlocks(Location loc, boolean noAir);
}

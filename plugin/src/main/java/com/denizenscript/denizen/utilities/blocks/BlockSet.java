package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.interfaces.BlockData;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashSet;

public interface BlockSet {

    class InputParams {

        public Location centerLocation;

        public boolean noAir;

        public HashSet<Material> mask;
    }

    BlockData[] getBlocks();

    void setBlocksDelayed(final Runnable runme, final InputParams input);

    void setBlocks(InputParams input);
}

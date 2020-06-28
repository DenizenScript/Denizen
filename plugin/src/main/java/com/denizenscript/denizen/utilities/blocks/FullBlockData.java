package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class FullBlockData {

    public FullBlockData(Block block) {
        this(block.getBlockData());
        tileEntityData = NMSHandler.getBlockHelper().getNbtData(block);
    }

    public FullBlockData(BlockData data) {
        this.data = data;
    }

    public BlockData data;

    public CompoundTag tileEntityData;

    public void set(Block block, boolean physics) {
        block.setBlockData(data, physics);
        if (tileEntityData != null) {
            NMSHandler.getBlockHelper().setNbtData(block, tileEntityData);
        }
    }
}

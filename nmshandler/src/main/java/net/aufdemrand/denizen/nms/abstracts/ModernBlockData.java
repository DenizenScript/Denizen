package net.aufdemrand.denizen.nms.abstracts;

import net.aufdemrand.denizen.nms.NMSHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

/**
 * Helper for 1.13+ block data.
 */
public class ModernBlockData {

    public BlockData data;

    public ModernBlockData(Material material) {
        this.data = material.createBlockData();
    }

    public ModernBlockData(Block block) {
        NMSHandler.getInstance().getChunkHelper().changeChunkServerThread(block.getWorld());
        this.data = block.getBlockData();
        NMSHandler.getInstance().getChunkHelper().restoreServerThread(block.getWorld());
    }

    public ModernBlockData(BlockState block) {
        this.data = block.getBlockData();
    }

    public ModernBlockData(BlockData data) {
        this.data = data;
    }

    public Material getMaterial() {
        return data.getMaterial();
    }

    public BlockState getBlockState() {
        BlockState state = NMSHandler.getInstance().getBlockHelper().generateBlockState(getMaterial());
        state.setBlockData(data);
        return state;
    }
}

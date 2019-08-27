package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.debugging.Debug;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Player;

/**
 * Helper for 1.13+ block data.
 */
public class ModernBlockData implements Cloneable {

    public BlockData data;

    public ModernBlockData(Material material) {
        this.data = material.createBlockData();
    }

    public ModernBlockData(Block block) {
        NMSHandler.getChunkHelper().changeChunkServerThread(block.getWorld());
        this.data = block.getBlockData();
        NMSHandler.getChunkHelper().restoreServerThread(block.getWorld());
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
        BlockState state = NMSHandler.getBlockHelper().generateBlockState(getMaterial());
        state.setBlockData(data);
        return state;
    }

    public Boolean getSwitchState() {
        if (data instanceof Openable) {
            return ((Openable) data).isOpen();
        }
        else if (data instanceof Powerable) {
            return ((Powerable) data).isPowered();
        }
        else if (data instanceof Dispenser) {
            return ((Dispenser) data).isTriggered();
        }
        return null;
    }

    public boolean setSwitchState(Block block, boolean state) {
        if (data instanceof Openable) {
            ((Openable) data).setOpen(state);
        }
        else if (data instanceof Powerable) {
            ((Powerable) data).setPowered(true);
        }
        else if (data instanceof Dispenser) {
            ((Dispenser) data).setTriggered(true);
        }
        else {
            return false;
        }
        NMSHandler.getChunkHelper().changeChunkServerThread(block.getWorld());
        block.setBlockData(data, true);
        block.getState().update(true, true);
        NMSHandler.getChunkHelper().restoreServerThread(block.getWorld());
        return true;
    }

    public void sendFakeChangeTo(Player player, Location location) {
        player.sendBlockChange(location, data);
    }

    @Override
    public ModernBlockData clone() {
        try {
            ModernBlockData data = (ModernBlockData) super.clone();
            data.data = data.data.clone();
            return data;
        }
        catch (CloneNotSupportedException ex) {
            Debug.echoError(ex);
            return null;
        }
    }
}

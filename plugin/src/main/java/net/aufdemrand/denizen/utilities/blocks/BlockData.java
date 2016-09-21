package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.TileEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;

import java.util.List;

public class BlockData {

    public Material material;
    public int data;

    public BlockData() {
    }

    public BlockData(short mat, byte dat) {
        material = Material.getMaterial(mat);
        data = dat;
    }

    public BlockData(Block block) {
        material = block.getType();
        data = block.getData();
        TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (te != null) {
            ctag = new NBTTagCompound();
            te.save(ctag);
        }
    }

    public void setBlock(Block block) {
        block.setTypeIdAndData(material.getId(), (byte) data, false);
        if (ctag != null) {
            ctag.setInt("x", block.getX());
            ctag.setInt("y", block.getY());
            ctag.setInt("z", block.getZ());
            // TODO: make this work!
            BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
            TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(blockPos);
            te.a(ctag);
        }
    }

    public String toCompressedFormat() {
        return "{" + material.getId() + ":" + data + "}";
    }

    public static BlockData fromCompressedString(String str) {
        BlockData data = new BlockData();
        String inner = str.substring(1, str.length() - 1);
        List<String> datas = CoreUtilities.split(inner, ':');
        data.material = Material.getMaterial(Integer.parseInt(datas.get(0)));
        data.data = Integer.parseInt(datas.get(1));
        if (data.material == null) {
            throw new RuntimeException("Null material: " + datas.get(0));
        }
        return data;
    }

    NBTTagCompound ctag = null;

    public NBTTagCompound getNBTTag() {
        return ctag;
    }

    public void setNBTTag(NBTTagCompound tag) {
        ctag = tag;
    }
}

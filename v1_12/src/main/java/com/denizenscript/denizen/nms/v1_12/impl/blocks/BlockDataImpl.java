package com.denizenscript.denizen.nms.v1_12.impl.blocks;

import com.denizenscript.denizen.nms.v1_12.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.TileEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

public class BlockDataImpl implements BlockData {

    private Material material;
    private byte data;

    public BlockDataImpl() {
    }

    public BlockDataImpl(Material mat, byte dat) {
        material = mat;
        data = dat;
    }

    public BlockDataImpl(Block block) {
        material = block.getType();
        data = block.getData();
        TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (te != null) {
            NBTTagCompound compound = new NBTTagCompound();
            te.save(compound);
            ctag = (CompoundTagImpl) CompoundTagImpl.fromNMSTag(compound);
        }
    }

    public void setBlock(Block block, boolean physics) {
        block.setTypeIdAndData(material.getId(), data, physics);
        if (ctag != null) {
            CompoundTagBuilder builder = ctag.createBuilder();
            builder.putInt("x", block.getX());
            builder.putInt("y", block.getY());
            builder.putInt("z", block.getZ());
            ctag = (CompoundTagImpl) builder.build();
            // TODO: make this work!
            BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
            TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(blockPos);
            te.load(ctag.toNMSTag());
        }
    }

    public String toCompressedFormat() {
        return "{" + material.getId() + ":" + data + "}";
    }

    public static BlockData fromCompressedString(String str) {
        BlockData data = new BlockDataImpl();
        String inner = str.substring(1, str.length() - 1);
        String[] datas = inner.split(":");
        data.setMaterial(Material.getMaterial(Integer.parseInt(datas[0])));
        data.setData(Byte.parseByte(datas[1]));
        if (data.getMaterial() == null) {
            throw new RuntimeException("Null material: " + datas[0]);
        }
        return data;
    }

    CompoundTagImpl ctag = null;

    @Override
    public CompoundTag getCompoundTag() {
        return ctag;
    }

    @Override
    public void setCompoundTag(CompoundTag tag) {
        ctag = (CompoundTagImpl) tag;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public void setMaterial(Material material) {
        this.material = material;
    }

    @Override
    public byte getData() {
        return data;
    }

    @Override
    public void setData(byte data) {
        this.data = data;
    }
}

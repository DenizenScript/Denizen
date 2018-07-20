package net.aufdemrand.denizen.nms.impl.blocks;

import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTagBuilder;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.TileEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;

public class BlockData_v1_10_R1 implements BlockData {

    private Material material;
    private byte data;

    public BlockData_v1_10_R1() {
    }

    public BlockData_v1_10_R1(Material mat, byte dat) {
        material = mat;
        data = dat;
    }

    public BlockData_v1_10_R1(Block block) {
        material = block.getType();
        data = block.getData();
        TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (te != null) {
            NBTTagCompound compound = new NBTTagCompound();
            te.save(compound);
            ctag = (CompoundTag_v1_10_R1) CompoundTag_v1_10_R1.fromNMSTag(compound);
        }
    }

    public void setBlock(Block block, boolean physics) {
        block.setTypeIdAndData(material.getId(), (byte) data, physics);
        if (ctag != null) {
            CompoundTagBuilder builder = ctag.createBuilder();
            builder.putInt("x", block.getX());
            builder.putInt("y", block.getY());
            builder.putInt("z", block.getZ());
            ctag = (CompoundTag_v1_10_R1) builder.build();
            // TODO: make this work!
            BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
            TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(blockPos);
            te.a(ctag.toNMSTag());
        }
    }

    public String toCompressedFormat() {
        return "{" + material.getId() + ":" + data + "}";
    }

    public static BlockData fromCompressedString(String str) {
        BlockData data = new BlockData_v1_10_R1();
        String inner = str.substring(1, str.length() - 1);
        String[] datas = inner.split(":");
        data.setMaterial(Material.getMaterial(Integer.parseInt(datas[0])));
        data.setData(Byte.parseByte(datas[1]));
        if (data.getMaterial() == null) {
            throw new RuntimeException("Null material: " + datas[0]);
        }
        return data;
    }

    CompoundTag_v1_10_R1 ctag = null;

    @Override
    public CompoundTag getCompoundTag() {
        return ctag;
    }

    @Override
    public void setCompoundTag(CompoundTag tag) {
        ctag = (CompoundTag_v1_10_R1) tag;
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

package com.denizenscript.denizen.nms.v1_15.impl.blocks;

import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizen.nms.v1_15.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

public class BlockDataImpl implements BlockData {

    private org.bukkit.block.data.BlockData blockData;

    public BlockDataImpl() {
    }

    public BlockDataImpl(Material mat, byte dat) {
        if (!mat.isLegacy()) {
            if (dat == 0) {
                blockData = Bukkit.createBlockData(mat);
                return;
            }
            mat = Bukkit.getUnsafe().toLegacy(mat);
        }
        blockData = Bukkit.getUnsafe().fromLegacy(mat, dat);
    }

    public BlockDataImpl(org.bukkit.block.data.BlockData data) {
        this.blockData = data;
        // TODO: ctag?
    }

    public BlockDataImpl(Block block) {
        ModernBlockData mbd = new ModernBlockData(block);
        blockData = mbd.data;
        TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(
                new BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (te != null) {
            NBTTagCompound compound = new NBTTagCompound();
            te.save(compound);
            ctag = (CompoundTagImpl) CompoundTagImpl.fromNMSTag(compound);
        }
    }

    public void setBlock(Block block, boolean physics) {
        block.setBlockData(blockData, physics);
        if (ctag != null) {
            CompoundTagBuilder builder = ctag.createBuilder();
            builder.putInt("x", block.getX());
            builder.putInt("y", block.getY());
            builder.putInt("z", block.getZ());
            ctag = (CompoundTagImpl) builder.build();
            BlockPosition blockPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
            TileEntity te = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(blockPos);
            te.load(ctag.toNMSTag());
        }
    }

    public String toCompressedFormat() {
        return "{" + blockData.getMaterial().name() + "}";
    }

    public static BlockData fromCompressedString(String str) {
        BlockDataImpl data = new BlockDataImpl();
        data.blockData = Bukkit.createBlockData(Material.getMaterial(str));
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
        return blockData.getMaterial();
    }

    @Override
    public void setMaterial(Material material) {
        blockData = Bukkit.createBlockData(material);
    }

    @Override
    public byte getData() {
        return 0;
    }

    @Override
    public void setData(byte data) {
        // no
    }

    @Override
    public ModernBlockData modern() {
        return new ModernBlockData(blockData);
    }
}

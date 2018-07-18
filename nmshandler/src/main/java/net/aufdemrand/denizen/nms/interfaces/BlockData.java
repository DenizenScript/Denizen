package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.block.Block;

public interface BlockData {

    void setBlock(Block block, boolean physics);

    String toCompressedFormat();

    CompoundTag getCompoundTag();

    void setCompoundTag(CompoundTag tag);

    Material getMaterial();

    void setMaterial(Material material);

    byte getData();

    void setData(byte data);
}

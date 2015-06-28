package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.utilities.jnbt.CompoundTag;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.block.Block;

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
    }

    public void setBlock(Block block) {
        block.setTypeIdAndData(material.getId(), (byte) data, false);
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

    public CompoundTag getNBTTag() {
        return null;
    }

    public void setNBTTag(CompoundTag tag) {
        return;
    }
}

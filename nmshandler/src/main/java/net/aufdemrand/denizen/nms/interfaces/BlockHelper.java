package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.material.MaterialData;

public interface BlockHelper {

    MaterialData getFlowerpotContents(Block block);

    void setFlowerpotContents(Block block, MaterialData data);

    PlayerProfile getPlayerProfile(Skull skull);

    void setPlayerProfile(Skull skull, PlayerProfile playerProfile);

    CompoundTag getNbtData(Block block);

    void setNbtData(Block block, CompoundTag compoundTag);

    BlockData getBlockData(short id, byte data);

    BlockData getBlockData(Block block);

    BlockData getBlockData(String compressedString);

    boolean isSafeBlock(Material material);
}

package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.material.MaterialData;

import java.util.List;

public interface BlockHelper {


    void applyPhysics(Location location);

    int idFor(Material mat);

    MaterialData getFlowerpotContents(Block block);

    void setFlowerpotContents(Block block, MaterialData data);

    PlayerProfile getPlayerProfile(Skull skull);

    void setPlayerProfile(Skull skull, PlayerProfile playerProfile);

    CompoundTag getNbtData(Block block);

    void setNbtData(Block block, CompoundTag compoundTag);

    BlockData getBlockData(Material material, byte data);

    BlockData getBlockData(Block block);

    default BlockData getBlockData(ModernBlockData data) {
        return null;
    }

    BlockData getBlockData(String compressedString);

    boolean hasBlock(Material material);

    boolean setBlockResistance(Material material, float resistance);

    float getBlockResistance(Material material);

    default boolean isSafeBlock(Location loc) {
        return loc.getBlockY() < 0 || loc.getBlockY() > 255 || isSafeBlock(loc.getBlock().getType());
    }

    boolean isSafeBlock(Material material);

    default BlockState generateBlockState(Material mat) {
        return null;
    }

    List<Location> getBlocksList(PortalCreateEvent event);

    default String getPushReaction(Material mat) {
        throw new UnsupportedOperationException();
    }

    default void setPushReaction(Material mat, String reaction) {
        throw new UnsupportedOperationException();
    }

    default float getBlockStength(Material mat) {
        throw new UnsupportedOperationException();
    }

    default void setBlockStrength(Material mat, float strength) {
        throw new UnsupportedOperationException();
    }

    default ModernBlockData parseBlockData(String text) {
        int openBracket = text.indexOf('[');
        String material = text;
        String otherData = null;
        if (openBracket > 0) {
            material = text.substring(0, openBracket);
            otherData = text.substring(openBracket);
        }
        if (material.startsWith("minecraft:")) {
            material = material.substring("minecraft:".length());
        }
        return parseBlockData(Material.getMaterial(material.toUpperCase()), otherData);
    }

    default ModernBlockData parseBlockData(Material material, String otherData) {
        throw new UnsupportedOperationException();
    }
}

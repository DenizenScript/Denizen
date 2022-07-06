package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.objects.EntityTag;
import org.bukkit.Color;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public interface BlockHelper {


    void applyPhysics(Location location);

    PlayerProfile getPlayerProfile(Skull skull);

    void setPlayerProfile(Skull skull, PlayerProfile playerProfile);

    CompoundTag getNbtData(Block block);

    void setNbtData(Block block, CompoundTag compoundTag);

    boolean setBlockResistance(Material material, float resistance);

    float getBlockResistance(Material material);

    BlockState generateBlockState(Block block, Material mat);

    List<Location> getBlocksList(PortalCreateEvent event);

    String getPushReaction(Material mat);

    void setPushReaction(Material mat, String reaction);

    float getBlockStength(Material mat);

    void setBlockStrength(Material mat, float strength);

    static String getMaterialNameFromBlockData(String text) {
        int openBracket = text.indexOf('[');
        String material = text;
        if (openBracket > 0) {
            material = text.substring(0, openBracket);
        }
        if (material.startsWith("minecraft:")) {
            material = material.substring("minecraft:".length());
        }
        return material;
    }

    default BlockData parseBlockData(String text) {
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

    BlockData parseBlockData(Material material, String otherData);

    default void makeBlockStateRaw(BlockState state) {
        throw new UnsupportedOperationException();
    }

    default void doRandomTick(Location location) {
        throw new UnsupportedOperationException();
    }

    default Instrument getInstrumentFor(Material mat) {
        throw new UnsupportedOperationException();
    }

    default void ringBell(Bell block) {
        throw new UnsupportedOperationException();
    }

    default int getExpDrop(Block block, ItemStack item) {
        throw new UnsupportedOperationException();
    }

    default void setSpawnerCustomRules(CreatureSpawner spawner, int skyMin, int skyMax, int blockMin, int blockMax) {
        throw new UnsupportedOperationException();
    }

    default void setSpawnerSpawnedType(CreatureSpawner spawner, EntityTag entity) {
        spawner.setSpawnedType(entity.getBukkitEntityType());
    }

    default Color getMapColor(Block block) {
        throw new UnsupportedOperationException();
    }

    default void setVanillaTags(Material material, Set<String> tags) {
        throw new UnsupportedOperationException();
    }

}

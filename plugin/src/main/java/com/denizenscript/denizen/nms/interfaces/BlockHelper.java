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
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public interface BlockHelper {


    void applyPhysics(Location location);

    PlayerProfile getPlayerProfile(Skull skull);

    void setPlayerProfile(Skull skull, PlayerProfile playerProfile);

    CompoundTag getNbtData(Block block);

    void setNbtData(Block block, CompoundTag compoundTag);

    boolean setBlockResistance(Material material, float resistance);

    float getBlockResistance(Material material);

    enum PistonPushReaction {
        NORMAL, DESTROY, BLOCK, IGNORE, PUSH_ONLY;
        public static final PistonPushReaction[] VALUES = values();
    }

    default PistonPushReaction getPushReaction(Material mat) { // TODO: once minimum version is 1.19, remove from NMS
        return PistonPushReaction.VALUES[mat.createBlockData().getPistonMoveReaction().ordinal()];
    }

    void setPushReaction(Material mat, PistonPushReaction reaction);

    float getBlockStrength(Material mat);

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
        return Material.matchMaterial(material).createBlockData(otherData);
    }

    void makeBlockStateRaw(BlockState state);

    void doRandomTick(Location location);

    Instrument getInstrumentFor(Material mat);

    default void ringBell(Bell bell) { /// TODO: once minimum version is 1.19, remove from NMS
        bell.ring();
    }

    int getExpDrop(Block block, ItemStack item);

    default void setSpawnerCustomRules(CreatureSpawner spawner, int skyMin, int skyMax, int blockMin, int blockMax) {
        throw new UnsupportedOperationException();
    }

    default void setSpawnerSpawnedType(CreatureSpawner spawner, EntityTag entity) {
        spawner.setSpawnedType(entity.getBukkitEntityType());
    }

    default Color getMapColor(Block block) { // TODO: once 1.20 is the minimum supported version, remove from NMS
        return block.getBlockData().getMapColor();
    }

    default void setVanillaTags(Material material, Set<String> tags) {
        throw new UnsupportedOperationException();
    }
}

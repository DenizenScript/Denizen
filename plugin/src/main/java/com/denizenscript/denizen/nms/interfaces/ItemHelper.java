package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class ItemHelper {

    public abstract String getInternalNameFromMaterial(Material material);

    public abstract Material getMaterialFromInternalName(String internalName);

    public abstract String getJsonString(ItemStack itemStack);

    public abstract PlayerProfile getSkullSkin(ItemStack itemStack);

    public abstract ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    public abstract ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    public abstract CompoundTag getNbtData(ItemStack itemStack);

    public abstract ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    public abstract PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color, boolean icon);

    public void setInventoryItem(Inventory inventory, ItemStack item, int slot) {
        inventory.setItem(slot, item);
    }
}

package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class ItemHelper {

    public abstract Recipe getRecipeById(NamespacedKey key);

    public abstract void removeRecipe(NamespacedKey key);

    public abstract void clearDenizenRecipes();

    public void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack ingredient, boolean exact) {
        throw new UnsupportedOperationException();
    }

    public abstract void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack ingredient, float exp, int time, String type, boolean exact);

    public abstract void registerShapelessRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredients, boolean[] exact);

    public abstract void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack item, boolean exact);

    public abstract String getInternalNameFromMaterial(Material material);

    public abstract Material getMaterialFromInternalName(String internalName);

    public abstract String getJsonString(ItemStack itemStack);

    public abstract String getRawHoverText(ItemStack itemStack);

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

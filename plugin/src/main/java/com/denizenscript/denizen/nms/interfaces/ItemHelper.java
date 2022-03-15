package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.IntArrayTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.objects.ItemTag;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public abstract class ItemHelper {

    public void setMaxStackSize(Material material, int size) {
        throw new UnsupportedOperationException();
    }

    public abstract Integer burnTime(Material material);

    public abstract Recipe getRecipeById(NamespacedKey key);

    public abstract void removeRecipe(NamespacedKey key);

    public abstract void clearDenizenRecipes();

    public void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, boolean exact) {
        throw new UnsupportedOperationException();
    }

    public abstract void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, float exp, int time, String type, boolean exact);

    public abstract void registerShapelessRecipe(String keyName, String group, ItemStack result, List<ItemStack[]> ingredients, boolean[] exact);

    public abstract void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack[] item, boolean exact);

    public abstract String getInternalNameFromMaterial(Material material);

    public abstract Material getMaterialFromInternalName(String internalName);

    public abstract String getJsonString(ItemStack itemStack);

    public abstract String getRawHoverText(ItemStack itemStack);

    public abstract PlayerProfile getSkullSkin(ItemStack itemStack);

    public abstract ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    public abstract ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    public abstract CompoundTag getNbtData(ItemStack itemStack);

    public abstract ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    public void registerSmithingRecipe(String keyName, ItemStack result, ItemStack[] baseItem, boolean baseExact, ItemStack[] upgradeItem, boolean upgradeExact) {
        throw new UnsupportedOperationException();
    }

    public void setInventoryItem(Inventory inventory, ItemStack item, int slot) {
        inventory.setItem(slot, item);
    }

    public IntArrayTag convertUuidToNbt(UUID id) {
        throw new UnsupportedOperationException();
    }

    public UUID convertNbtToUuid(IntArrayTag id) {
        throw new UnsupportedOperationException();
    }

    public String getDisplayName(ItemTag item) {
        if (!item.getItemMeta().hasDisplayName()) {
            return null;
        }
        return item.getItemMeta().getDisplayName();
    }

    public List<String> getLore(ItemTag item) {
        if (!item.getItemMeta().hasLore()) {
            return null;
        }
        return item.getItemMeta().getLore();
    }

    public void setDisplayName(ItemTag item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public void setLore(ItemTag item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public boolean renderEntireMap(int mapId, int xMin, int zMin, int xMax, int zMax) {
        throw new UnsupportedOperationException();
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributes(ItemStack item, org.bukkit.inventory.EquipmentSlot slot) {
        throw new UnsupportedOperationException();
    }

    public BlockData getPlacedBlock(Material material) {
        throw new UnsupportedOperationException();
    }
}

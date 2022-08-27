package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.IntArrayTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.objects.ItemTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.UUID;

public abstract class ItemHelper {

    public abstract void setMaxStackSize(Material material, int size);

    public abstract Integer burnTime(Material material);

    public abstract void removeRecipe(NamespacedKey key);

    public abstract void clearDenizenRecipes();

    public abstract void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, boolean exact);

    public abstract void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, float exp, int time, String type, boolean exact);

    public abstract void registerShapelessRecipe(String keyName, String group, ItemStack result, List<ItemStack[]> ingredients, boolean[] exact);

    public abstract void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack[] item, boolean exact);

    public abstract String getJsonString(ItemStack itemStack);

    public abstract String getRawHoverText(ItemStack itemStack);

    public abstract PlayerProfile getSkullSkin(ItemStack itemStack);

    public abstract ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    public abstract ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    public abstract CompoundTag getNbtData(ItemStack itemStack);

    public abstract ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    public abstract void registerSmithingRecipe(String keyName, ItemStack result, ItemStack[] baseItem, boolean baseExact, ItemStack[] upgradeItem, boolean upgradeExact);

    public abstract void setInventoryItem(Inventory inventory, ItemStack item, int slot);

    public abstract IntArrayTag convertUuidToNbt(UUID id);

    public abstract UUID convertNbtToUuid(IntArrayTag id);

    public abstract String getDisplayName(ItemTag item);

    public abstract List<String> getLore(ItemTag item);

    public abstract void setDisplayName(ItemTag item, String name);

    public abstract void setLore(ItemTag item, List<String> lore);

    public boolean renderEntireMap(int mapId, int xMin, int zMin, int xMax, int zMax) {
        throw new UnsupportedOperationException();
    }

    public BlockData getPlacedBlock(Material material) {
        throw new UnsupportedOperationException();
    }

    public abstract boolean isValidMix(ItemStack input, ItemStack ingredient);
}

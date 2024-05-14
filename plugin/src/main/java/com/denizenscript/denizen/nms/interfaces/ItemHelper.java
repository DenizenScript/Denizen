package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.IntArrayTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.objects.ItemTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.map.MapView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ItemHelper {

    public abstract void setMaxStackSize(Material material, int size);

    public abstract Integer burnTime(Material material);

    public abstract void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, boolean exact);

    public abstract void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, float exp, int time, String type, boolean exact, String category);

    public abstract void registerShapelessRecipe(String keyName, String group, ItemStack result, List<ItemStack[]> ingredients, boolean[] exact, String category);

    public abstract void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack[] item, boolean exact);

    public abstract String getJsonString(ItemStack itemStack);

    public abstract String getRawHoverText(ItemStack itemStack);

    public abstract PlayerProfile getSkullSkin(ItemStack itemStack);

    public abstract ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    public abstract ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    public abstract CompoundTag getNbtData(ItemStack itemStack);

    public abstract ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    public CompoundTag getEntityData(ItemStack item) { // TODO: once 1.20 is the minimum supported version, remove default impl
        CompoundTag nbt = getNbtData(item);
        return nbt != null && nbt.getValue().get("EntityTag") instanceof CompoundTag entityNbt ? entityNbt : null;
    }

    public ItemStack setEntityData(ItemStack item, CompoundTag entityNbt, EntityType entityType) { // TODO: once 1.20 is the minimum supported version, remove default impl
        boolean shouldRemove = entityNbt == null || entityNbt.isEmpty();
        CompoundTag nbt = getNbtData(item);
        if (shouldRemove && !nbt.containsKey("EntityTag")) {
            return item;
        }
        if (shouldRemove) {
            nbt = nbt.createBuilder().remove("EntityTag").build();
        }
        else {
            nbt = nbt.createBuilder().put("EntityTag", entityNbt).build();
        }
        return setNbtData(item, nbt);
    }

    public abstract void registerSmithingRecipe(String keyName, ItemStack result, ItemStack[] baseItem, boolean baseExact, ItemStack[] upgradeItem, boolean upgradeExact, ItemStack[] templateItem, boolean templateExact);

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

    public record BrewingRecipe(RecipeChoice input, RecipeChoice ingredient, ItemStack result) {}

    public Map<NamespacedKey, BrewingRecipe> getCustomBrewingRecipes() {
        throw new UnsupportedOperationException();
    }

    public byte[] renderMap(MapView mapView, Player player) {
        throw new UnsupportedOperationException();
    }

    public int getFoodPoints(Material itemType) {
        throw new UnsupportedOperationException();
    }
}

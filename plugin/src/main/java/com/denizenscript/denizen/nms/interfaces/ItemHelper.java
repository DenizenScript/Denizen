package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.google.gson.JsonObject;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.map.MapView;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ItemHelper {

    public abstract void setMaxStackSize(Material material, int size);

    public abstract Integer burnTime(Material material);

    public abstract void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, boolean exact);

    public abstract void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, float exp, int time, String type, boolean exact, String category);

    public abstract void registerShapelessRecipe(String keyName, String group, ItemStack result, List<ItemStack[]> ingredients, boolean[] exact, String category);

    public abstract void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack[] item, boolean exact);

    public abstract String getJsonString(ItemStack itemStack);

    public String getLegacyHoverNbt(ItemTag item) { // TODO: once 1.20 is the minimum supported version, remove this
        return item.getItemMeta().getAsString();
    }

    public JsonObject getRawHoverComponentsJson(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    public ItemStack applyRawHoverComponentsJson(ItemStack item, JsonObject components) {
        throw new UnsupportedOperationException();
    }

    public abstract PlayerProfile getSkullSkin(ItemStack itemStack);

    public abstract ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    public abstract ItemStack addNbtData(ItemStack itemStack, String key, BinaryTag value);

    public abstract CompoundBinaryTag getNbtData(ItemStack itemStack);

    public abstract ItemStack setNbtData(ItemStack itemStack, CompoundBinaryTag compoundTag);

    public CompoundBinaryTag getCustomData(ItemStack item) { // TODO: once 1.20 is the minimum supported version, remove default impl
        return getNbtData(item);
    }

    public ItemStack setCustomData(ItemStack item, CompoundBinaryTag data) { // TODO: once 1.20 is the minimum supported version, remove default impl
        return setNbtData(item, data);
    }

    public ItemStack setPartialOldNbt(ItemStack item, CompoundBinaryTag oldTag) {
        throw new UnsupportedOperationException();
    }

    public CompoundBinaryTag getEntityData(ItemStack item) { // TODO: once 1.20 is the minimum supported version, remove default impl
        return getNbtData(item).getCompound("EntityTag", null);
    }

    public ItemStack setEntityData(ItemStack item, CompoundBinaryTag entityNbt, EntityType entityType) { // TODO: once 1.20 is the minimum supported version, remove default impl
        boolean shouldRemove = entityNbt == null || entityNbt.isEmpty();
        CompoundBinaryTag nbt = getNbtData(item);
        // TODO: adventure-nbt: contains
        if (shouldRemove && !nbt.keySet().contains("EntityTag")) {
            return item;
        }
        if (shouldRemove) {
            nbt = nbt.remove("EntityTag");
        }
        else {
            nbt = nbt.put("EntityTag", entityNbt);
        }
        return setNbtData(item, nbt);
    }

    public List<Material> getCanPlaceOn(ItemStack item) { // TODO: once 1.20 is the minimum supported version, remove default impl
        return CustomNBT.getNBTMaterials(item, CustomNBT.KEY_CAN_PLACE_ON);
    }

    public ItemStack setCanPlaceOn(ItemStack item, List<Material> canPlaceOn) { // TODO: once 1.20 is the minimum supported version, remove default impl
        if (canPlaceOn == null) {
            return CustomNBT.clearNBT(item, CustomNBT.KEY_CAN_PLACE_ON);
        }
        return CustomNBT.setNBTMaterials(item, CustomNBT.KEY_CAN_PLACE_ON, canPlaceOn);
    }

    public List<Material> getCanBreak(ItemStack item) { // TODO: once 1.20 is the minimum supported version, remove default impl
        return CustomNBT.getNBTMaterials(item, CustomNBT.KEY_CAN_DESTROY);
    }

    public ItemStack setCanBreak(ItemStack item, List<Material> canBreak) { // TODO: once 1.20 is the minimum supported version, remove default impl
        if (canBreak == null) {
            return CustomNBT.clearNBT(item, CustomNBT.KEY_CAN_DESTROY);
        }
        return CustomNBT.setNBTMaterials(item, CustomNBT.KEY_CAN_DESTROY, canBreak);
    }

    public MapTag getRawComponentsPatch(ItemStack item, boolean excludeHandled) {
        throw new UnsupportedOperationException();
    }

    public ItemStack setRawComponentsPatch(ItemStack item, MapTag rawComponentsMap, int dataVersion, Consumer<String> errorHandler) {
        throw new UnsupportedOperationException();
    }

    public abstract void registerSmithingRecipe(String keyName, ItemStack result, ItemStack[] baseItem, boolean baseExact, ItemStack[] upgradeItem, boolean upgradeExact, ItemStack[] templateItem, boolean templateExact);

    public abstract void setInventoryItem(Inventory inventory, ItemStack item, int slot);

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

    public DyeColor getShieldColor(ItemStack item) { // TODO: once 1.21 is the minimum supported version, remove from NMS
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
            return ((ShieldMeta) item.getItemMeta()).getBaseColor();
        }
        // TODO: once 1.20 is the minimum supported version, remove legacy code ↓
        BlockStateMeta stateMeta = (BlockStateMeta) item.getItemMeta();
        return stateMeta.hasBlockState() ? ((Banner) stateMeta.getBlockState()).getBaseColor() : null;
    }

    public ItemStack setShieldColor(ItemStack item, DyeColor color) { // TODO: once 1.21 is the minimum supported version, remove from NMS
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
            ShieldMeta shieldMeta = (ShieldMeta) item.getItemMeta();
            shieldMeta.setBaseColor(color);
            item.setItemMeta(shieldMeta);
            return item;
        }
        // TODO: once 1.20 is the minimum supported version, remove legacy code ↓
        if (color == null) {
            CompoundBinaryTag noStateNbt = getNbtData(item).remove("BlockEntityTag");
            return setNbtData(item, noStateNbt);
        }
        BlockStateMeta stateMeta = (BlockStateMeta) item.getItemMeta();
        Banner banner = (Banner) stateMeta.getBlockState();
        banner.setBaseColor(color);
        stateMeta.setBlockState(banner);
        item.setItemMeta(stateMeta);
        return item;
    }

    public void blockRecipeFinalization() {
    }

    public void restoreRecipeFinalization() {
    }

    public void removeRecipes(List<NamespacedKey> keys) {
    }

    public void registerOtherRecipe(org.bukkit.inventory.Recipe recipe) {
    }
}

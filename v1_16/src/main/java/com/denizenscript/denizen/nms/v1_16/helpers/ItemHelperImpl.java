package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.v1_16.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemHelperImpl extends ItemHelper {

    public static IRecipe<?> getNMSRecipe(NamespacedKey key) {
        MinecraftKey nmsKey = CraftNamespacedKey.toMinecraft(key);
        for (Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>> recipeMap : ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().recipes.values()) {
            IRecipe<?> recipe = recipeMap.get(nmsKey);
            if (recipe != null) {
                return recipe;
            }
        }
        return null;
    }

    public void setMaxStackSize(Material material, int size) {
        try {
            ReflectionHelper.getFinalSetter(Material.class, "maxStack").invoke(material, size);
            ReflectionHelper.getFinalSetter(Item.class, "maxStackSize").invoke(IRegistry.ITEM.get(CraftNamespacedKey.toMinecraft(material.getKey())), size);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public Integer burnTime(Material material) {
        return TileEntityFurnace.f().get(CraftMagicNumbers.getItem(material));
    }

    @Override
    public void removeRecipe(NamespacedKey key) {
        MinecraftKey nmsKey = CraftNamespacedKey.toMinecraft(key);
        for (Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>> recipeMap : ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().recipes.values()) {
            recipeMap.remove(nmsKey);
        }
    }

    @Override
    public void clearDenizenRecipes() {
        for (Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>> recipeMap : ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().recipes.values()) {
            for (MinecraftKey key : new ArrayList<>(recipeMap.keySet())) {
                if (key.getNamespace().equalsIgnoreCase("denizen")) {
                    recipeMap.remove(key);
                }
            }
        }
    }

    @Override
    public void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack[] item, boolean exact) {
        if (item.length == 1 && item[0].getType() == Material.AIR) {
            recipe.setIngredient(c, new RecipeChoice.MaterialChoice(Material.AIR));
        }
        else if (exact) {
            recipe.setIngredient(c, new RecipeChoice.ExactChoice(item));
        }
        else {
            Material[] mats = new Material[item.length];
            for (int i = 0; i < item.length; i++) {
                mats[i] = item[i].getType();
            }
            recipe.setIngredient(c, new RecipeChoice.MaterialChoice(mats));
        }
    }

    public static RecipeItemStack itemArrayToRecipe(ItemStack[] items, boolean exact) {
        RecipeItemStack.StackProvider[] stacks = new RecipeItemStack.StackProvider[items.length];
        for (int i = 0; i < items.length; i++) {
            stacks[i] = new RecipeItemStack.StackProvider(CraftItemStack.asNMSCopy(items[i]));
        }
        RecipeItemStack itemRecipe = new RecipeItemStack(Arrays.stream(stacks));
        itemRecipe.exact = exact;
        return itemRecipe;
    }

    @Override
    public void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, float exp, int time, String type, boolean exact) {
        MinecraftKey key = new MinecraftKey("denizen", keyName);
        RecipeItemStack itemRecipe = itemArrayToRecipe(ingredient, exact);
        RecipeCooking recipe;
        if (type.equalsIgnoreCase("smoker")) {
            recipe = new RecipeSmoking(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else if (type.equalsIgnoreCase("blast")) {
            recipe = new RecipeBlasting(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else if (type.equalsIgnoreCase("campfire")) {
            recipe = new RecipeCampfire(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else {
            recipe = new FurnaceRecipe(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().addRecipe(recipe);
    }

    @Override
    public void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, boolean exact) {
        MinecraftKey key = new MinecraftKey("denizen", keyName);
        RecipeItemStack itemRecipe = itemArrayToRecipe(ingredient, exact);
        RecipeStonecutting recipe = new RecipeStonecutting(key, group, itemRecipe, CraftItemStack.asNMSCopy(result));
        ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().addRecipe(recipe);
    }

    @Override
    public void registerSmithingRecipe(String keyName, ItemStack result, ItemStack[] baseItem, boolean baseExact, ItemStack[] upgradeItem, boolean upgradeExact) {
        MinecraftKey key = new MinecraftKey("denizen", keyName);
        RecipeItemStack baseItemRecipe = itemArrayToRecipe(baseItem, baseExact);
        RecipeItemStack upgradeItemRecipe = itemArrayToRecipe(upgradeItem, upgradeExact);
        RecipeSmithing recipe = new RecipeSmithing(key, baseItemRecipe, upgradeItemRecipe, CraftItemStack.asNMSCopy(result));
        ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().addRecipe(recipe);
    }

    @Override
    public void registerShapelessRecipe(String keyName, String group, ItemStack result, List<ItemStack[]> ingredients, boolean[] exact) {
        MinecraftKey key = new MinecraftKey("denizen", keyName);
        ArrayList<RecipeItemStack> ingredientList = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientList.add(itemArrayToRecipe(ingredients.get(i), exact[i]));
        }
        ShapelessRecipes recipe = new ShapelessRecipes(key, group, CraftItemStack.asNMSCopy(result), NonNullList.a(null, ingredientList.toArray(new RecipeItemStack[0])));
        ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().addRecipe(recipe);
    }

    @Override
    public String getJsonString(ItemStack itemStack) {
        String json = CraftItemStack.asNMSCopy(itemStack).C().getChatModifier().toString().replace("\\", "\\\\").replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public String getRawHoverText(ItemStack itemStack) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getTag();
        if (tag == null) {
            return null;
        }
        return tag.toString();
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.server.v1_16_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
        if (itemStack.hasTag()) {
            NBTTagCompound tag = itemStack.getTag();
            if (tag.hasKeyOfType("SkullOwner", 10)) {
                GameProfile profile = GameProfileSerializer.deserialize(tag.getCompound("SkullOwner"));
                if (profile != null) {
                    Property property = Iterables.getFirst(profile.getProperties().get("textures"), null);
                    return new PlayerProfile(profile.getName(), profile.getId(),
                            property != null ? property.getValue() : null,
                            property != null ? property.getSignature() : null);
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile) {
        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        if (playerProfile.hasTexture()) {
            gameProfile.getProperties().get("textures").clear();
            if (playerProfile.getTextureSignature() != null) {
                gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
            }
            else {
                gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture()));
            }
        }
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        tag.set("SkullOwner", GameProfileSerializer.serialize(new NBTTagCompound(), gameProfile));
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        CompoundTag compound = CompoundTagImpl.fromNMSTag(tag).createBuilder().put(key, value).build();
        nmsItemStack.setTag(((CompoundTagImpl) compound).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && nmsItemStack.hasTag()) {
            return CompoundTagImpl.fromNMSTag(nmsItemStack.getTag());
        }
        return new CompoundTagImpl(new HashMap<>());
    }

    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(((CompoundTagImpl) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public void setInventoryItem(Inventory inventory, ItemStack item, int slot) {
        if (inventory instanceof CraftInventoryPlayer && ((CraftInventoryPlayer) inventory).getInventory().player == null) {
            ((CraftInventoryPlayer) inventory).getInventory().setItem(slot, CraftItemStack.asNMSCopy(item));
        }
        else {
            inventory.setItem(slot, item);
        }
    }

    @Override
    public IntArrayTag convertUuidToNbt(UUID id) {
        return new IntArrayTag(GameProfileSerializer.a(id).getInts());
    }

    @Override
    public UUID convertNbtToUuid(IntArrayTag id) {
        return GameProfileSerializer.a(new NBTTagIntArray(id.getValue()));
    }

    @Override
    public String getDisplayName(ItemTag item) {
        if (!item.getItemMeta().hasDisplayName()) {
            return null;
        }
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        String jsonText = ((NBTTagCompound) nmsItemStack.getTag().get("display")).getString("Name");
        BaseComponent[] nameComponent = ComponentSerializer.parse(jsonText);
        return FormattedTextHelper.stringify(nameComponent);
    }

    @Override
    public List<String> getLore(ItemTag item) {
        if (!item.getItemMeta().hasLore()) {
            return null;
        }
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        NBTTagList list = ((NBTTagCompound) nmsItemStack.getTag().get("display")).getList("Lore", 8);
        List<String> outList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            BaseComponent[] lineComponent = ComponentSerializer.parse(list.getString(i));
            outList.add(FormattedTextHelper.stringify(lineComponent));
        }
        return outList;
    }

    @Override
    public void setDisplayName(ItemTag item, String name) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        NBTTagCompound tag = nmsItemStack.getOrCreateTag();
        NBTTagCompound display = tag.getCompound("display");
        if (!tag.hasKey("display")) {
            tag.set("display", display);
        }
        if (name == null || name.isEmpty()) {
            display.set("Name", null);
            return;
        }
        BaseComponent[] components = FormattedTextHelper.parse(name, ChatColor.WHITE);
        display.set("Name", NBTTagString.a(ComponentSerializer.toString(components)));
        item.setItemStack(CraftItemStack.asBukkitCopy(nmsItemStack));
    }

    @Override
    public void setLore(ItemTag item, List<String> lore) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        NBTTagCompound tag = nmsItemStack.getOrCreateTag();
        NBTTagCompound display = tag.getCompound("display");
        if (!tag.hasKey("display")) {
            tag.set("display", display);
        }
        if (lore == null || lore.isEmpty()) {
            display.set("Lore", null);
        }
        else {
            NBTTagList tagList = new NBTTagList();
            for (String line : lore) {
                tagList.add(NBTTagString.a(ComponentSerializer.toString(FormattedTextHelper.parse(line, ChatColor.WHITE))));
            }
            display.set("Lore", tagList);
        }
        item.setItemStack(CraftItemStack.asBukkitCopy(nmsItemStack));
    }

    @Override
    public boolean isValidMix(ItemStack input, ItemStack ingredient) {
        net.minecraft.server.v1_16_R3.ItemStack nmsInput = CraftItemStack.asNMSCopy(input);
        net.minecraft.server.v1_16_R3.ItemStack nmsIngredient = CraftItemStack.asNMSCopy(ingredient);
        return net.minecraft.server.v1_16_R3.PotionBrewer.a(nmsInput, nmsIngredient);
    }
}

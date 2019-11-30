package com.denizenscript.denizen.nms.v1_13.helpers;

import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.v1_13.impl.jnbt.CompoundTagImpl;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ItemHelperImpl extends ItemHelper {

    @Override
    public Recipe getRecipeById(NamespacedKey key) {
        MinecraftKey nmsKey = CraftNamespacedKey.toMinecraft(key);
        Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe> recipeMap = ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().recipes;
        IRecipe recipe = recipeMap.get(nmsKey);
        if (recipe != null) {
            return recipe.toBukkitRecipe();
        }
        return null;
    }

    @Override
    public void removeRecipe(NamespacedKey key) {
        MinecraftKey nmsKey = CraftNamespacedKey.toMinecraft(key);
        Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe> recipeMap = ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().recipes;
        recipeMap.remove(nmsKey);
    }

    @Override
    public void clearDenizenRecipes() {
        Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe> recipeMap = ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().recipes;
        for (MinecraftKey key : new ArrayList<>(recipeMap.keySet())) {
            if (key.b().equalsIgnoreCase("denizen")) {
                recipeMap.remove(key);
            }
        }
    }

    @Override
    public void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack item, boolean exact) {
        if (exact) {
            recipe.setIngredient(c, new RecipeChoice.ExactChoice(item));
        }
        else {
            recipe.setIngredient(c, item.getType());
        }
    }

    @Override
    public void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack ingredient, float exp, int time, String type, boolean exact) {
        MinecraftKey key = new MinecraftKey("denizen", keyName);
        RecipeItemStack itemRecipe = new RecipeItemStack(Arrays.asList(new RecipeItemStack.StackProvider(CraftItemStack.asNMSCopy(ingredient))).stream());
        itemRecipe.exact = exact;
        FurnaceRecipe recipe = new FurnaceRecipe(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().a(recipe);
    }

    @Override
    public void registerShapelessRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredients, boolean[] exact) {
        MinecraftKey key = new MinecraftKey("denizen", keyName);
        ArrayList<RecipeItemStack> ingredientList = new ArrayList<>();
        for (int i = 0; i < ingredients.length; i++) {
            RecipeItemStack itemRecipe = new RecipeItemStack(Arrays.asList(new RecipeItemStack.StackProvider(CraftItemStack.asNMSCopy(ingredients[i]))).stream());
            itemRecipe.exact = exact[i];
            ingredientList.add(itemRecipe);
        }
        ShapelessRecipes recipe = new ShapelessRecipes(key, group, CraftItemStack.asNMSCopy(result), NonNullList.a(null, ingredientList.toArray(new RecipeItemStack[ingredientList.size()])));
        ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager().a(recipe);
    }

    @Override
    public String getInternalNameFromMaterial(Material material) {
        // In 1.13+ Material names match their internal name
        return "minecraft:" + CoreUtilities.toLowerCase(material.name());
    }

    @Override
    public Material getMaterialFromInternalName(String internalName) {
        return Material.matchMaterial(internalName);
    }

    @Override
    public String getJsonString(ItemStack itemStack) {
        String json = CraftItemStack.asNMSCopy(itemStack).A().getChatModifier().toString().replace("\\", "\\\\").replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public String getRawHoverText(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return "";
        }
        return CraftItemStack.asNMSCopy(itemStack).A().getChatModifier().i().b().getText();
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.server.v1_13_R2.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
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
        net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        tag.set("SkullOwner", GameProfileSerializer.serialize(new NBTTagCompound(), gameProfile));
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        CompoundTag compound = CompoundTagImpl.fromNMSTag(tag).createBuilder().put(key, value).build();
        nmsItemStack.setTag(((CompoundTagImpl) compound).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && nmsItemStack.hasTag()) {
            return CompoundTagImpl.fromNMSTag(nmsItemStack.getTag());
        }
        return new CompoundTagImpl(new HashMap<>());
    }

    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.server.v1_13_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(((CompoundTagImpl) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color, boolean icon) {
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }
}

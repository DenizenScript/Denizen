package com.denizenscript.denizen.nms.v1_15.helpers;

import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.v1_15.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
            ReflectionHelper.getFinalSetter(Item.class, "maxStackSize").invoke(IRegistry.ITEM.get(MinecraftKey.a(material.getKey().getKey())), size);
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
    public Recipe getRecipeById(NamespacedKey key) {
        IRecipe<?> recipe = getNMSRecipe(key);
        if (recipe == null) {
            return null;
        }
        return recipe.toBukkitRecipe();
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
        if (exact) {
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
        String json = CraftItemStack.asNMSCopy(itemStack).B().getChatModifier().toString().replace("\\", "\\\\").replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public String getRawHoverText(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return "";
        }
        return CraftItemStack.asNMSCopy(itemStack).B().getChatModifier().getHoverEvent().b().getText();
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.server.v1_15_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
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
        net.minecraft.server.v1_15_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        tag.set("SkullOwner", GameProfileSerializer.serialize(new NBTTagCompound(), gameProfile));
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.server.v1_15_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        CompoundTag compound = CompoundTagImpl.fromNMSTag(tag).createBuilder().put(key, value).build();
        nmsItemStack.setTag(((CompoundTagImpl) compound).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.server.v1_15_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && nmsItemStack.hasTag()) {
            return CompoundTagImpl.fromNMSTag(nmsItemStack.getTag());
        }
        return new CompoundTagImpl(new HashMap<>());
    }

    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.server.v1_15_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(((CompoundTagImpl) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
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
}

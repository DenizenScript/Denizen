package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizen.nms.v1_17.Handler;
import com.denizenscript.denizen.nms.v1_17.ReflectionMappingsInfo;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.nms.v1_17.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ItemHelperImpl extends ItemHelper {

    public static net.minecraft.world.item.crafting.Recipe<?> getNMSRecipe(NamespacedKey key) {
        ResourceLocation nmsKey = CraftNamespacedKey.toMinecraft(key);
        for (Object2ObjectLinkedOpenHashMap<ResourceLocation, net.minecraft.world.item.crafting.Recipe<?>> recipeMap : ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().recipes.values()) {
            net.minecraft.world.item.crafting.Recipe<?> recipe = recipeMap.get(nmsKey);
            if (recipe != null) {
                return recipe;
            }
        }
        return null;
    }

    public void setMaxStackSize(Material material, int size) {
        try {
            ReflectionHelper.getFinalSetter(Material.class, "maxStack").invoke(material, size);
            ReflectionHelper.getFinalSetter(Item.class, ReflectionMappingsInfo.Item_maxStackSize).invoke(Registry.ITEM.get(ResourceLocation.tryParse(material.getKey().getKey())), size);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public Integer burnTime(Material material) {
        return AbstractFurnaceBlockEntity.getFuel().get(CraftMagicNumbers.getItem(material));
    }

    @Override
    public Recipe getRecipeById(NamespacedKey key) {
        net.minecraft.world.item.crafting.Recipe<?> recipe = getNMSRecipe(key);
        if (recipe == null) {
            return null;
        }
        return recipe.toBukkitRecipe();
    }

    @Override
    public void removeRecipe(NamespacedKey key) {
        ResourceLocation nmsKey = CraftNamespacedKey.toMinecraft(key);
        for (Object2ObjectLinkedOpenHashMap<ResourceLocation, net.minecraft.world.item.crafting.Recipe<?>> recipeMap : ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().recipes.values()) {
            recipeMap.remove(nmsKey);
        }
    }

    @Override
    public void clearDenizenRecipes() {
        for (Object2ObjectLinkedOpenHashMap<ResourceLocation, net.minecraft.world.item.crafting.Recipe<?>> recipeMap : ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().recipes.values()) {
            for (ResourceLocation key : new ArrayList<>(recipeMap.keySet())) {
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

    public static Ingredient itemArrayToRecipe(ItemStack[] items, boolean exact) {
        Ingredient.ItemValue[] stacks = new Ingredient.ItemValue[items.length];
        for (int i = 0; i < items.length; i++) {
            stacks[i] = new Ingredient.ItemValue(CraftItemStack.asNMSCopy(items[i]));
        }
        Ingredient itemRecipe = new Ingredient(Arrays.stream(stacks));
        itemRecipe.exact = exact;
        return itemRecipe;
    }

    @Override
    public void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, float exp, int time, String type, boolean exact) {
        ResourceLocation key = new ResourceLocation("denizen", keyName);
        Ingredient itemRecipe = itemArrayToRecipe(ingredient, exact);
        AbstractCookingRecipe recipe;
        if (type.equalsIgnoreCase("smoker")) {
            recipe = new SmokingRecipe(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else if (type.equalsIgnoreCase("blast")) {
            recipe = new BlastingRecipe(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else if (type.equalsIgnoreCase("campfire")) {
            recipe = new CampfireCookingRecipe(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else {
            recipe = new SmeltingRecipe(key, group, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().addRecipe(recipe);
    }

    @Override
    public void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, boolean exact) {
        ResourceLocation key = new ResourceLocation("denizen", keyName);
        Ingredient itemRecipe = itemArrayToRecipe(ingredient, exact);
        StonecutterRecipe recipe = new StonecutterRecipe(key, group, itemRecipe, CraftItemStack.asNMSCopy(result));
        ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().addRecipe(recipe);
    }

    @Override
    public void registerSmithingRecipe(String keyName, ItemStack result, ItemStack[] baseItem, boolean baseExact, ItemStack[] upgradeItem, boolean upgradeExact) {
        ResourceLocation key = new ResourceLocation("denizen", keyName);
        Ingredient baseItemRecipe = itemArrayToRecipe(baseItem, baseExact);
        Ingredient upgradeItemRecipe = itemArrayToRecipe(upgradeItem, upgradeExact);
        UpgradeRecipe recipe = new UpgradeRecipe(key, baseItemRecipe, upgradeItemRecipe, CraftItemStack.asNMSCopy(result));
        ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().addRecipe(recipe);
    }

    @Override
    public void registerShapelessRecipe(String keyName, String group, ItemStack result, List<ItemStack[]> ingredients, boolean[] exact) {
        ResourceLocation key = new ResourceLocation("denizen", keyName);
        ArrayList<Ingredient> ingredientList = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientList.add(itemArrayToRecipe(ingredients.get(i), exact[i]));
        }
        ShapelessRecipe recipe = new ShapelessRecipe(key, group, CraftItemStack.asNMSCopy(result), NonNullList.of(null, ingredientList.toArray(new Ingredient[0])));
        ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().addRecipe(recipe);
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
        String json = CraftItemStack.asNMSCopy(itemStack).getDisplayName().getStyle().toString().replace("\\", "\\\\").replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public String getRawHoverText(ItemStack itemStack) {
        net.minecraft.nbt.CompoundTag tag = CraftItemStack.asNMSCopy(itemStack).getTag();
        if (tag == null) {
            return null;
        }
        return tag.toString();
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
        if (itemStack.hasTag()) {
            net.minecraft.nbt.CompoundTag tag = itemStack.getTag();
            if (tag.contains("SkullOwner", 10)) {
                GameProfile profile = NbtUtils.readGameProfile(tag.getCompound("SkullOwner"));
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
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.nbt.CompoundTag tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new net.minecraft.nbt.CompoundTag();
        tag.put("SkullOwner", NbtUtils.writeGameProfile(new net.minecraft.nbt.CompoundTag(), gameProfile));
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.nbt.CompoundTag tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new net.minecraft.nbt.CompoundTag();
        CompoundTag compound = CompoundTagImpl.fromNMSTag(tag).createBuilder().put(key, value).build();
        nmsItemStack.setTag(((CompoundTagImpl) compound).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && nmsItemStack.hasTag()) {
            return CompoundTagImpl.fromNMSTag(nmsItemStack.getTag());
        }
        return new CompoundTagImpl(new HashMap<>());
    }

    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(((CompoundTagImpl) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color, boolean icon) {
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

    @Override
    public IntArrayTag convertUuidToNbt(UUID id) {
        return new IntArrayTag(NbtUtils.createUUID(id).getAsIntArray());
    }

    @Override
    public UUID convertNbtToUuid(IntArrayTag id) {
        return NbtUtils.loadUUID(new net.minecraft.nbt.IntArrayTag(id.getValue()));
    }

    @Override
    public String getDisplayName(ItemTag item) {
        if (!item.getItemMeta().hasDisplayName()) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        String jsonText = ((net.minecraft.nbt.CompoundTag) nmsItemStack.getTag().get("display")).getString("Name");
        BaseComponent[] nameComponent = ComponentSerializer.parse(jsonText);
        return FormattedTextHelper.stringify(nameComponent, ChatColor.WHITE);
    }

    @Override
    public List<String> getLore(ItemTag item) {
        if (!item.getItemMeta().hasLore()) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        List<net.minecraft.nbt.Tag> list = ((net.minecraft.nbt.CompoundTag) nmsItemStack.getTag().get("display")).getList("Lore", 8);
        List<String> outList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            BaseComponent[] lineComponent = ComponentSerializer.parse(((ListTag) list).getString(i));
            outList.add(FormattedTextHelper.stringify(lineComponent, ChatColor.WHITE));
        }
        return outList;
    }

    @Override
    public void setDisplayName(ItemTag item, String name) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        net.minecraft.nbt.CompoundTag tag = nmsItemStack.getOrCreateTag();
        net.minecraft.nbt.CompoundTag display = tag.getCompound("display");
        if (!tag.contains("display")) {
            tag.put("display", display);
        }
        if (name == null || name.isEmpty()) {
            display.put("Name", null);
            return;
        }
        BaseComponent[] components = FormattedTextHelper.parse(name, ChatColor.WHITE);
        display.put("Name", net.minecraft.nbt.StringTag.valueOf(ComponentSerializer.toString(components)));
        item.setItemStack(CraftItemStack.asBukkitCopy(nmsItemStack));
    }

    @Override
    public void setLore(ItemTag item, List<String> lore) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        net.minecraft.nbt.CompoundTag tag = nmsItemStack.getOrCreateTag();
        net.minecraft.nbt.CompoundTag display = tag.getCompound("display");
        if (!tag.contains("display")) {
            tag.put("display", display);
        }
        if (lore == null || lore.isEmpty()) {
            display.put("Lore", null);
        }
        else {
            List<net.minecraft.nbt.Tag> tagList = new ListTag();
            for (String line : lore) {
                tagList.add(net.minecraft.nbt.StringTag.valueOf(ComponentSerializer.toString(FormattedTextHelper.parse(line, ChatColor.WHITE))));
            }
            display.put("Lore", (ListTag) tagList);
        }
        item.setItemStack(CraftItemStack.asBukkitCopy(nmsItemStack));
    }

    public static Map<NamespacedKey, org.bukkit.enchantments.Enchantment> ENCHANTMENTS_BY_KEY = ReflectionHelper.getFieldValue(org.bukkit.enchantments.Enchantment.class, "byKey", null);
    public static Map<String, org.bukkit.enchantments.Enchantment> ENCHANTMENTS_BY_NAME = ReflectionHelper.getFieldValue(org.bukkit.enchantments.Enchantment.class, "byName", null);

    @Override
    public void registerFakeEnchantment(EnchantmentScriptContainer.EnchantmentReference script) {
        try {
            EquipmentSlot[] slots = new EquipmentSlot[script.script.slots.size()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = EquipmentSlot.valueOf(script.script.slots.get(i).toUpperCase());
            }
            Enchantment nmsEnchant = new Enchantment(Enchantment.Rarity.valueOf(script.script.rarity), EnchantmentCategory.valueOf(script.script.category), slots) {
                @Override
                public int getMinLevel() {
                    return script.script.minLevel;
                }
                @Override
                public int getMaxLevel() {
                    return script.script.maxLevel;
                }
                @Override
                public int getMinCost(int level) {
                    return Integer.parseInt(script.script.autoTagForLevel(script.script.minCostTaggle, level));
                }
                @Override
                public int getMaxCost(int level) {
                    return Integer.parseInt(script.script.autoTagForLevel(script.script.maxCostTaggable, level));
                }
                @Override
                public int getDamageProtection(int level, DamageSource src) {
                    return script.script.getDamageProtection(level, src.msgId);
                }
                @Override
                public float getDamageBonus(int level, MobType type) {
                    String typeName = "UNDEFINED";
                    if (type == MobType.ARTHROPOD) {
                        typeName = "ARTHROPOD";
                    }
                    else if (type == MobType.ILLAGER) {
                        typeName = "ILLAGER";
                    }
                    else if (type == MobType.UNDEAD) {
                        typeName = "UNDEAD";
                    }
                    else if (type == MobType.WATER) {
                        typeName = "WATER";
                    }
                    return script.script.getDamageBonus(level, typeName);
                }
                @Override
                protected boolean checkCompatibility(Enchantment nmsEnchantment) {
                    ResourceLocation nmsKey = Registry.ENCHANTMENT.getKey(nmsEnchantment);
                    NamespacedKey bukkitKey = CraftNamespacedKey.fromMinecraft(nmsKey);
                    org.bukkit.enchantments.Enchantment bukkitEnchant = CraftEnchantment.getByKey(bukkitKey);
                    return script.script.isCompatible(bukkitEnchant);
                }
                @Override
                protected String getOrCreateDescriptionId() {
                    return script.script.descriptionId;
                }
                @Override
                public String getDescriptionId() {
                    return script.script.descriptionId;
                }
                @Override
                public Component getFullname(int level) {
                    return Handler.componentToNMS(script.script.getFullName(level));
                }
                @Override
                public boolean canEnchant(net.minecraft.world.item.ItemStack var0) {
                    return script.script.canEnchant(CraftItemStack.asBukkitCopy(var0));
                }
                @Override
                public void doPostAttack(LivingEntity attacker, Entity victim, int level) {
                    script.script.doPostAttack(attacker.getBukkitEntity(), victim.getBukkitEntity(), level);
                }
                @Override
                public void doPostHurt(LivingEntity victim, Entity attacker, int level) {
                    script.script.doPostHurt(victim.getBukkitEntity(), attacker.getBukkitEntity(), level);
                }
                @Override
                public boolean isTreasureOnly() {
                    return script.script.isTreasureOnly;
                }
                @Override
                public boolean isCurse() {
                    return script.script.isCurse;
                }
                @Override
                public boolean isTradeable() {
                    return script.script.isTradable;
                }
                @Override
                public boolean isDiscoverable() {
                    return script.script.isDiscoverable;
                }
            };
            String enchName = script.script.id.toUpperCase();
            Registry.register(Registry.ENCHANTMENT, "denizen:" + script.script.id, nmsEnchant);
            CraftEnchantment ench = new CraftEnchantment(nmsEnchant) {
                @Override
                public String getName() {
                    return enchName;
                }
            };
            ENCHANTMENTS_BY_KEY.put(ench.getKey(), ench);
            ENCHANTMENTS_BY_NAME.put(enchName, ench);
        }
        catch (Throwable ex) {
            Debug.echoError("Failed to register enchantment " + script.script.id);
            Debug.echoError(ex);
        }
    }
}

package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.EnchantmentTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class ItemEnchantments implements Property {

    public static boolean describes(ObjectTag item) {
        // Technically, all items can hold enchants.
        return item instanceof ItemTag;
    }

    public static ItemEnchantments getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemEnchantments((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "is_enchanted", "enchantments", "enchantment_map", "enchantment_types"
    };

    public static final String[] handledMechs = new String[] {
            "remove_enchantments", "enchantments"
    };

    public ItemEnchantments(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.is_enchanted>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.enchantments
        // @group properties
        // @description
        // Returns whether the item has any enchantments.
        // -->
        if (attribute.startsWith("is_enchanted")) {
            return new ElementTag(getEnchantments().size() > 0)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("enchantments.with_levels")) {
            BukkitImplDeprecations.itemEnchantmentTags.warn(attribute.context);
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            ListTag enchants = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                enchants.add(new EnchantmentTag(enchantment.getKey()).getCleanName() + "," + enchantment.getValue());
            }
            return enchants.getObjectAttribute(attribute.fulfill(2));
        }
        if (attribute.startsWith("enchantments.levels")) {
            BukkitImplDeprecations.itemEnchantmentTags.warn(attribute.context);
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            ListTag enchants = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                enchants.add(String.valueOf(enchantment.getValue()));
            }
            return enchants.getObjectAttribute(attribute.fulfill(2));
        }
        if (attribute.startsWith("enchantments.level") && attribute.hasContext(2)) {
            BukkitImplDeprecations.itemEnchantmentTags.warn(attribute.context);
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            if (enchantments.size() > 0) {
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                    if (enchantment.getKey().getName().equalsIgnoreCase(attribute.getContext(2))
                            || new EnchantmentTag(enchantment.getKey()).getCleanName().equalsIgnoreCase(attribute.getContext(2))) {
                        return new ElementTag(enchantment.getValue())
                                .getObjectAttribute(attribute.fulfill(2));
                    }
                }
            }
            return new ElementTag(0)
                    .getObjectAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <ItemTag.enchantment_types>
        // @returns ListTag(EnchantmentTag)
        // @mechanism ItemTag.enchantments
        // @group properties
        // @description
        // Returns a list of the types of enchantments on the item.
        // -->
        if (attribute.startsWith("enchantment_types")) {
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            ListTag enchants = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                enchants.addObject(new EnchantmentTag(enchantment.getKey()));
            }
            return enchants.getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.enchantments>
        // @returns ListTag
        // @mechanism ItemTag.enchantments
        // @group properties
        // @deprecated Use 'enchantment_types' or 'enchantment_map'
        // @description
        // Deprecated in favor of <@link tag ItemTag.enchantment_types> or <@link tag ItemTag.enchantment_map>
        // -->
        if (attribute.startsWith("enchantments")) {
            BukkitImplDeprecations.itemEnchantmentsLegacy.warn(attribute.context);
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            ListTag enchants = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                enchants.add(new EnchantmentTag(enchantment.getKey()).getCleanName());
            }
            return enchants.getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.enchantment_map>
        // @returns MapTag
        // @mechanism ItemTag.enchantments
        // @group properties
        // @description
        // Returns a map of enchantments on the item.
        // Map keys are enchantment names (like "sharpness"), and values are the level (as a number).
        // -->
        if (attribute.startsWith("enchantment_map")) {
            return getEnchantmentMap().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public MapTag getEnchantmentMap() {
        MapTag enchants = new MapTag();
        for (Map.Entry<Enchantment, Integer> enchantment : getEnchantments()) {
            enchants.putObject(new EnchantmentTag(enchantment.getKey()).getCleanName(), new ElementTag(enchantment.getValue()));
        }
        return enchants;
    }

    public Set<Map.Entry<Enchantment, Integer>> getEnchantments() {
        if (item.getItemStack().getEnchantments().size() > 0) {
            return item.getItemStack().getEnchantments().entrySet();
        }
        else if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            return ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants().entrySet();
        }
        return new HashSet<>();
    }

    @Override
    public String getPropertyString() {
        MapTag map = getEnchantmentMap();
        if (map.isEmpty()) {
            return null;
        }
        return map.toString();
    }

    @Override
    public String getPropertyId() {
        return "enchantments";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name remove_enchantments
        // @input ListTag
        // @description
        // Removes the specified enchantments from the item (as a list of EnchantmentTags).
        // Give no value input to remove all enchantments.
        // @tags
        // <ItemTag.enchantment_types>
        // <ItemTag.enchantment_map>
        // -->
        if (mechanism.matches("remove_enchantments")) {
            if (mechanism.hasValue()) {
                List<EnchantmentTag> toRemove = mechanism.valueAsType(ListTag.class).filter(EnchantmentTag.class, mechanism.context);
                if (item.getBukkitMaterial() == Material.ENCHANTED_BOOK) {
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                    for (EnchantmentTag ench : toRemove) {
                        meta.removeStoredEnchant(ench.enchantment);
                    }
                    item.setItemMeta(meta);

                }
                else {
                    for (EnchantmentTag ench : toRemove) {
                        item.getItemStack().removeEnchantment(ench.enchantment);
                    }
                    item.resetCache();
                }
            }
            else {
                if (item.getBukkitMaterial() == Material.ENCHANTED_BOOK) {
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                    for (Enchantment ench : meta.getStoredEnchants().keySet()) {
                        meta.removeStoredEnchant(ench);
                    }
                    item.setItemMeta(meta);
                }
                else {
                    for (Enchantment ench : item.getItemStack().getEnchantments().keySet()) {
                        item.getItemStack().removeEnchantment(ench);
                    }
                    item.resetCache();
                }
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name enchantments
        // @input MapTag
        // @description
        // Sets the item's enchantments as a map of EnchantmentTags or enchantment names to level.
        // For example: - inventory adjust slot:hand enchantments:sharpness=1
        // Does not remove existing enchantments, for that use <@link mechanism ItemTag.remove_enchantments>
        // @tags
        // <ItemTag.enchantment_map>
        // -->
        if (mechanism.matches("enchantments")) {
            String val = mechanism.getValue().asString();
            if (val.startsWith("map@") || val.startsWith("[") || (val.contains("=") && !val.contains(","))) {
                MapTag map = mechanism.valueAsType(MapTag.class);
                for (Map.Entry<StringHolder, ObjectTag> enchantments : map.entrySet()) {
                    Enchantment ench = EnchantmentTag.valueOf(enchantments.getKey().low, mechanism.context).enchantment;
                    int level = enchantments.getValue().asElement().asInt();
                    if (ench != null) {
                        if (item.getBukkitMaterial() == Material.ENCHANTED_BOOK) {
                            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                            meta.addStoredEnchant(ench, level, true);
                            item.setItemMeta(meta);
                        }
                        else {
                            item.getItemStack().addUnsafeEnchantment(ench, level);
                            item.resetCache();
                        }
                    }
                    else {
                        mechanism.echoError("Unknown enchantment '" + enchantments.getKey().str + "'");
                    }
                }
            }
            else {
                for (String enchant : mechanism.valueAsType(ListTag.class)) {
                    if (!enchant.contains(",")) {
                        mechanism.echoError("Invalid enchantment format, use name,level|...");
                    }
                    else {
                        String[] data = enchant.split(",", 2);
                        try {
                            Enchantment ench = EnchantmentTag.valueOf(data[0], mechanism.context).enchantment;
                            if (ench != null) {
                                if (item.getBukkitMaterial() == Material.ENCHANTED_BOOK) {
                                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                                    meta.addStoredEnchant(ench, Integer.parseInt(data[1]), true);
                                    item.setItemMeta(meta);
                                }
                                else {
                                    item.getItemStack().addUnsafeEnchantment(ench, Integer.parseInt(data[1]));
                                    item.resetCache();
                                }
                            }
                            else {
                                mechanism.echoError("Unknown enchantment '" + data[0] + "'");
                            }
                        }
                        catch (NullPointerException e) {
                            mechanism.echoError("Unknown enchantment '" + data[0] + "'");
                        }
                        catch (NumberFormatException ex) {
                            mechanism.echoError("Cannot apply enchantment '" + data[0] + "': '" + data[1] + "' is not a valid integer!");
                            if (CoreConfiguration.debugVerbose) {
                                Debug.echoError(ex);
                            }
                        }
                    }
                }
            }
        }
    }
}

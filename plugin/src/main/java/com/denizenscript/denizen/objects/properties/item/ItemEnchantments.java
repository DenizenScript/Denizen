package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
            "is_enchanted", "enchantments"
    };

    public static final String[] handledMechs = new String[] {
            "remove_enchantments", "enchantments"
    };

    private ItemEnchantments(ItemTag _item) {
        item = _item;
    }

    public static String getName(Enchantment enchantment) {
        return enchantment.getKey().getKey();
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

        // <--[tag]
        // @attribute <ItemTag.enchantments.with_levels>
        // @returns ListTag
        // @mechanism ItemTag.enchantments
        // @group properties
        // @description
        // Returns a list of enchantments on the item, with their levels listed too.
        // In the format of ENCHANTMENT,LEVEL - For example: sharpness,3
        // -->
        if (attribute.startsWith("enchantments.with_levels")) {
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            ListTag enchants = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                enchants.add(getName(enchantment.getKey()) + "," + enchantment.getValue());
            }
            return enchants.getObjectAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <ItemTag.enchantments.levels>
        // @returns ListTag
        // @mechanism ItemTag.enchantments
        // @group properties
        // @description
        // Returns a list of enchantments on the item, showing only the level.
        // -->
        if (attribute.startsWith("enchantments.levels")) {
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            ListTag enchants = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                enchants.add(String.valueOf(enchantment.getValue()));
            }
            return enchants.getObjectAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <ItemTag.enchantments.level[<name>]>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.enchantments
        // @group properties
        // @description
        // Returns the level of a specified enchantment.
        // -->
        if (attribute.startsWith("enchantments.level")
                && attribute.hasContext(2)) {
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            if (enchantments.size() > 0) {
                for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                    if (enchantment.getKey().getName().equalsIgnoreCase(attribute.getContext(2))
                            || getName(enchantment.getKey()).equalsIgnoreCase(attribute.getContext(2))) {
                        return new ElementTag(enchantment.getValue())
                                .getObjectAttribute(attribute.fulfill(2));
                    }
                }
            }
            return new ElementTag(0)
                    .getObjectAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <ItemTag.enchantments>
        // @returns ListTag
        // @mechanism ItemTag.enchantments
        // @group properties
        // @description
        // Returns a list of enchantments on the item.
        // -->
        if (attribute.startsWith("enchantments")) {
            Set<Map.Entry<Enchantment, Integer>> enchantments = getEnchantments();
            ListTag enchants = new ListTag();
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments) {
                enchants.add(getName(enchantment.getKey()));
            }
            return enchants.getObjectAttribute(attribute.fulfill(1));
        }

        return null;
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
        Set<Map.Entry<Enchantment, Integer>> enchants = getEnchantments();
        if (enchants.size() > 0) {
            StringBuilder returnable = new StringBuilder();
            for (Map.Entry<Enchantment, Integer> enchantment : enchants) {
                returnable.append(getName(enchantment.getKey())).append(",").append(enchantment.getValue()).append("|");
            }
            return returnable.substring(0, returnable.length() - 1);
        }
        else {
            return null;
        }
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
        // Removes the specified enchantments from the item (as a list of enchantment names).
        // Give no value input to remove all enchantments.
        // @tags
        // <ItemTag.enchantments>
        // <ItemTag.enchantments.levels>
        // <ItemTag.enchantments.with_levels>
        // -->
        if (mechanism.matches("remove_enchantments")) {
            HashSet<String> names = null;
            if (mechanism.hasValue()) {
                names = new HashSet<>();
                for (String ench : mechanism.valueAsType(ListTag.class)) {
                    names.add(CoreUtilities.toLowerCase(ench));
                }
            }
            if (item.getItemStack().getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                for (Enchantment ench : new ArrayList<>(meta.getStoredEnchants().keySet())) {
                    if (names == null || names.contains(CoreUtilities.toLowerCase(ench.getName())) ||
                            names.contains(CoreUtilities.toLowerCase(getName(ench)))) {
                        meta.removeStoredEnchant(ench);
                    }
                }
                item.getItemStack().setItemMeta(meta);
            }
            else {
                for (Enchantment ench : new ArrayList<>(item.getItemStack().getEnchantments().keySet())) {
                    if (names == null || names.contains(CoreUtilities.toLowerCase(ench.getName())) ||
                            names.contains(CoreUtilities.toLowerCase(getName(ench)))) {
                        item.getItemStack().removeEnchantment(ench);
                    }
                }
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name enchantments
        // @input ListTag
        // @description
        // Sets the item's enchantments.
        // In the format of ENCHANTMENT,LEVEL - For example: sharpness,3
        // @tags
        // <ItemTag.enchantments>
        // <ItemTag.enchantments.levels>
        // <ItemTag.enchantments.with_levels>
        // -->
        if (mechanism.matches("enchantments")) {
            for (String enchant : mechanism.valueAsType(ListTag.class)) {
                if (!enchant.contains(",")) {
                    Debug.echoError("Invalid enchantment format, use name,level|...");
                }
                else {
                    String[] data = enchant.split(",", 2);
                    if (Integer.valueOf(data[1]) == null) {
                        Debug.echoError("Cannot apply enchantment '" + data[0] + "': '" + data[1] + "' is not a valid integer!");
                    }
                    else {
                        try {
                            Enchantment ench = Utilities.getEnchantmentByName(data[0]);
                            if (ench != null) {
                                if (item.getItemStack().getType() == Material.ENCHANTED_BOOK) {
                                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                                    meta.addStoredEnchant(ench, Integer.valueOf(data[1]), true);
                                    item.getItemStack().setItemMeta(meta);
                                }
                                else {
                                    item.getItemStack().addUnsafeEnchantment(ench, Integer.valueOf(data[1]));
                                }
                            }
                            else {
                                Debug.echoError("Unknown enchantment '" + data[0] + "'");
                            }
                        }
                        catch (NullPointerException e) {
                            Debug.echoError("Unknown enchantment '" + data[0] + "'");
                        }
                    }
                }
            }
        }
    }
}

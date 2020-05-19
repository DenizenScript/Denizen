package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHidden implements Property {

    public static boolean describes(ObjectTag item) {
        // All items can have flags
        return item instanceof ItemTag && ((ItemTag) item).getItemStack().getType() != Material.AIR;
    }

    public static ItemHidden getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemHidden((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "flags", "hides"
    };

    public static final String[] handledMechs = new String[] {
            "flags", "hides"
    };

    private ItemHidden(ItemTag _item) {
        item = _item;
    }

    @Deprecated
    public ListTag flags() {
        ListTag output = new ListTag();
        ItemStack itemStack = item.getItemStack();
        if (itemStack.hasItemMeta()) {
            for (ItemFlag flag : itemStack.getItemMeta().getItemFlags()) {
                output.add(flag.name());
            }
        }
        return output;
    }

    public ListTag hides() {
        ListTag output = new ListTag();
        ItemStack itemStack = item.getItemStack();
        if (itemStack.hasItemMeta()) {
            for (ItemFlag flag : itemStack.getItemMeta().getItemFlags()) {
                output.add(flag.name().substring("HIDE_".length()));
            }
        }
        return output;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.hides>
        // @returns ListTag
        // @mechanism ItemTag.hides
        // @group properties
        // @description
        // Returns a list of item data types to be hidden from view on this item.
        // Valid hide types include: ATTRIBUTES, DESTROYS, ENCHANTS, PLACED_ON, POTION_EFFECTS, and UNBREAKABLE
        // NOTE: 'POTION_EFFECTS' also hides banner patterns.
        // -->
        if (attribute.startsWith("hides")) {
            return hides().getObjectAttribute(attribute.fulfill(1));
        }
        if (attribute.startsWith("flags")) {
            Deprecations.itemFlagsProperty.warn(attribute.context);
            return flags().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        ListTag hidden = hides();
        if (hidden.size() > 0) {
            return hidden.identify();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "hides";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name hides
        // @input ListTag
        // @description
        // Sets the item's list of data types to hide.
        // Use "ALL" to automatically hide all hideable item data.
        // @tags
        // <ItemTag.hides>
        // -->
        if (mechanism.matches("flags") || mechanism.matches("hides")) {
            if (mechanism.matches("flags")) {
                Deprecations.itemFlagsProperty.warn(mechanism.context);
            }
            ItemMeta meta = item.getItemStack().getItemMeta();
            meta.removeItemFlags(ItemFlag.values());
            ListTag new_hides = mechanism.valueAsType(ListTag.class);
            for (String str : new_hides) {
                str = str.toUpperCase();
                if (!str.startsWith("HIDE_")) {
                    str = "HIDE_" + str;
                }
                if (str.equals("HIDE_ALL")) {
                    meta.addItemFlags(ItemFlag.values());
                }
                else {
                    meta.addItemFlags(ItemFlag.valueOf(str));
                }
            }
            item.getItemStack().setItemMeta(meta);
        }

    }
}

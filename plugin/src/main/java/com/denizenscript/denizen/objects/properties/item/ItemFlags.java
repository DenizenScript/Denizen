package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemFlags implements Property {

    public static boolean describes(ObjectTag item) {
        // All items can have flags
        return item instanceof ItemTag && ((ItemTag) item).getItemStack().getType() != Material.AIR;
    }

    public static ItemFlags getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemFlags((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "flags"
    };

    public static final String[] handledMechs = new String[] {
            "flags"
    };


    private ItemFlags(ItemTag _item) {
        item = _item;
    }

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

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.flags>
        // @returns ListTag
        // @mechanism ItemTag.flags
        // @group properties
        // @description
        // Returns a list of flags set on this item.
        // Valid flags include: HIDE_ATTRIBUTES, HIDE_DESTROYS, HIDE_ENCHANTS, HIDE_PLACED_ON, HIDE_POTION_EFFECTS, and HIDE_UNBREAKABLE
        // NOTE: 'HIDE_POTION_EFFECTS' also hides banner patterns.
        // -->
        if (attribute.startsWith("flags")) {
            return flags()
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        ListTag flags = flags();
        if (flags.size() > 0) {
            return flags().identify();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "flags";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name flags
        // @input ListTag
        // @description
        // Sets the item's meta flag set.
        // @tags
        // <ItemTag.flags>
        // -->
        if (mechanism.matches("flags")) {
            ItemMeta meta = item.getItemStack().getItemMeta();
            meta.removeItemFlags(ItemFlag.values());
            ListTag new_flags = mechanism.valueAsType(ListTag.class);
            for (String str : new_flags) {
                meta.addItemFlags(ItemFlag.valueOf(str.toUpperCase()));
            }
            item.getItemStack().setItemMeta(meta);
        }

    }
}

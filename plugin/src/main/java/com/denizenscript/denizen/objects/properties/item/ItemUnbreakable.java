package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUnbreakable implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof ItemTag;
    }

    public static ItemUnbreakable getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        return new ItemUnbreakable((ItemTag) object);
    }

    public static final String[] handledTags = new String[] {
            "unbreakable"
    };

    public static final String[] handledMechs = new String[] {
            "unbreakable"
    };

    public ItemUnbreakable(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.unbreakable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @mechanism ItemTag.unbreakable
        // @description
        // Returns whether an item has the unbreakable flag.
        // -->
        if (attribute.startsWith("unbreakable")) {
            return new ElementTag(getPropertyString() != null).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public String getPropertyString() {
        return (item.getItemMeta() != null && item.getItemMeta().isUnbreakable()) ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "unbreakable";
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name unbreakable
        // @input ElementTag(Boolean)
        // @description
        // Changes whether an item has the unbreakable item flag.
        // @tags
        // <ItemTag.unbreakable>
        // -->
        if (mechanism.matches("unbreakable") && mechanism.requireBoolean()) {
            ItemMeta meta = item.getItemMeta();
            meta.setUnbreakable(mechanism.getValue().asBoolean());
            item.setItemMeta(meta);
        }
    }
}

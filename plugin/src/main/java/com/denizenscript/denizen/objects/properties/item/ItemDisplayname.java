package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDisplayname implements Property {

    public static boolean describes(ObjectTag item) {
        // Technically, all items can have a display name
        return item instanceof ItemTag;
    }

    public static ItemDisplayname getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemDisplayname((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "display", "has_display"
    };

    public static final String[] handledMechs = new String[] {
            "display_name"
    };


    private ItemDisplayname(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    public boolean hasDisplayName() {
        return item.getItemStack().hasItemMeta()
                && item.getItemStack().getItemMeta().hasDisplayName();
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.display>
        // @returns ElementTag
        // @mechanism ItemTag.display_name
        // @group properties
        // @description
        // Returns the display name of the item, as set by plugin or an anvil.
        // -->
        if (attribute.startsWith("display")) {
            if (hasDisplayName()) {
                return new ElementTag(item.getItemStack().getItemMeta().getDisplayName())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <ItemTag.has_display>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.display_name
        // @group properties
        // @description
        // Returns whether the item has a custom set display name.
        // -->
        if (attribute.startsWith("has_display")) {
            return new ElementTag(hasDisplayName())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (hasDisplayName()) {
            return EscapeTagBase.escape(item.getItemStack().getItemMeta().getDisplayName());
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "display_name";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name display_name
        // @input Element
        // @description
        // Changes the items display name.
        // See <@link language Property Escaping>
        // @tags
        // <ItemTag.display>
        // -->

        if (mechanism.matches("display_name")) {
            ItemMeta meta = item.getItemStack().getItemMeta();
            meta.setDisplayName(mechanism.hasValue() ? EscapeTagBase.unEscape(mechanism.getValue().asString()) : null);
            item.getItemStack().setItemMeta(meta);
        }
    }
}

package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCustomModel implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag;
    }

    public static ItemCustomModel getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemCustomModel((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
           "custom_model_data", "has_custom_model_data"
    };

    public static final String[] handledMechs = new String[] {
            "custom_model_data"
    };

    public ItemCustomModel(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.has_custom_model_data>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.custom_model_data
        // @group properties
        // @description
        // Returns whether the item has a custom model data ID number set on it.
        // Also see <@link tag ItemTag.custom_model_data>.
        // -->
        if (attribute.startsWith("has_custom_model_data")) {
            return new ElementTag(item.getItemMeta().hasCustomModelData())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.custom_model_data>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.custom_model_data
        // @group properties
        // @description
        // Returns the custom model data ID number of the item.
        // This tag is invalid for items that do not have a custom model data ID.
        // Also see <@link tag ItemTag.has_custom_model_data>.
        // -->
        if (attribute.startsWith("custom_model_data")) {
            if (item.getItemMeta().hasCustomModelData()) {
                return new ElementTag(item.getItemMeta().getCustomModelData())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        if (item.getItemMeta().hasCustomModelData()) {
            return String.valueOf(item.getItemMeta().getCustomModelData());
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "custom_model_data";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name custom_model_data
        // @input ElementTag(Number)
        // @description
        // Changes the custom model data ID number of the item.
        // Use with no input to remove the custom model data.
        // @tags
        // <ItemTag.has_custom_model_data>
        // <ItemTag.custom_model_data>
        // -->
        if (mechanism.matches("custom_model_data")) {
            ItemMeta meta = (item.getItemMeta());
            if (mechanism.hasValue() && mechanism.requireInteger()) {
                meta.setCustomModelData(mechanism.getValue().asInt());
            }
            else {
                meta.setCustomModelData(null);
            }
            item.setItemMeta(meta);
        }
    }
}

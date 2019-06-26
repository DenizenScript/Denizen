package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCustomModel implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem;
    }

    public static ItemCustomModel getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemCustomModel((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
           "custom_model_data", "has_custom_model_data"
    };

    public static final String[] handledMechs = new String[] {
            "custom_model_data"
    };


    private ItemCustomModel(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.has_custom_model_data>
        // @returns Element(Boolean)
        // @mechanism dItem.custom_model_data
        // @group properties
        // @description
        // Returns whether the item has a custom model data ID number set on it.
        // Also see <@link tag i@item.custom_model_data>.
        // -->
        if (attribute.startsWith("has_custom_model_data")) {
            return new Element(item.getItemStack().getItemMeta().hasCustomModelData())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.custom_model_data>
        // @returns Element(Number)
        // @mechanism dItem.custom_model_data
        // @group properties
        // @description
        // Returns the custom model data ID number of the item.
        // This tag is invalid for items that do not have a custom model data ID.
        // Also see <@link tag i@item.has_custom_model_data>.
        // -->
        if (attribute.startsWith("custom_model_data")) {
            if (item.getItemStack().getItemMeta().hasCustomModelData()) {
                return new Element(item.getItemStack().getItemMeta().getCustomModelData())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getItemMeta().hasCustomModelData()) {
            return String.valueOf(item.getItemStack().getItemMeta().getCustomModelData());
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
        // @object dItem
        // @name custom_model_data
        // @input Element(Number)
        // @description
        // Changes the custom model data ID number of the item.
        // Use with no input to remove the custom model data.
        // @tags
        // <i@item.has_custom_model_data>
        // <i@item.custom_model_data>
        // -->
        if (mechanism.matches("custom_model_data")) {
            ItemMeta meta = (item.getItemStack().getItemMeta());
            if (mechanism.hasValue() && mechanism.requireInteger()) {
                meta.setCustomModelData(mechanism.getValue().asInt());
            }
            else {
                meta.setCustomModelData(null);
            }
            item.getItemStack().setItemMeta(meta);
        }
    }
}

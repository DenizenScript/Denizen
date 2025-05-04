package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCustomModel extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name custom_model_data
    // @input ElementTag(Number)
    // @description
    // Controls the custom model data ID number of the item.
    // Use with no input to remove the custom model data.
    // See also <@link tag ItemTag.has_custom_model_data>
    // -->
    public static boolean describes(ItemTag item) {
        return !item.getBukkitMaterial().isAir();
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getItemMeta().hasCustomModelData()) {
            return new ElementTag(getItemMeta().getCustomModelData());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        ItemMeta meta = getItemMeta();
        if (mechanism.hasValue() && mechanism.requireInteger()) {
            meta.setCustomModelData(value.asInt());
        }
        else {
            meta.setCustomModelData(null);
        }
        setItemMeta(meta);
    }

    @Override
    public String getPropertyId() {
        return "custom_model_data";
    }

    public static void register() {
        autoRegisterNullable("custom_model_data", ItemCustomModel.class, ElementTag.class, false);

        // <--[tag]
        // @attribute <ItemTag.has_custom_model_data>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.custom_model_data
        // @group properties
        // @description
        // Returns whether the item has a custom model data ID number set on it.
        // See also <@link tag ItemTag.custom_model_data>.
        // -->
        PropertyParser.registerTag(ItemCustomModel.class, ElementTag.class, "has_custom_model_data", (attribute, prop) -> {
            return new ElementTag(prop.getItemMeta().hasCustomModelData());
        });
    }
}

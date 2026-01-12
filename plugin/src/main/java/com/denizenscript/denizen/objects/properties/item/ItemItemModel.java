package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemItemModel extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name item_model
    // @input ElementTag
    // @description
    // Controls the Item Model of the item.
    // Use with no input to remove the Item Model.
    // See also <@link tag ItemTag.has_item_model>
    // -->
    public static boolean describes(ItemTag item) {
        return !item.getBukkitMaterial().isAir();
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getItemMeta().hasItemModel()) {
            return new ElementTag(getItemMeta().getItemModel().toString());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        ItemMeta meta = getItemMeta();
        if (mechanism.hasValue()) {
            meta.setItemModel(NamespacedKey.fromString(value.asString()));
        }
        else {
            meta.setItemModel(null);
        }
        setItemMeta(meta);
    }

    @Override
    public String getPropertyId() {
        return "item_model";
    }

    public static void register() {
        autoRegisterNullable("item_model", ItemItemModel.class, ElementTag.class, false);

        // <--[tag]
        // @attribute <ItemTag.has_item_model>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.item_model
        // @group properties
        // @description
        // Returns whether the item has an Item Model set on it.
        // See also <@link tag ItemTag.item_model>.
        // -->
        PropertyParser.registerTag(ItemItemModel.class, ElementTag.class, "has_item_model", (attribute, prop) -> {
            return new ElementTag(prop.getItemMeta().hasItemModel());
        });
    }
}

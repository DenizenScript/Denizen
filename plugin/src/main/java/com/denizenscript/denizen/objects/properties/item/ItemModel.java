package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemModel extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name item_model
    // @input ElementTag
    // @description
    // Controls the item model of the item in namespaced key format.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() != Material.AIR;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getItemMeta().hasItemModel()) {
            return new ElementTag(Utilities.namespacedKeyToString(getItemMeta().getItemModel()));
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag element, Mechanism mechanism) {
        editMeta(ItemMeta.class, meta -> meta.setItemModel(Utilities.parseNamespacedKey(element.toString())));
    }

    @Override
    public String getPropertyId() {
        return "item_model";
    }

    public static void register() {
        autoRegisterNullable("item_model", ItemModel.class, ElementTag.class, false);
    }
}

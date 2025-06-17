package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMaxStackSize extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name max_stack_size
    // @input ElementTag
    // @description
    // Controls the maximum stack size of the item.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() != Material.AIR;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getItemMeta().hasMaxStackSize()) {
            return new ElementTag(getItemMeta().getMaxStackSize());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag element, Mechanism mechanism) {
        editMeta(ItemMeta.class, meta -> meta.setMaxStackSize(element.asInt()));
    }

    @Override
    public String getPropertyId() {
        return "max_stack_size";
    }

    public static void register() {
        autoRegisterNullable("max_stack_size", ItemMaxDurability.class, ElementTag.class, false);
    }
}

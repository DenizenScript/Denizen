package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMaxDurability extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name max_durability
    // @input ElementTag
    // @description
    // Controls the maximum durability of an item.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() != Material.AIR
                && item.getItemMeta() instanceof Damageable;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getItemMeta() instanceof Damageable damageable
                && damageable.hasMaxDamage()) {
            damageable.getMaxDamage();
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag element, Mechanism mechanism) {
        editMeta(ItemMeta.class, meta -> {
            if (meta instanceof Damageable damageable) {
                damageable.setMaxDamage(element.asInt());
            }
        });
    }

    @Override
    public String getPropertyId() {
        return "max_durability";
    }

    public static void register() {
        autoRegisterNullable("max_durability", ItemMaxDurability.class, ElementTag.class, false);
    }
}

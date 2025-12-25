package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemRarity extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name rarity
    // @input ElementTag
    // @description
    // Controls the rarity of the item.
    // Valid rarities are: 'COMMON', 'UNCOMMON', 'RARE', and 'EPIC'.
    // @mechanism
    // Provide no input to reset the rarity to the default for the item type.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() != Material.AIR;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getItemMeta().hasRarity()) {
            return Utilities.enumlikeToElement(getItemMeta().getRarity());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag element, Mechanism mechanism) {
        if (element == null) {
            editMeta(ItemMeta.class, meta -> meta.setRarity(getItemMeta().getRarity()));
        }
        else if (mechanism.requireEnum(org.bukkit.inventory.ItemRarity.class)) {
            editMeta(ItemMeta.class, meta -> meta.setRarity(element.asEnum(org.bukkit.inventory.ItemRarity.class)));
        }
    }

    @Override
    public String getPropertyId() {
        return "rarity";
    }

    public static void register() {
        autoRegisterNullable("rarity", ItemRarity.class, ElementTag.class, false);
    }
}

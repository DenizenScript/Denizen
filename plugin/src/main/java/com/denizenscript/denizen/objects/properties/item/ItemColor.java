package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Color;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class ItemColor extends ItemProperty<ColorTag> {

    // <--[property]
    // @object ItemTag
    // @name color
    // @input ColorTag
    // @description
    // The color of a leather armor, potion, filled map, or tipped arrow item.
    // For the tag: will return a white <@link objecttype ColorTag> if the given potion item doesn't have a color.
    // For the tag: will return null if the given map item doesn't have a color.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof LeatherArmorMeta
                || item.getItemMeta() instanceof MapMeta
                || item.getItemMeta() instanceof PotionMeta;
    }

    @Override
    public ColorTag getPropertyValue() {
        if (getItemMeta() instanceof LeatherArmorMeta leatherArmorMeta) {
            return BukkitColorExtensions.fromColor(leatherArmorMeta.getColor());
        }
        if (getItemMeta() instanceof MapMeta mapMeta) {
            if (!mapMeta.hasColor()) {
                return null;
            }
            return BukkitColorExtensions.fromColor(mapMeta.getColor());
        }
        if (getItemMeta() instanceof PotionMeta potionMeta) {
            if (!potionMeta.hasColor()) {
                return null;
            }
            return BukkitColorExtensions.fromColor(potionMeta.getColor());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ColorTag color, Mechanism mechanism) {
        if (getItemMeta() instanceof LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(BukkitColorExtensions.getColor(color));
            setItemMeta(leatherArmorMeta);
            return;
        }
        if (getItemMeta() instanceof MapMeta mapMeta) {
            mapMeta.setColor(BukkitColorExtensions.getColor(color));
            setItemMeta(mapMeta);
            return;
        }
        editMeta(PotionMeta.class, meta -> meta.setColor(BukkitColorExtensions.getColor(color)));
    }

    @Override
    public String getPropertyId() {
        return "color";
    }

    public static void register() {
        PropertyParser.registerTag(ItemColor.class, ColorTag.class, "color", (attribute, item) -> {
            if (item.getItemMeta() instanceof PotionMeta potionMeta && !potionMeta.hasColor()) {
                return BukkitColorExtensions.fromColor(Color.WHITE);
            }
            else {
                return item.getPropertyValue();
            }
        }, "dye_color");

        PropertyParser.registerMechanism(ItemColor.class, ColorTag.class, "color", (item, mechanism, color) -> {
            item.setPropertyValue(color, mechanism);
        }, "dye", "dye_color");
    }
}

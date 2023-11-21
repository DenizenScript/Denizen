package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ColorTag;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class ItemColor extends ItemProperty<ColorTag> {

    // <--[property]
    // @object ItemTag
    // @name color
    // @input ColorTag
    // @description
    // Controls the color of the leather armor item, potion item, filled map item, or tipped arrow item.
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
            if (!mapMeta.hasColor() || mapMeta.getColor() == null) {
                return null;
            }
            return BukkitColorExtensions.fromColor(mapMeta.getColor());
        }
        if (getItemMeta() instanceof PotionMeta potionMeta) {
            if (!potionMeta.hasColor() || potionMeta.getColor() == null) {
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
            getItemStack().setItemMeta(leatherArmorMeta);
            return;
        }
        if (getItemMeta() instanceof MapMeta mapMeta) {
            mapMeta.setColor(BukkitColorExtensions.getColor(color));
            getItemStack().setItemMeta(mapMeta);
            return;
        }
        if (getItemMeta() instanceof PotionMeta potionMeta) {
            potionMeta.setColor(BukkitColorExtensions.getColor(color));
            getItemStack().setItemMeta(potionMeta);
        }
    }

    @Override
    public String getPropertyId() {
        return "color";
    }

    public static void register() {
        autoRegister("color", ItemColor.class, ColorTag.class, false, "dye", "dye_color");
    }
}

package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class ItemColor implements Property {

    public static boolean describes(ObjectTag item) {
        if (!(item instanceof ItemTag)) {
            return false;
        }
        Material mat = ((ItemTag) item).getBukkitMaterial();
        // Leather armor, potions, and filled map
        return mat == Material.LEATHER_BOOTS
                || mat == Material.LEATHER_CHESTPLATE
                || mat == Material.LEATHER_HELMET
                || mat == Material.LEATHER_LEGGINGS
                || mat == Material.LEATHER_HORSE_ARMOR
                || mat == Material.POTION
                || mat == Material.SPLASH_POTION
                || mat == Material.LINGERING_POTION
                || mat == Material.FILLED_MAP;
    }

    public static ItemColor getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemColor((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "color", "dye_color"
    };

    public static final String[] handledMechs = new String[] {
            "color", "dye_color", "dye"
    };

    public ItemColor(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.color>
        // @returns ColorTag
        // @mechanism ItemTag.color
        // @group properties
        // @description
        // Returns the color of the leather armor item, potion item, or filled map item.
        // -->
        if (attribute.startsWith("color") || attribute.startsWith("dye_color")) {
            Material mat = item.getBukkitMaterial();
            if (mat == Material.POTION
                    || mat == Material.LINGERING_POTION
                    || mat == Material.SPLASH_POTION) {
                PotionMeta pm = (PotionMeta) item.getItemMeta();
                if (!pm.hasColor()) {
                    return BukkitColorExtensions.fromColor(Color.WHITE).getObjectAttribute(attribute.fulfill((1)));
                }
                return BukkitColorExtensions.fromColor(pm.getColor()).getObjectAttribute(attribute.fulfill((1)));
            }
            if (mat == Material.FILLED_MAP) {
                MapMeta mapMeta = (MapMeta) item.getItemMeta();
                if (!mapMeta.hasColor()) {
                    return null;
                }
                return BukkitColorExtensions.fromColor(mapMeta.getColor()).getObjectAttribute(attribute.fulfill((1)));
            }
            return BukkitColorExtensions.fromColor(((LeatherArmorMeta) item.getItemMeta()).getColor()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        Material mat = item.getBukkitMaterial();
        if (mat == Material.POTION
                || mat == Material.LINGERING_POTION
                || mat == Material.SPLASH_POTION) {
            PotionMeta pm = (PotionMeta) item.getItemMeta();
            if (!pm.hasColor()) {
                return null;
            }
            return BukkitColorExtensions.fromColor(pm.getColor()).identify();
        }
        if (mat == Material.FILLED_MAP) {
            MapMeta mapMeta = (MapMeta) item.getItemMeta();
            if (!mapMeta.hasColor()) {
                return null;
            }
            return BukkitColorExtensions.fromColor(mapMeta.getColor()).identify();
        }
        return BukkitColorExtensions.fromColor(((LeatherArmorMeta) item.getItemMeta()).getColor()).identify();
    }

    @Override
    public String getPropertyId() {
        return "color";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name color
        // @input ColorTag
        // @description
        // Sets the leather armor item's dye color, potion item's color, or filled map item's color.
        // @tags
        // <ItemTag.color>
        // -->
        if ((mechanism.matches("dye") || mechanism.matches("dye_color")
                || mechanism.matches("color")) && (mechanism.requireObject(ColorTag.class))) {
            ColorTag color = mechanism.valueAsType(ColorTag.class);
            Material mat = item.getBukkitMaterial();
            if (mat == Material.POTION || mat == Material.LINGERING_POTION || mat == Material.SPLASH_POTION) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                meta.setColor(BukkitColorExtensions.getColor(color));
                item.setItemMeta(meta);
                return;
            }
            if (mat == Material.FILLED_MAP) {
                MapMeta meta = (MapMeta) item.getItemMeta();
                meta.setColor(BukkitColorExtensions.getColor(color));
                item.setItemMeta(meta);
                return;
            }
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(BukkitColorExtensions.getColor(color));
            item.setItemMeta(meta);
        }

    }
}

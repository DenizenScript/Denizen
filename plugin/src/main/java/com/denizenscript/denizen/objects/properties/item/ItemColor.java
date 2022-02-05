package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class ItemColor implements Property {

    public static boolean describes(ObjectTag item) {
        if (!(item instanceof ItemTag)) {
            return false;
        }
        Material mat = ((ItemTag) item).getBukkitMaterial();
        // Leather armor and potions
        return mat == Material.LEATHER_BOOTS
                || mat == Material.LEATHER_CHESTPLATE
                || mat == Material.LEATHER_HELMET
                || mat == Material.LEATHER_LEGGINGS
                || mat == Material.LEATHER_HORSE_ARMOR
                || mat == Material.POTION
                || mat == Material.SPLASH_POTION
                || mat == Material.LINGERING_POTION;
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

    private ItemColor(ItemTag _item) {
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
        // Returns the color of the leather armor item or potion item.
        // -->
        if (attribute.startsWith("color") || attribute.startsWith("dye_color")) {
            Material mat = item.getBukkitMaterial();
            if (mat == Material.POTION
                    || mat == Material.LINGERING_POTION
                    || mat == Material.SPLASH_POTION) {
                PotionMeta pm = (PotionMeta) item.getItemMeta();
                if (!pm.hasColor()) {
                    return new ColorTag(Color.WHITE).getObjectAttribute(attribute.fulfill((1)));
                }
                return new ColorTag(pm.getColor()).getObjectAttribute(attribute.fulfill((1)));
            }
            return new ColorTag(((LeatherArmorMeta) item.getItemMeta()).getColor()).getObjectAttribute(attribute.fulfill(1));
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
            return new ColorTag(pm.getColor()).identify();
        }
        return new ColorTag(((LeatherArmorMeta) item.getItemMeta()).getColor()).identify();
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
        // Sets the leather armor item's dye color or the potion item's color.
        // @tags
        // <ItemTag.color>
        // -->
        if ((mechanism.matches("dye") || mechanism.matches("dye_color")
                || mechanism.matches("color")) && (mechanism.requireObject(ColorTag.class))) {
            ColorTag color = mechanism.valueAsType(ColorTag.class);
            Material mat = item.getBukkitMaterial();
            if (mat == Material.POTION || mat == Material.LINGERING_POTION || mat == Material.SPLASH_POTION) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                meta.setColor(color.getColor());
                item.setItemMeta(meta);
                return;
            }
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color.getColor());
            item.setItemMeta(meta);
        }

    }
}

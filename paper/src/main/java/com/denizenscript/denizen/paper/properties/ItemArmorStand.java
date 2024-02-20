package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.destroystokyo.paper.inventory.meta.ArmorStandMeta;
import org.bukkit.Material;

public class ItemArmorStand implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getBukkitMaterial() == Material.ARMOR_STAND;
    }

    public static ItemArmorStand getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        return new ItemArmorStand((ItemTag) item);
    }

    public static final String[] handledMechs = new String[] {
            "armor_stand_data"
    };

    public ItemArmorStand(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    public MapTag getDataMap() {
        ArmorStandMeta meta = (ArmorStandMeta) item.getItemMeta();
        if (meta == null) {
            return null;
        }
        MapTag result = new MapTag();
        result.putObject("base_plate", new ElementTag(!meta.hasNoBasePlate()));
        result.putObject("visible", new ElementTag(!meta.isInvisible()));
        result.putObject("marker", new ElementTag(meta.isMarker()));
        result.putObject("is_small", new ElementTag(meta.isSmall()));
        result.putObject("arms", new ElementTag(meta.shouldShowArms()));
        return result;
    }

    @Override
    public String getPropertyString() {
        MapTag result = getDataMap();
        if (result == null) {
            return null;
        }
        return result.toString();
    }

    @Override
    public String getPropertyId() {
        return "armor_stand_data";
    }

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.armor_stand_data>
        // @returns MapTag
        // @mechanism ItemTag.armor_stand_data
        // @group properties
        // @Plugin Paper
        // @description
        // Returns a map of basic armor stand data, with keys matching EntityTag property names.
        // Keys: base_plate, visible, marker, is_small, arms
        // -->
        PropertyParser.registerTag(ItemArmorStand.class, MapTag.class, "armor_stand_data", (attribute, item) -> {
            return item.getDataMap();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name armor_stand_data
        // @input MapTag
        // @Plugin Paper
        // @group properties
        // @description
        // Sets a map of basic armor stand data, with keys matching EntityTag property names.
        // Allowed keys: base_plate, visible, marker, is_small, arms
        // @tags
        // <ItemTag.armor_stand_data>
        // -->
        if (mechanism.matches("armor_stand_data") && mechanism.requireObject(MapTag.class)) {
            MapTag map = mechanism.valueAsType(MapTag.class);
            ArmorStandMeta meta = (ArmorStandMeta) item.getItemMeta();
            ElementTag base_plate = map.getElement("base_plate");
            ElementTag visible = map.getElement("visible");
            ElementTag marker = map.getElement("marker");
            ElementTag is_small = map.getElement("is_small");
            ElementTag arms = map.getElement("arms");
            if (base_plate != null) {
                meta.setNoBasePlate(!base_plate.asBoolean());
            }
            if (visible != null) {
                meta.setInvisible(!visible.asBoolean());
            }
            if (marker != null) {
                meta.setMarker(marker.asBoolean());
            }
            if (is_small != null) {
                meta.setSmall(is_small.asBoolean());
            }
            if (arms != null) {
                meta.setShowArms(arms.asBoolean());
            }
            item.setItemMeta(meta);
        }
    }
}

package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.meta.MapMeta;

public class ItemMap implements Property {

    public static Material MAP_MATERIAL = Material.FILLED_MAP;

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getBukkitMaterial() == MAP_MATERIAL);
    }

    public static ItemMap getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemMap((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "map", "map_scale"
    };

    public static final String[] handledMechs = new String[] {
            "map", "full_render"
    };

    private ItemMap(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.map>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism ItemTag.map
        // @description
        // Returns the ID number of the map item's map.
        // -->
        if (attribute.startsWith("map")) {
            if (!hasMapId()) {
                return null;
            }
            return new ElementTag(getMapId()).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.map_scale>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism ItemTag.map
        // @description
        // Returns the scale of the map, from 0 (smallest) to 4 (largest).
        // -->
        if (attribute.startsWith("map_scale")) {
            if (!hasMapId()) {
                return null;
            }
            MapMeta map = (MapMeta) item.getItemMeta();
            if (map.getMapView() == null || map.getMapView().getScale() == null) {
                return null;
            }
            return new ElementTag(map.getMapView().getScale().getValue()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public boolean hasMapId() {
        MapMeta map = (MapMeta) item.getItemMeta();
        return map.hasMapId();
    }

    public int getMapId() {
        MapMeta map = (MapMeta) item.getItemMeta();
        if (!map.hasMapId()) {
            return 0;
        }
        return map.getMapId();
    }

    public void setMapId(int id) {
        MapMeta map = (MapMeta) item.getItemMeta();
        map.setMapId(id);
        item.setItemMeta(map);
    }

    @Override
    public String getPropertyString() {
        if (!hasMapId()) {
            return null;
        }
        return String.valueOf(getMapId());
    }

    @Override
    public String getPropertyId() {
        return "map";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name map
        // @input ElementTag(Number)
        // @description
        // Changes what map ID number a map item uses.
        // @tags
        // <ItemTag.map>
        // <ItemTag.map_scale>
        // -->
        if (mechanism.matches("map") && mechanism.requireInteger()) {
            setMapId(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object ItemTag
        // @name full_render
        // @input ElementTag(Number)
        // @description
        // Fully renders a map item's view of the world.
        // Be warned that this can run very slowly on large maps.
        // @tags
        // <ItemTag.map>
        // <ItemTag.map_scale>
        // -->
        if (mechanism.matches("full_render")) {
            boolean worked = NMSHandler.getItemHelper().renderEntireMap(getMapId());
            if (!worked) {
                mechanism.echoError("Cannot render map: ID doesn't exist. Has the map never been displayed?");
            }
        }
    }
}

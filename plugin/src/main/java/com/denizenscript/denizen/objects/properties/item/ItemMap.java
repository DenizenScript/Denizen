package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.meta.MapMeta;

import java.util.List;

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
            "map", "map_scale", "map_locked"
    };

    public static final String[] handledMechs = new String[] {
            "map", "full_render", "map_locked"
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
            if (!map.hasMapView()) {
                return null;
            }
            return new ElementTag(map.getMapView().getScale().getValue()).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.map_locked>
        // @returns ElementTag(Boolean)
        // @group properties
        // @mechanism ItemTag.map_locked
        // @description
        // Returns whether maps with the same ID as this map are locked.
        // -->
        if (attribute.startsWith("map_locked")) {
            if (!hasMapId()) {
                return null;
            }
            MapMeta map = (MapMeta) item.getItemMeta();
            if (!map.hasMapView()) {
                return null;
            }
            return new ElementTag(map.getMapView().isLocked()).getObjectAttribute(attribute.fulfill(1));
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
        // @input ElementTag
        // @description
        // Fully renders all or part of a map item's view of the world.
        // Be warned that this can run very slowly on large maps.
        // Input can be nothing to render the full map, or a comma separated set of integers to render part of the map, in format x1,z1,x2,z2.
        // Input numbers are pixel indices within the map image - so, any integer from 0 to 128.
        // The input for a full map render would be 0,0,128,128.
        //
        // Example usage to render sections slowly (to reduce server impact):
        // <code>
        // - repeat 16 as:x:
        //     - adjust <item[map[map=4]]> full_render:<[x].sub[1].mul[8]>,0,<[x].mul[8]>,128
        //     - wait 2t
        // </code>
        // @tags
        // <ItemTag.map>
        // <ItemTag.map_scale>
        // -->
        if (mechanism.matches("full_render")) {
            int xMin = 0, zMin = 0, xMax = 128, zMax = 128;
            if (mechanism.hasValue()) {
                List<String> input = CoreUtilities.split(mechanism.getValue().asString(), ',');
                if (input.size() != 4) {
                    mechanism.echoError("Invalid input to 'full_render' - must be a set of 4 comma separated integers.");
                    return;
                }
                try {
                    xMin = Math.max(Integer.parseInt(input.get(0)), 0);
                    zMin = Math.max(Integer.parseInt(input.get(1)), 0);
                    xMax = Math.min(Integer.parseInt(input.get(2)), 128);
                    zMax = Math.min(Integer.parseInt(input.get(3)), 128);
                }
                catch (NumberFormatException ex) {
                    mechanism.echoError("Invalid input to 'full_render' - found comma separated list of 4 values, but not all values are integers: " + ex.getMessage());
                }
            }
            boolean worked = NMSHandler.itemHelper.renderEntireMap(getMapId(), xMin, zMin, xMax, zMax);
            if (!worked) {
                mechanism.echoError("Cannot render map: ID doesn't exist. Has the map never been displayed?");
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name map_locked
        // @input ElementTag(Boolean)
        // @description
        // Changes whether the map is currently locked.
        // Note that this applies globally to all map items with the same ID.
        // @tags
        // <ItemTag.map>
        // <ItemTag.map_locked>
        // -->
        if (mechanism.matches("map_locked") && mechanism.requireBoolean()) {
            MapMeta meta = ((MapMeta) item.getItemMeta());
            if (!meta.hasMapView()) {
                Debug.echoError("Map is yet loaded/rendered.");
                return;
            }
            meta.getMapView().setLocked(mechanism.getValue().asBoolean());
        }
    }
}

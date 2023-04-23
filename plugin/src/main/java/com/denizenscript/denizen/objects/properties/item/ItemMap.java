package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.List;

public class ItemMap implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getBukkitMaterial() == Material.FILLED_MAP);
    }

    public static ItemMap getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemMap((ItemTag) _item);
        }
    }

    public static final String[] handledMechs = new String[] {
            "map", "full_render", "map_locked", "map_center"
    };

    public ItemMap(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.map>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism ItemTag.map
        // @description
        // Returns the ID number of the map item's map.
        // -->
        PropertyParser.registerTag(ItemMap.class, ElementTag.class, "map", (attribute, object) -> {
            if (!object.hasMapId()) {
                return null;
            }
            return new ElementTag(object.getMapId());
        });

        // <--[tag]
        // @attribute <ItemTag.map_scale>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism ItemTag.map
        // @description
        // Returns the scale of the map, from 0 (smallest) to 4 (largest).
        // -->
        PropertyParser.registerTag(ItemMap.class, ElementTag.class, "map_scale", (attribute, object) -> {
            if (!object.hasMapId()) {
                return null;
            }
            MapMeta map = object.getMapMeta();
            if (!map.hasMapView()) {
                return null;
            }
            return new ElementTag(map.getMapView().getScale().getValue());
        });

        // <--[tag]
        // @attribute <ItemTag.map_locked>
        // @returns ElementTag(Boolean)
        // @group properties
        // @mechanism ItemTag.map_locked
        // @description
        // Returns whether maps with the same ID as this map are locked.
        // -->
        PropertyParser.registerTag(ItemMap.class, ElementTag.class, "map_locked", (attribute, object) -> {
            if (!object.hasMapId()) {
                return null;
            }
            MapMeta map = object.getMapMeta();
            if (!map.hasMapView()) {
                return null;
            }
            return new ElementTag(map.getMapView().isLocked());
        });

        // <--[tag]
        // @attribute <ItemTag.map_center>
        // @returns LocationTag
        // @group properties
        // @mechanism ItemTag.map_center
        // @description
        // Returns the center location on the map's display.
        // Note that there is no Y value (it's always 0), only X, Z, and a World.
        // -->
        PropertyParser.registerTag(ItemMap.class, LocationTag.class, "map_center", (attribute, object) -> {
            if (!object.hasMapId()) {
                return null;
            }
            MapMeta map = object.getMapMeta();
            if (!map.hasMapView()) {
                return null;
            }
            MapView mapView = map.getMapView();
            return new LocationTag(mapView.getWorld(), mapView.getCenterX(), 0, mapView.getCenterZ());
        });
    }

    public MapMeta getMapMeta() {
        return (MapMeta) item.getItemMeta();
    }

    public boolean hasMapId() {
        return getMapMeta().hasMapId();
    }

    public int getMapId() {
        MapMeta map = getMapMeta();
        return map.hasMapId() ? map.getMapId() : 0;
    }

    public void setMapId(int id) {
        MapMeta map = getMapMeta();
        map.setMapId(id);
        item.setItemMeta(map);
    }

    @Override
    public String getPropertyString() {
        return hasMapId() ? String.valueOf(getMapId()) : null;
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
        // @example
        // # Use to render sections slowly (to reduce server impact):
        // - repeat 16 as:x:
        //     - adjust <item[filled_map[map=4]]> full_render:<[x].sub[1].mul[8]>,0,<[x].mul[8]>,128
        //     - wait 2t
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
            MapMeta meta = getMapMeta();
            if (!meta.hasMapView()) {
                mechanism.echoError("Map is not loaded/rendered.");
                return;
            }
            meta.getMapView().setLocked(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object ItemTag
        // @name map_center
        // @input LocationTag
        // @description
        // Sets the map's center location (the location in the middle of the map's display).
        // @tags
        // <ItemTag.map_center>
        // -->
        if (mechanism.matches("map_center") && mechanism.requireObject(LocationTag.class)) {
            LocationTag loc = mechanism.valueAsType(LocationTag.class);
            MapMeta meta = getMapMeta();
            if (!meta.hasMapView()) {
                mechanism.echoError("Map is not loaded/rendered.");
                return;
            }
            MapView mapView = meta.getMapView();
            mapView.setCenterX(loc.getBlockX());
            mapView.setCenterZ(loc.getBlockZ());
            if (loc.getWorld() != null) {
                mapView.setWorld(loc.getWorld());
            }
        }
    }
}

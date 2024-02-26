package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.maps.*;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.NaturalOrderComparator;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapScriptContainer extends ScriptContainer {

    public MapScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
    }

    // <--[language]
    // @name Map Script Containers
    // @group Script Container System
    // @description
    // Map scripts allow you define custom in-game map items, for usage with the map command.
    //
    // The following is the format for the container.
    //
    // <code>
    // # The name of the map script is used by the map command.
    // Map_Script_Name:
    //
    //     type: map
    //
    //     # Whether to display the original map below the custom values. Defaults to true.
    //     # | Some map scripts should have this key!
    //     original: true/false
    //
    //     # Whether to constantly update things. Defaults to true.
    //     # | Some map scripts should have this key!
    //     auto update: true
    //
    //     # Whether this map script renders uniquely per-player. Defaults to true.
    //     # | Some map scripts should have this key!
    //     contextual: true
    //
    //     # Lists all contained objects.
    //     # | Most map scripts should have this key!
    //     objects:
    //
    //         # The first object...
    //         1:
    //             # Specify the object type
    //             # Type can be IMAGE, TEXT, CURSOR, or DOT.
    //             type: image
    //             # Specify an HTTP url or file path within Denizen/images/ for the image. Supports animated .gif!
    //             image: my_image.png
    //             # Optionally add width/height numbers.
    //             width: 128
    //             height: 128
    //             # Specify a tag to show or hide custom content! Valid for all objects.
    //             # Note that all inputs other than 'type' for all objects support tags that will be dynamically reparsed per-player each time the map updates.
    //             visible: <player.name.contains_text[bob].not>
    //
    //         2:
    //             type: text
    //             # Specify any text to display. Color codes not permitted (unless you know how to format CraftMapCanvas byte-ID color codes).
    //             text: Hello <player.name>
    //             # Specify the color of the text as any valid ColorTag.
    //             color: red
    //             # | Optionally, specify the following additional options:
    //             # Specify a font to use, which allows using special characters/other languages the default font may not support.
    //             font: arial
    //             # Specify a text size (only available with a custom font).
    //             size: 18
    //             # Specify a style, as a list that contains either "bold", "italic", or both (only available with a custom font).
    //             style: bold|italic
    //         3:
    //             type: cursor
    //             # Specify a cursor type
    //             cursor: red_marker
    //             # Optionally, specify a cursor direction. '180' seems to display as up-right usually.
    //             direction: 180
    //             # Supported on all objects: x/y positions, and whether to use worldly or map coordinates.
    //             x: 5
    //             # If 'world_coordinates' is set to 'true', the 'y' value corresponds to the 'z' value of a location.
    //             y: 5
    //             # If true: uses world coordinates. If false: uses map local coordinates. (Defaults to false).
    //             world_coordinates: false
    //             # If true: when the object goes past the edge, will stay in view at the corner. If false: disappears past the edge (defaults to false).
    //             show_past_edge: false
    //
    //         4:
    //             type: dot
    //             # Specify the radius of the dot.
    //             radius: 1
    //             # Specify the color of the dot as any valid ColorTag.
    //             color: red
    //
    // </code>
    //
    // A list of cursor types is available through <@link tag server.map_cursor_types>.
    //
    // -->

    public void applyTo(MapView mapView) {
        boolean contextual = getString("contextual", "true").equalsIgnoreCase("true");
        DenizenMapRenderer renderer = new DenizenMapRenderer(mapView.getRenderers(), getString("auto update", "true").equalsIgnoreCase("true"), contextual);
        if (contains("original", String.class)) {
            renderer.displayOriginal = getString("original").equalsIgnoreCase("true");
        }
        if (contains("objects", Map.class)) {
            YamlConfiguration objectsSection = getConfigurationSection("objects");
            List<StringHolder> objectKeys1 = new ArrayList<>(objectsSection.getKeys(false));
            List<String> objectKeys = new ArrayList<>(objectKeys1.size());
            for (StringHolder sh : objectKeys1) {
                objectKeys.add(sh.str);
            }
            objectKeys.sort(new NaturalOrderComparator());
            for (String objectKey : objectKeys) {
                YamlConfiguration objectSection = objectsSection.getConfigurationSection(objectKey);
                if (!objectSection.contains("type")) {
                    Debug.echoError("Map script '" + getName() + "' has an object without a specified type!");
                    return;
                }
                String type = CoreUtilities.toLowerCase(objectSection.getString("type"));
                String x = objectSection.getString("x", "0");
                String y = objectSection.getString("y", "0");
                String visible = objectSection.getString("visible", "true");
                MapObject added = null;
                switch (type) {
                    case "image":
                        if (!objectSection.contains("image")) {
                            Debug.echoError("Map script '" + getName() + "'s image '" + objectKey
                                    + "' has no specified image location!");
                            return;
                        }
                        String image = objectSection.getString("image");
                        int width = Integer.parseInt(objectSection.getString("width", "0"));
                        int height = Integer.parseInt(objectSection.getString("height", "0"));
                        added = new MapImage(renderer, x, y, visible, shouldDebug(), image, width, height);
                        break;
                    case "text":
                        if (!objectSection.contains("text")) {
                            Debug.echoError("Map script '" + getName() + "'s text object '" + objectKey
                                    + "' has no specified text!");
                            return;
                        }
                        added = new MapText(x, y, visible, shouldDebug(), objectSection.getString("text"), objectSection.getString("color", "black"),
                                objectSection.getString("font"), objectSection.getString("size"), objectSection.getString("style"));
                        break;
                    case "cursor":
                        if (!objectSection.contains("cursor")) {
                            Debug.echoError("Map script '" + getName() + "'s cursor '" + objectKey
                                    + "' has no specified cursor type!");
                            return;
                        }
                        String cursor = objectSection.getString("cursor");
                        if (cursor == null) {
                            Debug.echoError("Map script '" + getName() + "'s cursor '" + objectKey
                                    + "' is missing a cursor type!");
                            return;
                        }
                        added = new MapCursor(x, y, visible, shouldDebug(), objectSection.getString("direction", "0"), cursor);
                        break;
                    case "dot":
                        added = new MapDot(x, y, visible, shouldDebug(), objectSection.getString("radius", "1"), objectSection.getString("color", "black"));
                        break;
                    default:
                        Debug.echoError("Weird map data!");
                        break;
                }
                if (added != null) {
                    renderer.addObject(added);
                    if (objectSection.contains("world_coordinates") && objectSection.getString("world_coordinates", "false").equalsIgnoreCase("true")) {
                        added.worldCoordinates = true;
                    }
                    added.showPastEdge = CoreUtilities.equalsIgnoreCase(objectSection.getString("show_past_edge", "false"), "true");
                }
            }
        }
        DenizenMapManager.setMap(mapView, renderer);
    }
}

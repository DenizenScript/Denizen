package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.maps.*;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.NaturalOrderComparator;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapScriptContainer extends ScriptContainer {

    public MapScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
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
    // Item Script Name:
    //
    //   type: map
    //
    //   # Whether to display the original map below the custom values.
    //   original: true/false
    //
    //   # The 'custom name' can be anything you wish. Use color tags to make colored custom names.
    //   display name: custom name
    //
    //   # Whether to constantly update things.
    //   auto update: true
    //
    //   # Lists all contained objects.
    //   objects:
    //
    //     # The first object...
    //     1:
    //       # Specify the object type
    //       type: image
    //       # Specify an HTTP url or file path within Denizen/images/ for the image. Supports animated .gif!
    //       image: my_image.png
    //       # Optionally add width/height numbers.
    //
    //     2:
    //       type: text
    //       # Specify any text, with tags.
    //       text: Hello <player.name>
    //       # Specify a tag to show or hide custom content! Valid for all objects.
    //       visible: <player.name.contains[bob].not>
    //
    //     3:
    //       type: cursor
    //       # Specify a cursor - <@link tag server.list_map_cursor_types>
    //       cursor: red_marker
    //       # Supported on all objects: x/y positions, and whether to use worldly or map coordinates.
    //       x: 5
    //       y: 5
    //       world_coordinates: false
    // </code>
    //
    // -->

    public void applyTo(MapView mapView) {
        DenizenMapRenderer renderer = new DenizenMapRenderer(mapView.getRenderers(), getString("auto update", "true").equalsIgnoreCase("true"));
        boolean debug = true;
        if (contains("original")) {
            renderer.displayOriginal = getString("original").equalsIgnoreCase("true");
        }
        if (contains("debug")) {
            debug = getString("debug").equalsIgnoreCase("true");
        }
        if (contains("objects")) {
            YamlConfiguration objectsSection = getConfigurationSection("objects");
            List<StringHolder> objectKeys1 = new ArrayList<>(objectsSection.getKeys(false));
            List<String> objectKeys = new ArrayList<>(objectKeys1.size());
            for (StringHolder sh : objectKeys1) {
                objectKeys.add(sh.str);
            }
            Collections.sort(objectKeys, new NaturalOrderComparator());
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
                boolean worldC = objectSection.contains("world_coordinates") && objectSection.getString("world_coordinates", "false").equalsIgnoreCase("true");
                if (type.equals("image")) {
                    if (!objectSection.contains("image")) {
                        Debug.echoError("Map script '" + getName() + "'s image '" + objectKey
                                + "' has no specified image location!");
                        return;
                    }
                    String image = objectSection.getString("image");
                    int width = Integer.parseInt(objectSection.getString("width", "0"));
                    int height = Integer.parseInt(objectSection.getString("height", "0"));
                    if (CoreUtilities.toLowerCase(image).endsWith(".gif")) {
                        renderer.addObject(new MapAnimatedImage(x, y, visible, debug, image, width, height));
                    }
                    else {
                        renderer.addObject(new MapImage(x, y, visible, debug, image, width, height));
                    }
                }
                else if (type.equals("text")) {
                    if (!objectSection.contains("text")) {
                        Debug.echoError("Map script '" + getName() + "'s text object '" + objectKey
                                + "' has no specified text!");
                        return;
                    }
                    String text = objectSection.getString("text");
                    renderer.addObject(new MapText(x, y, visible, debug, text));
                }
                else if (type.equals("cursor")) {
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
                    renderer.addObject(new MapCursor(x, y, visible, debug, objectSection.getString("direction", "0"), cursor));
                }
                else if (type.equals("dot")) {
                    renderer.addObject(new MapDot(x, y, visible, debug, objectSection.getString("radius", "1"),
                            objectSection.getString("color", "black")));
                }
                else {
                    Debug.echoError("Weird map data!");
                }
                if (worldC && renderer.mapObjects.size() > 0) {
                    renderer.mapObjects.get(renderer.mapObjects.size() - 1).worldCoordinates = true;
                }
            }
        }
        DenizenMapManager.setMap(mapView, renderer);
    }

}

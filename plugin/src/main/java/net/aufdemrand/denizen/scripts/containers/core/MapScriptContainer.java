package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.maps.*;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.NaturalOrderComparator;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import net.aufdemrand.denizencore.utilities.text.StringHolder;
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
    //       # Specify a cursor - {RED|GREEN|WHITE|BLUE)_POINTER, WHITE_CROSS, WHITE_CIRCLE, RED_MARKER, SMALL_WHITE_CIRCLE,
    //       # MANSION, TEMPLE
    //       cursor: red_marker
    //       # Supported on all objects: x/y positions, and whether to use worldly or map coordinates.
    //       x: 5
    //       y: 5
    //       world_coordinates: false
    // </code>
    //
    // -->

    public void applyTo(MapView mapView) {
        DenizenMapRenderer renderer = new DenizenMapRenderer(mapView.getRenderers(),
                aH.getBooleanFrom(getString("AUTO UPDATE", "true")));
        boolean debug = true;
        if (contains("ORIGINAL")) {
            renderer.displayOriginal = aH.getBooleanFrom(getString("ORIGINAL"));
        }
        if (contains("DEBUG")) {
            debug = aH.getBooleanFrom(getString("DEBUG"));
        }
        if (contains("OBJECTS")) {
            YamlConfiguration objectsSection = getConfigurationSection("OBJECTS");
            List<StringHolder> objectKeys1 = new ArrayList<StringHolder>(objectsSection.getKeys(false));
            List<String> objectKeys = new ArrayList<String>(objectKeys1.size());
            for (StringHolder sh : objectKeys1) {
                objectKeys.add(sh.str);
            }
            Collections.sort(objectKeys, new NaturalOrderComparator());
            for (String objectKey : objectKeys) {
                YamlConfiguration objectSection = objectsSection.getConfigurationSection(objectKey);
                if (!objectSection.contains("TYPE")) {
                    dB.echoError("Map script '" + getName() + "' has an object without a specified type!");
                    return;
                }
                String type = objectSection.getString("TYPE").toUpperCase();
                String x = objectSection.getString("X", "0");
                String y = objectSection.getString("Y", "0");
                String visible = objectSection.getString("VISIBLE", "true");
                boolean worldC = objectSection.contains("WORLD_COORDINATES") && aH.getBooleanFrom(objectSection.getString("WORLD_COORDINATES", "false"));
                if (type.equals("IMAGE")) {
                    if (!objectSection.contains("IMAGE")) {
                        dB.echoError("Map script '" + getName() + "'s image '" + objectKey
                                + "' has no specified image location!");
                        return;
                    }
                    String image = objectSection.getString("IMAGE");
                    int width = aH.getIntegerFrom(objectSection.getString("WIDTH", "0"));
                    int height = aH.getIntegerFrom(objectSection.getString("HEIGHT", "0"));
                    if (CoreUtilities.toLowerCase(image).endsWith(".gif")) {
                        renderer.addObject(new MapAnimatedImage(x, y, visible, debug, image, width, height));
                    }
                    else {
                        renderer.addObject(new MapImage(x, y, visible, debug, image, width, height));
                    }
                }
                else if (type.equals("TEXT")) {
                    if (!objectSection.contains("TEXT")) {
                        dB.echoError("Map script '" + getName() + "'s text object '" + objectKey
                                + "' has no specified text!");
                        return;
                    }
                    String text = objectSection.getString("TEXT");
                    renderer.addObject(new MapText(x, y, visible, debug, text));
                }
                else if (type.equals("CURSOR")) {
                    if (!objectSection.contains("CURSOR")) {
                        dB.echoError("Map script '" + getName() + "'s cursor '" + objectKey
                                + "' has no specified cursor type!");
                        return;
                    }
                    String cursor = objectSection.getString("CURSOR");
                    if (cursor == null) {
                        dB.echoError("Map script '" + getName() + "'s cursor '" + objectKey
                                + "' is missing a cursor type!");
                        return;
                    }
                    renderer.addObject(new MapCursor(x, y, visible, debug, objectSection.getString("DIRECTION", "0"), cursor));
                }
                else if (type.equals("DOT")) {
                    renderer.addObject(new MapDot(x, y, visible, debug, objectSection.getString("RADIUS", "1"),
                            objectSection.getString("COLOR", "black")));
                }
                else {
                    dB.echoError("Weird map data!");
                }
                if (worldC && renderer.mapObjects.size() > 0) {
                    renderer.mapObjects.get(renderer.mapObjects.size() - 1).worldCoordinates = true;
                }
            }
        }
        DenizenMapManager.setMap(mapView, renderer);
    }

}

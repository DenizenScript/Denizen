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

    public void applyTo(MapView mapView) {
        DenizenMapRenderer renderer = new DenizenMapRenderer(mapView.getRenderers(),
                aH.getBooleanFrom(getString("AUTO UPDATE", "true")));
        boolean debug = true;
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
            }
        }
        DenizenMapManager.setMap(mapView, renderer);
    }

}

package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.scripts.containers.core.MapScriptContainer;
import com.denizenscript.denizen.utilities.maps.*;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapCommand extends AbstractCommand {

    public MapCommand() {
        setName("map");
        setSyntax("map [<#>/new:<world>] (reset:<location>/reset_to_blank) (scale:<value>) (tracking) (image:<file>) (resize) (script:<script>) (dot:<color>) (radius:<#>) (x:<#>) (y:<#>) (text:<text>) (width:<#>) (height:<#>)");
        setRequiredArguments(2, 14);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Map
    // @Syntax map [<#>/new:<world>] (reset:<location>/reset_to_blank) (scale:<value>) (tracking) (image:<file>) (resize) (script:<script>) (dot:<color>) (radius:<#>) (x:<#>) (y:<#>) (text:<text>) (width:<#>) (height:<#>)
    // @Required 2
    // @Maximum 14
    // @Short Modifies a new or existing map by adding images or text.
    // @Group item
    //
    // @Description
    // This command modifies an existing map, or creates a new one. Using this will override existing non-Denizen map renderers with Denizen's custom map renderer.
    //
    // You must specify at least one of 'reset', 'reset_to_blank', 'script', 'image', 'dot', 'text'. You can specify multiple at once if you prefer.
    //
    // When using 'reset' or 'reset_to_blank', you can specify optionally 'scale' and/or 'tracking'.
    // When using 'image' you can optionally specify 'resize' or 'width:<#>' and 'height:<#>'.
    // When using 'dot', you can specify any valid ColorTag (it will be compressed to map's color space), and you can optionally also specify 'radius' as a number.
    //    Use "radius:0" with dot to set on a single pixel. 1 or higher will make a circle centered on the x/y given.
    //
    // You can reset this at any time by using the 'reset:<location>' argument, which will remove all
    // images and texts on the map and show the default world map at the specified location.
    // You can also specify 'reset_to_blank' to reset without specified location.
    //
    // The 'scale' argument takes input of one of the values listed here:
    // <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/map/MapView.Scale.html>
    //
    // The 'tracking' argument determines if the map will track its location on the map it displays.
    // This is often the player holding the map's location.
    //
    // Note that all maps have a size of 128x128.
    //
    // The file path is relative to the 'plugins/Denizen/images/' folder.
    // Instead of a local file path, an http(s) URL can be used, which will automatically download the image from the URL given.
    // If the file path points to a .gif, the map will automatically be animated.
    //
    // Use escaping to let the image and text arguments have tags based on the player viewing the map.
    //
    // Custom maps will persist over restarts using the 'maps.yml' save file in the Denizen plugins folder.
    //
    // @Tags
    // <entry[saveName].created_map> returns the map created by the 'new:' argument if used.
    //
    // @Usage
    // Use to add an auto-resized background image to map 3.
    // - map 3 image:my_map_images/my_background.png resize
    //
    // @Usage
    // Use to add an image with the top-left corner at the center of a new map.
    // - map new:WorldTag image:my_map_images/my_center_image.png x:64 y:64 save:map
    // - give filled_map[map=<entry[map].created_map>]
    //
    // @Usage
    // Use to reset map 3 to be centered at the player's location.
    // - map 3 reset:<player.location>
    //
    // @Usage
    // Use to remove any custom renderers on map 3 and then apply the contents of the named <@link language Map Script Containers> to map 3.
    // - map 3 script:Map_Script_Name
    //
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("map-id") @ArgLinear @ArgDefaultNull ElementTag id,
                                   @ArgName("new") @ArgPrefixed @ArgDefaultNull WorldTag create,
                                   @ArgName("reset") @ArgPrefixed @ArgDefaultNull LocationTag resetLoc,
                                   @ArgName("reset_to_blank") boolean resetEmpty,
                                   @ArgName("scale") @ArgPrefixed @ArgDefaultNull MapView.Scale scale,
                                   @ArgName("tracking") boolean tracking,
                                   @ArgName("image") @ArgPrefixed @ArgDefaultNull String image,
                                   @ArgName("resize") boolean resize,
                                   @ArgName("script") @ArgPrefixed @ArgDefaultNull ScriptTag script,
                                   @ArgName("dot") @ArgPrefixed @ArgDefaultNull ColorTag dot,
                                   @ArgName("radius") @ArgPrefixed @ArgDefaultText("-1") int radius,
                                   @ArgName("x") @ArgPrefixed @ArgDefaultText("0") double x,
                                   @ArgName("y") @ArgPrefixed @ArgDefaultText("0") double y,
                                   @ArgName("width") @ArgPrefixed @ArgDefaultText("-1") int width,
                                   @ArgName("height") @ArgPrefixed @ArgDefaultText("-1") int height,
                                   @ArgName("text") @ArgPrefixed @ArgDefaultNull ElementTag text) {
        if (create == null && id == null) {
            throw new InvalidArgumentsRuntimeException("Must specify a map ID or create a new map!");
        }
        if (resetLoc == null && image == null && script == null && dot == null && text == null && !resetEmpty) {
            throw new InvalidArgumentsRuntimeException("Must specify a valid action to perform!");
        }
        MapView map;
        if (create != null) {
            map = Bukkit.getServer().createMap(create.getWorld());
            scriptEntry.saveObject("created_map", new ElementTag(map.getId()));
            Debug.echoDebug(scriptEntry, "Created map with id " + map.getId() + ".");
        }
        else { // id != null
            map = Bukkit.getServer().getMap(id.asInt());
            if (map == null) {
                Debug.echoError("No map found for ID '" + id.asInt() + "'!");
                return;
            }
        }
        if (resetEmpty || resetLoc != null) {
            map.setTrackingPosition(tracking);
            if (scale != null) {
                map.setScale(scale);
            }
            for (MapRenderer renderer : DenizenMapManager.removeDenizenRenderers(map)) {
                map.addRenderer(renderer);
            }
            if (resetLoc != null) {
                map.setCenterX(resetLoc.getBlockX());
                map.setCenterZ(resetLoc.getBlockZ());
                map.setWorld(resetLoc.getWorld());
            }
        }
        if (script != null) {
            DenizenMapManager.removeDenizenRenderers(map);
            ((MapScriptContainer) script.getContainer()).applyTo(map);
        }
        DenizenMapRenderer dmr = DenizenMapManager.getDenizenRenderer(map);
        if (image != null) {
            width = formatSize(width, resize);
            height = formatSize(height, resize);
            if (CoreUtilities.toLowerCase(image).endsWith(".gif")) {
                dmr.autoUpdate = true;
            }
            dmr.addObject(new MapImage(dmr, String.valueOf(x), String.valueOf(y), "true", false, image, width, height));
            dmr.hasChanged = true;
        }
        if (dot != null) {
            dmr.addObject(new MapDot(String.valueOf(x), String.valueOf(y), "true", false, String.valueOf(radius), dot.toString()));
            dmr.hasChanged = true;
        }
        if (text != null) {
            dmr.addObject(new MapText(String.valueOf(x), String.valueOf(y), "true", false, text.asString(), null, null, null, null));
            dmr.hasChanged = true;
        }
    }

    public static int formatSize(int size, boolean resize) {
        if (size != -1) {
            return size;
        }
        return resize ? 128 : 0;
    }
}

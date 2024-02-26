package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.scripts.containers.core.MapScriptContainer;
import com.denizenscript.denizen.utilities.maps.*;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.List;

public class MapCommand extends AbstractCommand {

    public MapCommand() {
        setName("map");
        setSyntax("map [<#>/new:<world>] (reset:<location>) (scale:<value>) (tracking) (image:<file>) (resize) (script:<script>) (dot:<color>) (radius:<#>) (x:<#>) (y:<#>) (text:<text>)");
        setRequiredArguments(2, 10);
        isProcedural = false;
        setPrefixesHandled("dot", "radius", "image", "script", "x", "y", "reset", "new", "text", "scale");
        setBooleansHandled("resize", "tracking");
    }

    // <--[command]
    // @Name Map
    // @Syntax map [<#>/new:<world>] (reset:<location>) (scale:<value>) (tracking) (image:<file>) (resize) (script:<script>) (dot:<color>) (radius:<#>) (x:<#>) (y:<#>) (text:<text>)
    // @Required 2
    // @Maximum 10
    // @Short Modifies a new or existing map by adding images or text.
    // @Group item
    //
    // @Description
    // This command modifies an existing map, or creates a new one. Using this will override existing non-Denizen map renderers with Denizen's custom map renderer.
    //
    // You must specify at least one of 'reset', 'script', 'image', 'dot', 'text'. You can specify multiple at once if you prefer.
    //
    // When using 'reset', you can specify optionally 'scale' and/or 'tracking'.
    // When using 'image' you can optionally specify 'resize'.
    // When using 'dot', you can specify any valid ColorTag (it will be compressed to map's color space), and you can optionally also specify 'radius' as a number.
    //    Use "radius:0" with dot to set on a single pixel. 1 or higher will make a circle centered on the x/y given.
    //
    // You can reset this at any time by using the 'reset:<location>' argument, which will remove all
    // images and texts on the map and show the default world map at the specified location.
    // You can also specify 'reset' without a location.
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

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("map-id")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("map-id", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("map-id");
        WorldTag create = scriptEntry.argForPrefix("new", WorldTag.class, true);
        LocationTag resetLoc = scriptEntry.argForPrefix("reset", LocationTag.class, true);
        ElementTag image = scriptEntry.argForPrefixAsElement("image", null);
        boolean resize = scriptEntry.argAsBoolean("resize");
        ScriptTag script = scriptEntry.argForPrefix("script", ScriptTag.class, true);
        ElementTag width = scriptEntry.argForPrefixAsElement("width", null);
        ElementTag height = scriptEntry.argForPrefixAsElement("height", null);
        ElementTag scale = scriptEntry.argForPrefixAsElement("scale", null);
        boolean tracking = scriptEntry.argAsBoolean("tracking");
        ElementTag x = scriptEntry.argForPrefixAsElement("x", "0");
        ElementTag y = scriptEntry.argForPrefixAsElement("y", "0");
        ColorTag dot = scriptEntry.argForPrefix("dot", ColorTag.class, true);
        ElementTag radius = scriptEntry.argForPrefixAsElement("radius", null);
        ElementTag text = scriptEntry.argForPrefixAsElement("text", null);
        if (!x.isFloat() || !y.isFloat()) {
            throw new InvalidArgumentsRuntimeException("Invalid X or Y value!");
        }
        if (scale != null && !scale.matchesEnum(MapView.Scale.class)) {
            throw new InvalidArgumentsRuntimeException("Invalid scale - doesn't match scale enum!");
        }
        if (create == null && id == null) {
            throw new InvalidArgumentsRuntimeException("Must specify a map ID or create a new map!");
        }
        if (resetLoc == null && image == null && script == null && dot == null && text == null) {
            throw new InvalidArgumentsRuntimeException("Must specify a valid action to perform!");
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, create, resetLoc, image, script, dot, radius, scale, db("resize", resize), db("tracking", tracking), width, height, x, y, text);
        }
        MapView map;
        if (create != null) {
            map = Bukkit.getServer().createMap(create.getWorld());
            scriptEntry.saveObject("created_map", new ElementTag(map.getId()));
            Debug.echoDebug(scriptEntry, "Created map with id " + map.getId() + ".");
        }
        else { // id != null
            map = Bukkit.getServer().getMap((short) id.asInt());
            if (map == null) {
                Debug.echoError("No map found for ID '" + id.asInt() + "'!");
                return;
            }
        }
        if (resetLoc != null) {
            map.setTrackingPosition(tracking);
            if (scale != null) {
                map.setScale(MapView.Scale.valueOf(scale.asString().toUpperCase()));
            }
            List<MapRenderer> oldRenderers = DenizenMapManager.removeDenizenRenderers(map);
            for (MapRenderer renderer : oldRenderers) {
                map.addRenderer(renderer);
            }
            map.setCenterX(resetLoc.getBlockX());
            map.setCenterZ(resetLoc.getBlockZ());
            map.setWorld(resetLoc.getWorld());
        }
        if (script != null) {
            DenizenMapManager.removeDenizenRenderers(map);
            ((MapScriptContainer) script.getContainer()).applyTo(map);
        }
        if (image != null) {
            DenizenMapRenderer dmr = DenizenMapManager.getDenizenRenderer(map);
            int wide = width != null ? width.asInt() : resize ? 128 : 0;
            int high = height != null ? height.asInt() : resize ? 128 : 0;
            if (image.asLowerString().endsWith(".gif")) {
                dmr.autoUpdate = true;
            }
            dmr.addObject(new MapImage(dmr, x.asString(), y.asString(), "true", false, image.asString(), wide, high));
            dmr.hasChanged = true;
        }
        if (dot != null) {
            DenizenMapRenderer dmr = DenizenMapManager.getDenizenRenderer(map);
            dmr.addObject(new MapDot(x.asString(), y.asString(), "true", false, radius == null ? "1" : radius.toString(), dot.toString()));
            dmr.hasChanged = true;
        }
        if (text != null) {
            DenizenMapRenderer dmr = DenizenMapManager.getDenizenRenderer(map);
            dmr.addObject(new MapText(x.asString(), y.asString(), "true", false, text.asString(), null, null, null, null));
            dmr.hasChanged = true;
        }
    }
}

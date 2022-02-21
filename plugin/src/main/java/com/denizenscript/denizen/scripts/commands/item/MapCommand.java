package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.scripts.containers.core.MapScriptContainer;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.maps.DenizenMapManager;
import com.denizenscript.denizen.utilities.maps.DenizenMapRenderer;
import com.denizenscript.denizen.utilities.maps.MapImage;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.List;

public class MapCommand extends AbstractCommand {

    public MapCommand() {
        setName("map");
        setSyntax("map [<#>/new:<world>] (reset:<location>) (scale:<value>) (tracking) (image:<file>) (resize) (script:<script>) (x:<#>) (y:<#>)");
        setRequiredArguments(2, 7);
        isProcedural = false;
    }

    // <--[command]
    // @Name Map
    // @Syntax map [<#>/new:<world>] (reset:<location>) (scale:<value>) (tracking) (image:<file>) (resize) (script:<script>) (x:<#>) (y:<#>)
    // @Required 2
    // @Maximum 7
    // @Short Modifies a new or existing map by adding images or text.
    // @Group item
    //
    // @Description
    // This command modifies an existing map, or creates a new one. Using this will override existing non-Denizen map renderers with Denizen's custom map renderer.
    //
    // You must specify at least one of 'reset', 'script', or 'image'. You can specify multiple at once if you prefer.
    //
    // When using 'reset', you can specify optionally 'scale' and/or 'tracking'.
    // When using 'image' you can optionally specify 'resize'.
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
    // Use to add an auto-resized background image to map 3
    // - map 3 image:my_map_images/my_background.png resize
    //
    // @Usage
    // Use to add an image with the top-left corner at the center of a new map
    // - map new:WorldTag image:my_map_images/my_center_image.png x:64 y:64 save:map
    // - give filled_map[map=<entry[map].created_map>]
    //
    // @Usage
    // Reset map to have the center at the player's location
    // - map 3 reset:<player.location>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("new")
                    && arg.matchesPrefix("new")
                    && arg.matchesArgumentType(WorldTag.class)) {
                scriptEntry.addObject("new", arg.asType(WorldTag.class));
            }
            else if (!scriptEntry.hasObject("reset-loc")
                    && arg.matchesPrefix("r", "reset")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("reset-loc", arg.asType(LocationTag.class));
                scriptEntry.addObject("reset", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("image")
                    && arg.matchesPrefix("i", "img", "image")) {
                scriptEntry.addObject("image", arg.asElement());
            }
            else if (!scriptEntry.hasObject("resize")
                    && arg.matches("resize")) {
                scriptEntry.addObject("resize", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("width")
                    && arg.matchesPrefix("width")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("width", arg.asElement());
            }
            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("height", arg.asElement());
            }
            else if (!scriptEntry.hasObject("scale")
                    && arg.matchesPrefix("scale")
                    && arg.matchesEnum(MapView.Scale.class)) {
                scriptEntry.addObject("scale", arg.asElement());
            }
            else if (!scriptEntry.hasObject("tracking")
                    && arg.matches("tracking")) {
                scriptEntry.addObject("tracking", new ElementTag("true"));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesPrefix("s", "script")
                    && arg.matchesArgumentType(ScriptTag.class)) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (!scriptEntry.hasObject("x-value")
                    && arg.matchesPrefix("x")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("x-value", arg.asElement());
            }
            else if (!scriptEntry.hasObject("y-value")
                    && arg.matchesPrefix("y")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("y-value", arg.asElement());
            }
            else if (!scriptEntry.hasObject("map-id")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("map-id", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }

        }
        if (!scriptEntry.hasObject("map-id") && !scriptEntry.hasObject("new")) {
            throw new InvalidArgumentsException("Must specify a map ID or create a new map!");
        }
        if (!scriptEntry.hasObject("reset")
                && !scriptEntry.hasObject("reset-loc")
                && !scriptEntry.hasObject("image")
                && !scriptEntry.hasObject("script")) {
            throw new InvalidArgumentsException("Must specify a valid action to perform!");
        }
        scriptEntry.defaultObject("reset", new ElementTag(false)).defaultObject("resize", new ElementTag(false))
                .defaultObject("x-value", new ElementTag(0)).defaultObject("y-value", new ElementTag(0));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag id = scriptEntry.getElement("map-id");
        WorldTag create = scriptEntry.getObjectTag("new");
        ElementTag reset = scriptEntry.getElement("reset");
        LocationTag resetLoc = scriptEntry.getObjectTag("reset-loc");
        ElementTag image = scriptEntry.getElement("image");
        ScriptTag script = scriptEntry.getObjectTag("script");
        ElementTag resize = scriptEntry.getElement("resize");
        ElementTag width = scriptEntry.getElement("width");
        ElementTag height = scriptEntry.getElement("height");
        ElementTag scale = scriptEntry.getElement("scale");
        ElementTag tracking = scriptEntry.getElement("tracking");
        ElementTag x = scriptEntry.getElement("x-value");
        ElementTag y = scriptEntry.getElement("y-value");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), id, create, reset, resetLoc, image, script, resize, width, height, x, y);
        }
        MapView map;
        if (create != null) {
            map = Bukkit.getServer().createMap(create.getWorld());
            scriptEntry.addObject("created_map", new ElementTag(map.getId()));
            Debug.echoDebug(scriptEntry, "Created map with id " + map.getId() + ".");
        }
        else if (id != null) {
            map = Bukkit.getServer().getMap((short) id.asInt());
            if (map == null) {
                Debug.echoError("No map found for ID '" + id.asInt() + "'!");
                return;
            }
        }
        else { // not possible
            return;
        }
        if (reset.asBoolean()) {
            if (tracking != null) {
                map.setTrackingPosition(true);
            }
            if (scale != null) {
                map.setScale(MapView.Scale.valueOf(scale.asString().toUpperCase()));
            }
            List<MapRenderer> oldRenderers = DenizenMapManager.removeDenizenRenderers(map);
            for (MapRenderer renderer : oldRenderers) {
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
        if (image != null) {
            DenizenMapRenderer dmr = DenizenMapManager.getDenizenRenderer(map);
            int wide = width != null ? width.asInt() : resize.asBoolean() ? 128 : 0;
            int high = height != null ? height.asInt() : resize.asBoolean() ? 128 : 0;
            if (CoreUtilities.toLowerCase(image.asString()).endsWith(".gif")) {
                dmr.autoUpdate = true;
            }
            dmr.addObject(new MapImage(dmr, x.asString(), y.asString(), "true", false, image.asString(), wide, high));
            dmr.hasChanged = true;
        }
    }
}

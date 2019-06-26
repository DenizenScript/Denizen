package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.scripts.containers.core.MapScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.maps.DenizenMapManager;
import net.aufdemrand.denizen.utilities.maps.DenizenMapRenderer;
import net.aufdemrand.denizen.utilities.maps.MapAnimatedImage;
import net.aufdemrand.denizen.utilities.maps.MapImage;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.List;

public class MapCommand extends AbstractCommand {

    // <--[command]
    // @Name Map
    // @Syntax map [<#>/new:<world>] [reset:<location>/image:<file> (resize)/script:<script>] (x:<#>) (y:<#>)
    // @Required 2
    // @Short Modifies a new or existing map by adding images or text.
    // @Group item
    //
    // @Description
    // This command modifies an existing map, or creates a new one. Using this will override existing
    // non-Denizen map renderers with Denizen's custom map renderer.
    // You can reset this at any time by using the 'reset:<location>' argument, which will remove all
    // images and texts on the map and show the default world map at the specified location.
    // Note that all maps have a size of 128x128.
    // The file path is relative to the 'plugins/Denizen/images/' folder.
    // Use escaping to let the image and text arguments have tags based on the player viewing the map.
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
    // - map new:w@world image:my_map_images/my_center_image.png x:64 y:64
    //
    // @Usage
    // Reset map to have the center at the player's location
    // - map 3 reset:<player.location>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("new")
                    && arg.matchesPrefix("new")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("new", arg.asType(dWorld.class));
            }
            else if (!scriptEntry.hasObject("reset-loc")
                    && arg.matchesPrefix("r", "reset")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("reset-loc", arg.asType(dLocation.class));
                scriptEntry.addObject("reset", new Element(true));
            }
            else if (!scriptEntry.hasObject("reset")
                    && arg.matches("reset")) {
                scriptEntry.addObject("reset", new Element(true));
            }
            else if (!scriptEntry.hasObject("image")
                    && arg.matchesPrefix("i", "img", "image")) {
                scriptEntry.addObject("image", arg.asElement());
            }
            else if (!scriptEntry.hasObject("resize")
                    && arg.matches("resize")) {
                scriptEntry.addObject("resize", new Element(true));
            }
            else if (!scriptEntry.hasObject("width")
                    && arg.matchesPrefix("width")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("width", arg.asElement());
            }
            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("height", arg.asElement());
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesPrefix("s", "script")
                    && arg.matchesArgumentType(dScript.class)) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (!scriptEntry.hasObject("x-value")
                    && arg.matchesPrefix("x")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("x-value", arg.asElement());
            }
            else if (!scriptEntry.hasObject("y-value")
                    && arg.matchesPrefix("y")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("y-value", arg.asElement());
            }
            else if (!scriptEntry.hasObject("map-id")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("map-id", arg.asElement());
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

        scriptEntry.defaultObject("reset", new Element(false)).defaultObject("resize", new Element(false))
                .defaultObject("x-value", new Element(0)).defaultObject("y-value", new Element(0));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element id = scriptEntry.getElement("map-id");
        dWorld create = scriptEntry.getdObject("new");
        Element reset = scriptEntry.getElement("reset");
        dLocation resetLoc = scriptEntry.getdObject("reset-loc");
        Element image = scriptEntry.getElement("image");
        dScript script = scriptEntry.getdObject("script");
        Element resize = scriptEntry.getElement("resize");
        Element width = scriptEntry.getElement("width");
        Element height = scriptEntry.getElement("height");
        Element x = scriptEntry.getElement("x-value");
        Element y = scriptEntry.getElement("y-value");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), (id != null ? id.debug() : "") + (create != null ? create.debug() : "")
                    + reset.debug() + (resetLoc != null ? resetLoc.debug() : "") + (image != null ? image.debug() : "")
                    + (script != null ? script.debug() : "") + resize.debug() + (width != null ? width.debug() : "")
                    + (height != null ? height.debug() : "") + x.debug() + y.debug());

        }

        MapView map = null;
        if (create != null) {
            map = Bukkit.getServer().createMap(create.getWorld());
            scriptEntry.addObject("created_map", new Element(map.getId()));
        }
        else if (id != null) {
            map = Bukkit.getServer().getMap((short) id.asInt());
            if (map == null) {
                dB.echoError("No map found for ID '" + id.asInt() + "'!");
                return;
            }
        }
        else {
            dB.echoError("The map command failed somehow! Report this to a developer!");
            return;
        }

        if (reset.asBoolean()) {
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
        else if (script != null) {
            DenizenMapManager.removeDenizenRenderers(map);
            ((MapScriptContainer) script.getContainer()).applyTo(map);
        }
        else {
            DenizenMapRenderer dmr = DenizenMapManager.getDenizenRenderer(map);
            if (image != null) {
                int wide = width != null ? width.asInt() : resize.asBoolean() ? 128 : 0;
                int high = height != null ? height.asInt() : resize.asBoolean() ? 128 : 0;
                if (CoreUtilities.toLowerCase(image.asString()).endsWith(".gif")) {
                    dmr.autoUpdate = true;
                    dmr.addObject(new MapAnimatedImage(x.asString(), y.asString(), "true", false, image.asString(),
                            wide, high));
                }
                else {
                    dmr.addObject(new MapImage(x.asString(), y.asString(), "true", false, image.asString(),
                            wide, high));
                }
            }
        }

    }
}

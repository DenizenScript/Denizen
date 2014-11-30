package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.maps.DenizenMapRenderer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import org.bukkit.Bukkit;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.io.File;
import java.util.List;

public class MapCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("new")
                    && arg.matchesPrefix("new")
                    && arg.matchesArgumentType(dWorld.class)) {
                scriptEntry.addObject("new", arg.asType(dWorld.class));
            }

            else if (!scriptEntry.hasObject("reset")
                    && arg.matchesPrefix("r", "reset")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("reset", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("image")
                    && arg.matchesPrefix("i", "img","image")) {
                scriptEntry.addObject("image", arg.asElement());
            }

            else if (!scriptEntry.hasObject("resize")
                    && arg.matches("resize")) {
                scriptEntry.addObject("resize", Element.TRUE);
            }

            else if (!scriptEntry.hasObject("text")
                    && arg.matchesPrefix("t", "text")) {
                scriptEntry.addObject("text", arg.asElement());
            }

            else if (!scriptEntry.hasObject("x-value")
                    && arg.matchesPrefix("x")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("x-value", arg.asElement());
            }

            else if (!scriptEntry.hasObject("y-value")
                    && arg.matchesPrefix("y")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("y-value", arg.asElement());
            }

            else if (!scriptEntry.hasObject("map-id")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("map-id", arg.asElement());
            }

        }

        if (!scriptEntry.hasObject("map-id") && !scriptEntry.hasObject("new"))
            throw new InvalidArgumentsException("Must specify a map ID or create a new map!");

        if (!scriptEntry.hasObject("reset")
                && !scriptEntry.hasObject("image")
                && !scriptEntry.hasObject("text"))
            throw new InvalidArgumentsException("Must specify value to modify!");

        scriptEntry.defaultObject("x-value", new Element(0)).defaultObject("y-value", new Element(0))
                .defaultObject("resize", Element.FALSE);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element id = scriptEntry.getElement("map-id");
        dWorld create = scriptEntry.getdObject("new");
        dLocation reset = scriptEntry.getdObject("reset");
        Element image = scriptEntry.getElement("image");
        Element resize = scriptEntry.getElement("resize");
        Element text = scriptEntry.getElement("text");
        Element x = scriptEntry.getElement("x-value");
        Element y = scriptEntry.getElement("y-value");

        dB.report(scriptEntry, getName(), (id != null ? id.debug() : "") + (create != null ? create.debug() : "")
                + (reset != null ? reset.debug() : "") + (image != null ? image.debug() : "") + resize.debug()
                + (text != null ? text.debug() : "") + x.debug() + y.debug());

        MapView map = null;
        if (create != null) {
            map = Bukkit.getServer().createMap(create.getWorld());
            scriptEntry.addObject("created_map", new Element(map.getId()));
        }
        else if (id != null) {
            map = Bukkit.getServer().getMap((short) id.asInt());
            if (map == null)
                throw new CommandExecutionException("No map found for ID '" + id.asInt() + "'!");
        }
        else {
            throw new CommandExecutionException("The map command failed somehow! Report this to a developer!");
        }

        if (reset != null) {
            for (MapRenderer renderer : map.getRenderers()) {
                if (renderer instanceof DenizenMapRenderer) {
                    map.removeRenderer(renderer);
                    for (MapRenderer oldRenderer : ((DenizenMapRenderer) renderer).getOldRenderers())
                        map.addRenderer(oldRenderer);
                    map.setCenterX(reset.getBlockX());
                    map.setCenterZ(reset.getBlockZ());
                    map.setWorld(reset.getWorld());
                }
            }
        }
        else {
            DenizenMapRenderer dmr = null;
            List<MapRenderer> oldRendererList = map.getRenderers();
            for (MapRenderer renderer : oldRendererList) {
                if (!(renderer instanceof DenizenMapRenderer) || dmr != null)
                    map.removeRenderer(renderer);
                else
                    dmr = (DenizenMapRenderer) renderer;
            }
            if (dmr == null) {
                dmr = new DenizenMapRenderer(oldRendererList);
                map.addRenderer(dmr);
            }
            if (image != null)
                dmr.addImage(x.asInt(), y.asInt(), new File(DenizenAPI.getCurrentInstance().getDataFolder(),
                        image.asString()).getPath(), resize.asBoolean());
            else if (text != null)
                dmr.addText(x.asInt(), y.asInt(), text.asString());
        }

    }
}

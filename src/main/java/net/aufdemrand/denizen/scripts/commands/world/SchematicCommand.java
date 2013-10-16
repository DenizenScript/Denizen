package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

import java.io.File;
import java.net.URLDecoder;
import java.util.Map;

public class SchematicCommand extends AbstractCommand implements Listener {

    @Override
    public void onEnable() {
        if (Depends.worldEdit != null)
            Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }


    private enum Type { LOAD, UNLOAD, ROTATE, PASTE }
    public static Map<String, CuboidClipboard> schematics;

    // TODO: Create schematic from dCuboid
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // - schematic load name:Potato
        // - schematic unload name:Potato
        // - schematic rotate name:Potato angle:90
        // - schematic paste name:Potato location:x,y,z,world (noair)
        // - schematic [load/unload/rotate/paste] [name:<name>] (angle:<#>) (<location>) (noair)

        if (Depends.worldEdit == null) {
            dB.echoError("This command requires WorldEdit!");
            return;
        } // TODO: Make independent!

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                && arg.matchesEnum(Type.values()))
                scriptEntry.addObject("type", new Element(arg.raw_value.toUpperCase()));

            else if (!scriptEntry.hasObject("name")
                    && arg.matchesPrefix("name"))
                scriptEntry.addObject("name", arg.asElement());

            else if (!scriptEntry.hasObject("angle")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("angle", arg.asElement());

            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("noair")
                    && arg.matches("noair"))
                scriptEntry.addObject("noair", Element.TRUE);

            else
                dB.echoError(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        if (!scriptEntry.hasObject("type"))
          throw new InvalidArgumentsException(dB.Messages.ERROR_MISSING_OTHER, "type");

        if (!scriptEntry.hasObject("name"))
            throw new InvalidArgumentsException(dB.Messages.ERROR_MISSING_OTHER, "name");

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element angle = scriptEntry.getElement("angle");
        Element type = scriptEntry.getElement("type");
        Element name = scriptEntry.getElement("name");
        Element noair = scriptEntry.getElement("noair");
        dLocation location = (dLocation) scriptEntry.getObject("location");

        dB.report(getName(), type.debug()
                           + name.debug()
                           + (location != null ? location.debug(): "")
                           + (angle != null ? angle.debug(): "")
                           + (noair != null ? noair.debug(): ""));

        CuboidClipboard cc;
        switch (Type.valueOf(type.asString())) {
            case LOAD:
                if (schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError("Schematic file " + name.asString() + " is already loaded.");
                    return;
                }
                try {
                    String directory = URLDecoder.decode(System.getProperty("user.dir"));
                    File f = new File(directory + "/plugins/Denizen/schematics/" + name.asString() + ".schematic");
                    if (!f.exists()) {
                        dB.echoError("Schematic file " + name.asString() + " does not exist.");
                        return;
                    }
                    cc = SchematicFormat.MCEDIT.load(f);
                }
                catch (Exception ex) {
                    dB.echoError("Error loading schematic file " + name.asString() + ".");
                    return;
                }
                schematics.put(name.asString().toUpperCase(), cc);
                break;
            case UNLOAD:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError("Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.remove(name.asString().toUpperCase());
                break;
            case ROTATE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError("Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                if (angle == null) {
                    dB.echoError(dB.Messages.ERROR_MISSING_OTHER, "ANGLE");
                    return;
                }
                schematics.get(name.asString().toUpperCase()).rotate2D(angle.asInt());
                break;
            case PASTE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError("Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                if (location == null) {
                    dB.echoError(dB.Messages.ERROR_MISSING_OTHER, "LOCATION");
                    return;
                }
                try {
                    schematics.get(name.asString().toUpperCase())
                        .paste(new EditSession(new BukkitWorld(location.getWorld()), 99999999),
                                new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ()),
                                noair != null);
                }
                catch (Exception ex) {
                    dB.echoError("Exception while pasting " + name.asString());
                }
                break;
        }
    }
    @EventHandler
    public void schematicTags(ReplaceableTagEvent event) {

        if (!event.matches("schematic, schem")) return;

        if (!event.hasNameContext()) {
            return;
        }

        String id = event.getNameContext().toUpperCase();

        if (!schematics.containsKey(id)) {
            dB.echoError("Schematic file " + id + " is not loaded.");
            return;
        }

        CuboidClipboard cc = schematics.get(id);

        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        //
        // Check attributes
        //

        // <--[tag]
        // @attribute <schematic[<name>].height>
        // @returns Element(Number)
        // @description
        // Returns the height of the schematic.
        // -->
        if (attribute.startsWith("height")) {
            event.setReplaced(new Element(cc.getHeight())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].length>
        // @returns Element(Number)
        // @description
        // Returns the length of the schematic.
        // -->
        if (attribute.startsWith("length")) {
            event.setReplaced(new Element(cc.getLength())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].width>
        // @returns Element(Number)
        // @description
        // Returns the width of the schematic.
        // -->
        if (attribute.startsWith("width")) {
            event.setReplaced(new Element(cc.getWidth())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].block[<location>]>
        // @returns dMaterial
        // @description
        // Returns the material for the block at the location in the schematic.
        // -->
        if (attribute.startsWith("block")) {
            if (attribute.hasContext(1) && dLocation.matches(attribute.getContext(1))) {
                dLocation location = dLocation.valueOf(attribute.getContext(1));
                BaseBlock bb = cc.getBlock(new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ()));
                event.setReplaced(dMaterial.valueOf(bb.getType() + ":" + bb.getData()) // TODO: Better representation of the block
                        .getAttribute(attribute.fulfill(1)));
                return;
            }
        }

        // <--[tag]
        // @attribute <schematic[<name>].origin>
        // @returns dLocation
        // @description
        // Returns the origin location of the schematic.
        // -->
        if (attribute.startsWith("origin")) {
            event.setReplaced(new dLocation(Bukkit.getWorlds().get(0),
                    cc.getOrigin().getX(), cc.getOrigin().getY(), cc.getOrigin().getZ())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].offset>
        // @returns dLocation
        // @description
        // Returns the offset location of the schematic.
        // -->
        if (attribute.startsWith("offset")) {
            event.setReplaced(new dLocation(Bukkit.getWorlds().get(0),
                    cc.getOffset().getX(), cc.getOffset().getY(), cc.getOffset().getZ())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].blocks>
        // @returns Element(Number)
        // @description
        // Returns the number of blocks in the schematic.
        // -->
        if (attribute.startsWith("blocks")) {
            event.setReplaced(new Element(cc.getHeight() * cc.getWidth() * cc.getLength())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }
    }
}

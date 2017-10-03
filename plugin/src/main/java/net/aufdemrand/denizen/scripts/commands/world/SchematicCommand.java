package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.blocks.CuboidBlockSet;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class SchematicCommand extends AbstractCommand implements Holdable, Listener {

    @Override
    public void onEnable() {
        TagManager.registerTagEvents(this);
        schematics = new HashMap<String, CuboidBlockSet>();
        noPhys = false;
        Bukkit.getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public static boolean noPhys = false;

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (noPhys) {
            event.setCancelled(true);
        }
    }


    private enum Type {CREATE, LOAD, UNLOAD, ROTATE, PASTE, SAVE, FLIP_X, FLIP_Y, FLIP_Z}

    public static Map<String, CuboidBlockSet> schematics;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", new Element(arg.raw_value.toUpperCase()));
            }
            else if (!scriptEntry.hasObject("name")
                    && arg.matchesPrefix("name")) {
                scriptEntry.addObject("name", arg.asElement());
            }
            else if (!scriptEntry.hasObject("filename")
                    && arg.matchesPrefix("filename")) {
                scriptEntry.addObject("filename", arg.asElement());
            }
            else if (!scriptEntry.hasObject("angle")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("angle", arg.asElement());
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("cuboid")
                    && arg.matchesArgumentType(dCuboid.class)) {
                scriptEntry.addObject("cuboid", arg.asType(dCuboid.class));
            }
            else if (!scriptEntry.hasObject("delayed")
                    && arg.matches("delayed")) {
                scriptEntry.addObject("delayed", new Element("true"));
            }
            else if (!scriptEntry.hasObject("noair")
                    && arg.matches("noair")) {
                scriptEntry.addObject("noair", new Element("true"));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("type")) {
            throw new InvalidArgumentsException("Missing type argument!");
        }

        if (!scriptEntry.hasObject("name")) {
            throw new InvalidArgumentsException("Missing name argument!");
        }
    }


    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        Element angle = scriptEntry.getElement("angle");
        Element type = scriptEntry.getElement("type");
        Element name = scriptEntry.getElement("name");
        Element filename = scriptEntry.getElement("filename");
        Element noair = scriptEntry.getElement("noair");
        Element delayed = scriptEntry.getElement("delayed");
        dLocation location = scriptEntry.getdObject("location");
        dCuboid cuboid = scriptEntry.getdObject("cuboid");

        dB.report(scriptEntry, getName(), type.debug()
                + name.debug()
                + (location != null ? location.debug() : "")
                + (filename != null ? filename.debug() : "")
                + (cuboid != null ? cuboid.debug() : "")
                + (angle != null ? angle.debug() : "")
                + (noair != null ? noair.debug(): "")
                + (delayed != null ? delayed.debug() : ""));

        CuboidBlockSet set;
        Type ttype = Type.valueOf(type.asString());
        if (scriptEntry.shouldWaitFor() && ttype != Type.PASTE) {
            dB.echoError("Tried to wait for a non-paste schematic command.");
            scriptEntry.setFinished(true);
        }
        String fname = filename != null ? filename.asString(): name.asString();
        switch (ttype) {
            case CREATE:
                if (schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is already loaded.");
                    return;
                }
                if (cuboid == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Missing cuboid argument!");
                    return;
                }
                if (location == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Missing origin location argument!");
                    return;
                }
                try {
                    // TODO: Make me waitable!
                    set = new CuboidBlockSet(cuboid, location);
                    schematics.put(name.asString().toUpperCase(), set);
                }
                catch (Exception ex) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Error creating schematic object " + name.asString() + ".");
                    dB.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
            case LOAD:
                if (schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is already loaded.");
                    return;
                }
                try {
                    String directory = URLDecoder.decode(System.getProperty("user.dir"));
                    File f = new File(directory + "/plugins/Denizen/schematics/" + fname + ".schematic");
                    if (!f.exists()) {
                        dB.echoError("Schematic file " + fname + " does not exist. Are you sure it's in " + directory + "/plugins/Denizen/schematics/?");
                        return;
                    }
                    InputStream fs = new FileInputStream(f);
                    // TODO: Make me waitable!
                    set = CuboidBlockSet.fromMCEditStream(fs);
                    fs.close();
                    schematics.put(name.asString().toUpperCase(), set);
                }
                catch (Exception ex) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Error loading schematic file " + name.asString() + ".");
                    dB.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
            case UNLOAD:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.remove(name.asString().toUpperCase());
                break;
            case ROTATE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                if (angle == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Missing angle argument!");
                    return;
                }
                // TODO: Make me waitable!
                int ang = angle.asInt();
                while (ang < 0) {
                    ang = 360 + ang;
                }
                while (ang > 360) {
                    ang -= 360;
                }
                while (ang > 0) {
                    ang -= 90;
                    schematics.get(name.asString().toUpperCase()).rotateOne();
                }
                break;
            case FLIP_X:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.get(name.asString().toUpperCase()).flipX();
                break;
            case FLIP_Y:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.get(name.asString().toUpperCase()).flipY();
                break;
            case FLIP_Z:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.get(name.asString().toUpperCase()).flipZ();
                break;
            case PASTE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                if (location == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Missing location argument!");
                    return;
                }
                try {
                    if (delayed != null && delayed.asBoolean()) {
                        schematics.get(name.asString().toUpperCase()).setBlocksDelayed(location, new Runnable() {
                            @Override
                            public void run() {
                                scriptEntry.setFinished(true);
                            }
                        }, noair != null && noair.asBoolean());
                    }
                    else {
                        scriptEntry.setFinished(true);
                        schematics.get(name.asString().toUpperCase()).setBlocks(location, noair != null && noair.asBoolean());
                    }
                }
                catch (Exception ex) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Exception pasting schematic file " + name.asString() + ".");
                    dB.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
            case SAVE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                try {
                    set = schematics.get(name.asString().toUpperCase());
                    String directory = URLDecoder.decode(System.getProperty("user.dir"));
                    File f = new File(directory + "/plugins/Denizen/schematics/" + fname + ".schematic");
                    f.getParentFile().mkdirs();
                    // TODO: Make me waitable!
                    FileOutputStream fs = new FileOutputStream(f);
                    set.saveMCEditFormatToStream(fs);
                    fs.flush();
                    fs.close();
                }
                catch (Exception ex) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Error saving schematic file " + fname + ".");
                    dB.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
        }
    }

    @TagManager.TagEvents
    public void schematicTags(ReplaceableTagEvent event) {

        if (!event.matches("schematic", "schem")) {
            return;
        }

        String id = event.hasNameContext() ? event.getNameContext().toUpperCase(): null;

        Attribute attribute = event.getAttributes().fulfill(1);

        // <--[tag]
        // @attribute <schematic.list>
        // @returns dList(Element)
        // @description
        // Returns a list of all loaded schematics.
        // -->
        if (attribute.startsWith("list")) {
            event.setReplaced(new dList(schematics.keySet()).getAttribute(attribute.fulfill(1)));
        }

        if (id == null) {
            return;
        }

        if (!schematics.containsKey(id)) {
            // Meta below
            if (attribute.startsWith("exists")) {
                event.setReplaced(new Element(false)
                        .getAttribute(attribute.fulfill(1)));
                return;
            }

            dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null, "Schematic file " + id + " is not loaded.");
            return;
        }

        CuboidBlockSet set = schematics.get(id);

        //
        // Check attributes
        //

        // <--[tag]
        // @attribute <schematic[<name>].exists>
        // @returns Element(Boolean)
        // @description
        // Returns whether the schematic exists.
        // -->
        if (attribute.startsWith("exists")) {
            event.setReplaced(new Element(true)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].height>
        // @returns Element(Number)
        // @description
        // Returns the height (Y) of the schematic.
        // -->
        if (attribute.startsWith("height")) {
            event.setReplaced(new Element(set.y_length)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].length>
        // @returns Element(Number)
        // @description
        // Returns the length (Z) of the schematic.
        // -->
        if (attribute.startsWith("length")) {
            event.setReplaced(new Element(set.z_height)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].width>
        // @returns Element(Number)
        // @description
        // Returns the width (X) of the schematic.
        // -->
        if (attribute.startsWith("width")) {
            event.setReplaced(new Element(set.x_width)
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
                BlockData block = set.blockAt(location.getX(), location.getY(), location.getZ());
                event.setReplaced(dMaterial.getMaterialFrom(block.getMaterial(), block.getData())
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
            event.setReplaced(new dLocation(null, set.center_x, set.center_y, set.center_z)
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
            event.setReplaced(new Element(set.blocks.size())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].cuboid[<origin location>]>
        // @returns dCuboid
        // @description
        // Returns a cuboid of where the schematic would be if it was pasted at an origin.
        // -->
        if (attribute.startsWith("cuboid") && attribute.hasContext(1)) {
            dLocation origin = dLocation.valueOf(attribute.getContext(1));
            event.setReplaced(set.getCuboid(origin)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }
    }
}

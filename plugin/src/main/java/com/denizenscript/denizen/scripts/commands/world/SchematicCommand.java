package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.blocks.CuboidBlockSet;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
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

    // <--[command]
    // @Name Schematic
    // @Syntax schematic [create/load/unload/rotate/paste/save/flip_x/flip_y/flip_z] [name:<name>] (filename:<name>) (angle:<#>) (<location>) (<cuboid>) (delayed) (noair)
    // @Group World
    // @Required 2
    // @Short Creates, loads, pastes, and saves schematics (Sets of blocks).
    //
    // @Description
    // Creates, loads, pastes, and saves schematics. Schematics are files containing info about
    // blocks and the order of those blocks.
    //
    // Denizen offers a number of tools to manipulate and work with schematics.
    // Schematics can be rotated, flipped, pasted with no air, or pasted with a delay.
    // The "noair" option skips air blocks in the pasted schematics- this means those air blocks will not replace
    // any blocks in the target location.
    // The "delayed" option delays how many blocks can be pasted at once. This is recommended for large schematics.
    //
    // @Tags
    // <schematic[<name>].height>
    // <schematic[<name>].length>
    // <schematic[<name>].width>
    // <schematic[<name>].block[<location>]>
    // <schematic[<name>].origin>
    // <schematic[<name>].blocks>
    // <schematic[<name>].exists>
    // <schematic[<name>].cuboid[<origin location>]>
    // <schematic.list>
    //
    // @Usage
    // Use to create a new schematic from a cuboid and an origin location
    // - schematic create name:MySchematic cu@<player.location.sub[5,5,5]>|<player.location.add[5,5,5]> <player.location>
    //
    // @Usage
    // Use to load a schematic
    // - schematic load name:MySchematic
    //
    // @Usage
    // Use to unload a schematic
    // - schematic unload name:MySchematic
    //
    // @Usage
    // Use to paste a loaded schematic with no air blocks
    // - schematic paste name:MySchematic <player.location> noair
    //
    // @Usage
    // Use to save a created schematic
    // - schematic save name:MySchematic
    // -->

    @Override
    public void onEnable() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                schematicTags(event);
            }
        }, "schematic", "schem");
        schematics = new HashMap<>();
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

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", new ElementTag(arg.raw_value.toUpperCase()));
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
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)) {
                scriptEntry.addObject("angle", arg.asElement());
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("cuboid")
                    && arg.matchesArgumentType(CuboidTag.class)) {
                scriptEntry.addObject("cuboid", arg.asType(CuboidTag.class));
            }
            else if (!scriptEntry.hasObject("delayed")
                    && arg.matches("delayed")) {
                scriptEntry.addObject("delayed", new ElementTag("true"));
            }
            else if (!scriptEntry.hasObject("noair")
                    && arg.matches("noair")) {
                scriptEntry.addObject("noair", new ElementTag("true"));
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
    public void execute(final ScriptEntry scriptEntry) {

        ElementTag angle = scriptEntry.getElement("angle");
        ElementTag type = scriptEntry.getElement("type");
        ElementTag name = scriptEntry.getElement("name");
        ElementTag filename = scriptEntry.getElement("filename");
        ElementTag noair = scriptEntry.getElement("noair");
        ElementTag delayed = scriptEntry.getElement("delayed");
        LocationTag location = scriptEntry.getObjectTag("location");
        CuboidTag cuboid = scriptEntry.getObjectTag("cuboid");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), type.debug()
                    + name.debug()
                    + (location != null ? location.debug() : "")
                    + (filename != null ? filename.debug() : "")
                    + (cuboid != null ? cuboid.debug() : "")
                    + (angle != null ? angle.debug() : "")
                    + (noair != null ? noair.debug() : "")
                    + (delayed != null ? delayed.debug() : ""));

        }

        CuboidBlockSet set;
        Type ttype = Type.valueOf(type.asString());
        if (scriptEntry.shouldWaitFor() && ttype != Type.PASTE) {
            Debug.echoError("Tried to wait for a non-paste schematic command.");
            scriptEntry.setFinished(true);
        }
        String fname = filename != null ? filename.asString() : name.asString();
        switch (ttype) {
            case CREATE:
                if (schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is already loaded.");
                    return;
                }
                if (cuboid == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Missing cuboid argument!");
                    return;
                }
                if (location == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin location argument!");
                    return;
                }
                try {
                    // TODO: Make me waitable!
                    set = new CuboidBlockSet(cuboid, location);
                    schematics.put(name.asString().toUpperCase(), set);
                }
                catch (Exception ex) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Error creating schematic object " + name.asString() + ".");
                    Debug.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
            case LOAD:
                if (schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is already loaded.");
                    return;
                }
                try {
                    String directory = URLDecoder.decode(System.getProperty("user.dir"));
                    File f = new File(directory + "/plugins/Denizen/schematics/" + fname + ".schematic");
                    if (!Utilities.canReadFile(f)) {
                        Debug.echoError("Server config denies reading files in that location.");
                        return;
                    }
                    if (!f.exists()) {
                        Debug.echoError("Schematic file " + fname + " does not exist. Are you sure it's in " + directory + "/plugins/Denizen/schematics/?");
                        return;
                    }
                    InputStream fs = new FileInputStream(f);
                    // TODO: Make me waitable!
                    set = CuboidBlockSet.fromMCEditStream(fs);
                    fs.close();
                    schematics.put(name.asString().toUpperCase(), set);
                }
                catch (Exception ex) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Error loading schematic file " + name.asString() + ".");
                    Debug.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
            case UNLOAD:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.remove(name.asString().toUpperCase());
                break;
            case ROTATE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                if (angle == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Missing angle argument!");
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
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.get(name.asString().toUpperCase()).flipX();
                break;
            case FLIP_Y:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.get(name.asString().toUpperCase()).flipY();
                break;
            case FLIP_Z:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                schematics.get(name.asString().toUpperCase()).flipZ();
                break;
            case PASTE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                if (location == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Missing location argument!");
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
                    Debug.echoError(scriptEntry.getResidingQueue(), "Exception pasting schematic file " + name.asString() + ".");
                    Debug.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
            case SAVE:
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                try {
                    set = schematics.get(name.asString().toUpperCase());
                    String directory = URLDecoder.decode(System.getProperty("user.dir"));
                    File f = new File(directory + "/plugins/Denizen/schematics/" + fname + ".schematic");
                    if (!Utilities.canWriteToFile(f)) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Cannot edit that file!");
                        return;
                    }
                    f.getParentFile().mkdirs();
                    // TODO: Make me waitable!
                    FileOutputStream fs = new FileOutputStream(f);
                    set.saveMCEditFormatToStream(fs);
                    fs.flush();
                    fs.close();
                }
                catch (Exception ex) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Error saving schematic file " + fname + ".");
                    Debug.echoError(scriptEntry.getResidingQueue(), ex);
                    return;
                }
                break;
        }
    }

    public void schematicTags(ReplaceableTagEvent event) {

        if (!event.matches("schematic", "schem")) {
            return;
        }

        String id = event.hasNameContext() ? event.getNameContext().toUpperCase() : null;

        Attribute attribute = event.getAttributes().fulfill(1);

        // <--[tag]
        // @attribute <schematic.list>
        // @returns ListTag
        // @description
        // Returns a list of all loaded schematics.
        // -->
        if (attribute.startsWith("list")) {
            event.setReplaced(new ListTag(schematics.keySet()).getAttribute(attribute.fulfill(1)));
        }

        if (id == null) {
            return;
        }

        if (!schematics.containsKey(id)) {
            // Meta below
            if (attribute.startsWith("exists")) {
                event.setReplaced(new ElementTag(false)
                        .getAttribute(attribute.fulfill(1)));
                return;
            }

            Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null, "Schematic file " + id + " is not loaded.");
            return;
        }

        CuboidBlockSet set = schematics.get(id);

        //
        // Check attributes
        //

        // <--[tag]
        // @attribute <schematic[<name>].exists>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the schematic exists.
        // -->
        if (attribute.startsWith("exists")) {
            event.setReplaced(new ElementTag(true)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].height>
        // @returns ElementTag(Number)
        // @description
        // Returns the height (Y) of the schematic.
        // -->
        if (attribute.startsWith("height")) {
            event.setReplaced(new ElementTag(set.y_length)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].length>
        // @returns ElementTag(Number)
        // @description
        // Returns the length (Z) of the schematic.
        // -->
        if (attribute.startsWith("length")) {
            event.setReplaced(new ElementTag(set.z_height)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].width>
        // @returns ElementTag(Number)
        // @description
        // Returns the width (X) of the schematic.
        // -->
        if (attribute.startsWith("width")) {
            event.setReplaced(new ElementTag(set.x_width)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].block[<location>]>
        // @returns MaterialTag
        // @description
        // Returns the material for the block at the location in the schematic.
        // -->
        if (attribute.startsWith("block")) {
            if (attribute.hasContext(1) && LocationTag.matches(attribute.getContext(1))) {
                LocationTag location = LocationTag.valueOf(attribute.getContext(1));
                BlockData block = set.blockAt(location.getX(), location.getY(), location.getZ());
                event.setReplaced(new MaterialTag(block)
                        .getAttribute(attribute.fulfill(1)));
                return;
            }
        }

        // <--[tag]
        // @attribute <schematic[<name>].origin>
        // @returns LocationTag
        // @description
        // Returns the origin location of the schematic.
        // -->
        if (attribute.startsWith("origin")) {
            event.setReplaced(new LocationTag(null, set.center_x, set.center_y, set.center_z)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].blocks>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of blocks in the schematic.
        // -->
        if (attribute.startsWith("blocks")) {
            event.setReplaced(new ElementTag(set.blocks.size())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].cuboid[<origin location>]>
        // @returns CuboidTag
        // @description
        // Returns a cuboid of where the schematic would be if it was pasted at an origin.
        // -->
        if (attribute.startsWith("cuboid") && attribute.hasContext(1)) {
            LocationTag origin = LocationTag.valueOf(attribute.getContext(1));
            event.setReplaced(set.getCuboid(origin)
                    .getAttribute(attribute.fulfill(1)));
            return;
        }
    }
}

package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.blocks.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.TagRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SchematicCommand extends AbstractCommand implements Holdable, Listener {

    public SchematicCommand() {
        setName("schematic");
        setSyntax("schematic [create/load/unload/rotate/save/flip_x/flip_y/flip_z/paste (fake_to:<player>|... fake_duration:<duration>) (noair) (mask:<material_matcher>)] [name:<name>] (filename:<name>) (angle:<#>) (<location>) (area:<area>) (delayed) (max_delay_ms:<#>) (entities) (flags)");
        setRequiredArguments(2, 13);
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                schematicTags(event);
            }
        }, "schematic");
        schematics = new HashMap<>();
        noPhys = false;
        Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
        isProcedural = false;
        setBooleansHandled("noair", "delayed", "entities", "flags");
        setPrefixesHandled("angle", "fake_duration", "mask", "name", "filename", "max_delay_ms", "fake_to", "area");
    }

    // <--[command]
    // @Name Schematic
    // @Syntax schematic [create/load/unload/rotate/save/flip_x/flip_y/flip_z/paste (fake_to:<player>|... fake_duration:<duration>) (noair) (mask:<material_matcher>)] [name:<name>] (filename:<name>) (angle:<#>) (<location>) (area:<area>) (delayed) (max_delay_ms:<#>) (entities) (flags)
    // @Group world
    // @Required 2
    // @Maximum 13
    // @Short Creates, loads, pastes, and saves schematics (Sets of blocks).
    //
    // @Description
    // Creates, loads, pastes, and saves schematics. Schematics are files containing info about blocks and the order of those blocks.
    //
    // Denizen offers a number of tools to manipulate and work with schematics.
    // Schematics can be rotated, flipped, pasted with no air, or pasted with a delay.
    //
    // All schematic command usages must specify the "name" argument, which is a unique global identifier of the schematic in memory.
    // This will be created by "create" or "load" options, and persist in memory until "unload" is used (or the server is restarted).
    //
    // The 'create' option requires an area and a center location as input.
    // The area can be defined as any valid <@link ObjectType AreaObject>, such as a CuboidTag.
    // Note that all schematics are internally tracked as cuboids, and other area shapes will only constrain the copy region.
    // Note that the block boundaries of non-cuboid regions are defined by whether the region definition contains the center of a block.
    // This will create a new schematic in memory based on world data.
    //
    // The "rotate angle:#" and "flip_x/y/z" options will apply the change to the copy of the schematic in memory, to later be pasted or saved.
    // This will rotate the set of blocks itself, the relative origin, and any directional blocks inside the schematic.
    // Rotation angles must be a multiple of 90 degrees.
    //
    // When using 'paste', you can specify 'angle:#' to have that paste rotated, without rotating the original schematic.
    //
    // The "delayed" option makes the command non-instant. This is recommended for large schematics.
    // For 'save', 'load', and 'rotate', this processes async to prevent server lockup.
    // For 'paste' and 'create', this delays how many blocks can be processed at once, spread over many ticks.
    // Optionally, specify 'max_delay_ms' to control how many milliseconds the 'delayed' set can run for in any given tick (defaults to 50) (for create/paste only).
    //
    // The "load" option by default will load '.schem' files. If no '.schem' file is available, will attempt to load a legacy '.schematic' file instead.
    //
    // For load and save, the "filename" option is available to specify the name of the file to look for.
    // If unspecified, the filename will default to the same as the "name" input.
    //
    // The "noair" option skips air blocks in the pasted schematics- this means those air blocks will not replace any blocks in the target location.
    //
    // The "mask" option can be specified to limit what block types the schematic will be pasted over.
    // When using "create" and "mask", any block that doesn't match the mask will become a structure void.
    //
    // The "fake_to" option can be specified to cause the schematic paste to be a fake (packet-based, see <@link command showfake>)
    // block set, instead of actually modifying the blocks in the world.
    // This takes an optional duration as "fake_duration" for how long the fake blocks should remain.
    //
    // The "create" and "paste" options allow the "entities" argument to be specified - when used, entities will be copied or pasted.
    // At current time, entity types included will be: Paintings, ItemFrames, ArmorStands.
    //
    // The "create" option allows the "flags" argument to be specified - when used, block location flags will be copied.
    //
    // The schematic command is ~waitable as an alternative to 'delayed' argument. Refer to <@link language ~waitable>.
    //
    // To delete a schematic file, use <@link mechanism server.delete_file>.
    //
    // @Tags
    // <schematic[<name>].height>
    // <schematic[<name>].length>
    // <schematic[<name>].width>
    // <schematic[<name>].block[<location>]>
    // <schematic[<name>].origin>
    // <schematic[<name>].blocks>
    // <schematic[<name>].exists>
    // <schematic[<name>].cuboid[<origin_location>]>
    // <schematic.list>
    //
    // @Usage
    // Use to create a new schematic from a cuboid and an origin location.
    // - schematic create name:MySchematic area:<[my_cuboid]> <player.location>
    //
    // @Usage
    // Use to load a schematic.
    // - ~schematic load name:MySchematic
    //
    // @Usage
    // Use to unload a schematic.
    // - schematic unload name:MySchematic
    //
    // @Usage
    // Use to paste a loaded schematic with no air blocks.
    // - schematic paste name:MySchematic <player.location> noair
    //
    // @Usage
    // Use to save a created schematic.
    // - ~schematic save name:MySchematic
    // -->

    public static boolean noPhys = false;

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (noPhys) {
            event.setCancelled(true);
        }
    }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("name:", schematics.keySet());
    }

    private enum Type {CREATE, LOAD, UNLOAD, ROTATE, PASTE, SAVE, FLIP_X, FLIP_Y, FLIP_Z}

    public static Map<String, CuboidBlockSet> schematics;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(Type.class)) {
                scriptEntry.addObject("type", new ElementTag(arg.getRawValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class).getBlockLocation());
            }
            else if (!scriptEntry.hasObject("area")
                    && arg.matchesArgumentType(CuboidTag.class)) { // Historical input format
                scriptEntry.addObject("area", arg.asType(CuboidTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("type")) {
            throw new InvalidArgumentsException("Missing type argument!");
        }
    }

    public static void rotateSchem(CuboidBlockSet schematic, int angle, boolean delayed, Runnable callback) {
        Runnable rotateRunnable = () -> {
            try {
                int ang = angle;
                while (ang < 0) {
                    ang = 360 + ang;
                }
                while (ang >= 360) {
                    ang -= 360;
                }
                if (ang != 0) {
                    ang = 360 - ang;
                    while (ang > 0) {
                        ang -= 90;
                        schematic.rotateOne();
                    }
                }
            }
            finally {
                if (delayed) {
                    Bukkit.getScheduler().runTask(Denizen.instance, () -> schematic.isModifying = false);
                }
                if (callback != null) {
                    if (delayed) {
                        Bukkit.getScheduler().runTask(Denizen.instance, callback);
                    }
                    else {
                        callback.run();
                    }
                }
            }
        };
        if (delayed) {
            schematic.isModifying = true;
            Bukkit.getScheduler().runTaskAsynchronously(Denizen.instance, rotateRunnable);
        }
        else {
            rotateRunnable.run();
        }
    }

    public static void parseMask(ScriptEntry scriptEntry, String maskText, HashSet<Material> mask) {
        if (maskText.startsWith("li@")) { // Back-compat: input used to be a list of materials
            for (MaterialTag material : ListTag.valueOf(maskText, scriptEntry.getContext()).filter(MaterialTag.class, scriptEntry)) {
                mask.add(material.getMaterial());
            }
        }
        else {
            for (Material material : Material.values()) {
                if (MaterialTag.advancedMatchesInternal(material, maskText, true)) {
                    mask.add(material);
                }
            }
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        ElementTag angle = scriptEntry.argForPrefixAsElement("angle", null);
        ElementTag type = scriptEntry.getElement("type");
        ElementTag name = scriptEntry.requiredArgForPrefixAsElement("name");
        ElementTag filename = scriptEntry.argForPrefixAsElement("filename", null);
        boolean noair = scriptEntry.argAsBoolean("noair");
        boolean delayed = scriptEntry.argAsBoolean("delayed") || scriptEntry.shouldWaitFor();
        ElementTag maxDelayMs = scriptEntry.argForPrefixAsElement("max_delay_ms", "50");
        boolean copyEntities = scriptEntry.argAsBoolean("entities");
        boolean flags = scriptEntry.argAsBoolean("flags");
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag mask = scriptEntry.argForPrefixAsElement("mask", null);
        List<PlayerTag> fakeTo = scriptEntry.argForPrefixList("fake_to", PlayerTag.class, true);
        DurationTag fakeDuration = scriptEntry.argForPrefix("fake_duration", DurationTag.class, true);
        CuboidTag legacyCuboid = scriptEntry.getObjectTag("area");
        AreaContainmentObject areaVal = null;
        if (legacyCuboid != null) {
            areaVal = legacyCuboid;
        }
        else {
            Argument areaArg = scriptEntry.argForPrefix("area");
            if (areaArg != null) {
                if (areaArg.object instanceof AreaContainmentObject) {
                    areaVal = (AreaContainmentObject) areaArg.object;
                }
                else {
                    ObjectTag reparsedArea = ObjectFetcher.pickObjectFor(areaArg.getValue(), scriptEntry.context);
                    if (reparsedArea instanceof AreaContainmentObject) {
                        areaVal = (AreaContainmentObject)  reparsedArea;
                    }
                    else {
                        throw new InvalidArgumentsRuntimeException("Area input '" + areaArg.getValue() + "' is not a valid Area object");
                    }
                }
            }
        }
        final AreaContainmentObject area = areaVal;
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), type, name, location, filename, area, angle, db("noair", noair), db("delayed", delayed),
                    maxDelayMs, db("flags", flags), db("entities", copyEntities), mask, fakeDuration, db("fake_to", fakeTo));
        }
        CuboidBlockSet set;
        Type ttype = Type.valueOf(type.asString());
        String fname = filename != null ? filename.asString() : name.asString();
        switch (ttype) {
            case CREATE: {
                if (schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is already loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                if (area == null) {
                    Debug.echoError(scriptEntry, "Missing area argument!");
                    scriptEntry.setFinished(true);
                    return;
                }
                if (location == null) {
                    Debug.echoError(scriptEntry, "Missing origin location argument!");
                    scriptEntry.setFinished(true);
                    return;
                }
                try {
                    HashSet<Material> maskSet = null;
                    if (mask != null) {
                        String maskText = mask.asString();
                        maskSet = new HashSet<>();
                        parseMask(scriptEntry, maskText, maskSet);
                    }
                    set = new CuboidBlockSet();
                    if (delayed) {
                        set.buildDelayed(area, location, maskSet, () -> {
                            if (copyEntities) {
                                set.buildEntities(area, location);
                            }
                            schematics.put(name.asString().toUpperCase(), set);
                            scriptEntry.setFinished(true);
                        }, maxDelayMs.asLong(), flags);
                    }
                    else {
                        scriptEntry.setFinished(true);
                        set.buildImmediate(area, location, maskSet, flags);
                        if (copyEntities) {
                            set.buildEntities(area, location);
                        }
                        schematics.put(name.asString().toUpperCase(), set);
                    }
                }
                catch (Exception ex) {
                    Debug.echoError(scriptEntry, "Error creating schematic object " + name.asString() + ".");
                    Debug.echoError(scriptEntry, ex);
                    scriptEntry.setFinished(true);
                    return;
                }
                break;
            }
            case LOAD: {
                if (schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is already loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                File f = new File(Denizen.getInstance().getDataFolder(), "schematics/" + fname + ".schem");
                if (!Utilities.canReadFile(f)) {
                    Debug.echoError("Cannot read from that file path due to security settings in Denizen/config.yml.");
                    scriptEntry.setFinished(true);
                    return;
                }
                if (!f.exists()) {
                    f = new File(Denizen.getInstance().getDataFolder(), "schematics/" + fname + ".schematic");
                    if (!f.exists()) {
                        Debug.echoError("Schematic file " + fname + " does not exist. Are you sure it's in plugins/Denizen/schematics/?");
                        scriptEntry.setFinished(true);
                        return;
                    }
                }
                File schemFile = f;
                Runnable loadRunnable = () -> {
                    try {
                        InputStream fs = new FileInputStream(schemFile);
                        CuboidBlockSet newSet;
                        newSet = SpongeSchematicHelper.fromSpongeStream(fs);
                        fs.close();
                        Runnable storeSchem = () -> {
                            schematics.put(name.asString().toUpperCase(), newSet);
                            scriptEntry.setFinished(true);
                        };
                        if (delayed) {
                            Bukkit.getScheduler().runTask(Denizen.instance, storeSchem);
                        }
                        else {
                            storeSchem.run();
                        }
                    }
                    catch (Exception ex) {
                        Runnable showError = () -> {
                            Debug.echoError(scriptEntry, "Error loading schematic file " + name.asString() + ".");
                            Debug.echoError(scriptEntry, ex);
                        };
                        if (delayed) {
                            Bukkit.getScheduler().runTask(Denizen.instance, showError);
                        }
                        else {
                            showError.run();
                        }
                        scriptEntry.setFinished(true);
                        return;
                    }
                };
                if (delayed) {
                    Bukkit.getScheduler().runTaskAsynchronously(Denizen.instance, loadRunnable);
                }
                else {
                    loadRunnable.run();
                    scriptEntry.setFinished(true);
                }
                break;
            }
            case UNLOAD: {
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is not loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                schematics.remove(name.asString().toUpperCase());
                scriptEntry.setFinished(true);
                break;
            }
            case ROTATE: {
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is not loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                if (angle == null) {
                    Debug.echoError(scriptEntry, "Missing angle argument!");
                    scriptEntry.setFinished(true);
                    return;
                }
                final CuboidBlockSet schematic = schematics.get(name.asString().toUpperCase());
                if (schematic.isModifying || schematic.readingProcesses > 0) {
                    Debug.echoError("Cannot rotate schematic: schematic is currently processing another instruction.");
                    return;
                }
                rotateSchem(schematic, angle.asInt(), delayed, () -> scriptEntry.setFinished(true));
                break;
            }
            case FLIP_X: {
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is not loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                final CuboidBlockSet schematic = schematics.get(name.asString().toUpperCase());
                if (schematic.isModifying || schematic.readingProcesses > 0) {
                    Debug.echoError("Cannot flip schematic: schematic is currently processing another instruction.");
                    return;
                }
                schematic.flipX();
                scriptEntry.setFinished(true);
                break;
            }
            case FLIP_Y: {
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is not loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                final CuboidBlockSet schematic = schematics.get(name.asString().toUpperCase());
                if (schematic.isModifying || schematic.readingProcesses > 0) {
                    Debug.echoError("Cannot flip schematic: schematic is currently processing another instruction.");
                    return;
                }
                schematic.flipY();
                scriptEntry.setFinished(true);
                break;
            }
            case FLIP_Z: {
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is not loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                final CuboidBlockSet schematic = schematics.get(name.asString().toUpperCase());
                if (schematic.isModifying || schematic.readingProcesses > 0) {
                    Debug.echoError("Cannot flip schematic: schematic is currently processing another instruction.");
                    return;
                }
                schematic.flipZ();
                scriptEntry.setFinished(true);
                break;
            }
            case PASTE: {
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is not loaded.");
                    scriptEntry.setFinished(true);
                    return;
                }
                if (location == null) {
                    Debug.echoError(scriptEntry, "Missing location argument!");
                    scriptEntry.setFinished(true);
                    return;
                }
                try {
                    BlockSet.InputParams input = new BlockSet.InputParams();
                    input.centerLocation = location;
                    input.noAir = noair;
                    input.fakeTo = fakeTo;
                    if (fakeTo != null && copyEntities) {
                        Debug.echoError(scriptEntry, "Cannot fake paste entities currently.");
                        scriptEntry.setFinished(true);
                        return;
                    }
                    if (fakeDuration == null) {
                        fakeDuration = new DurationTag(0);
                    }
                    input.fakeDuration = fakeDuration;
                    if (mask != null) {
                        String maskText = mask.asString();
                        input.mask = new HashSet<>();
                        parseMask(scriptEntry, maskText, input.mask);
                    }
                    set = schematics.get(name.asString().toUpperCase());
                    if (set.isModifying) {
                        Debug.echoError("Cannot paste schematic: schematic is currently processing another instruction.");
                        return;
                    }
                    Consumer<CuboidBlockSet> pasteRunnable = (schematic) -> {
                        if (delayed) {
                            schematic.readingProcesses++;
                            schematic.setBlocksDelayed(() -> {
                                try {
                                    if (copyEntities) {
                                        schematic.pasteEntities(location);
                                    }
                                }
                                finally {
                                    scriptEntry.setFinished(true);
                                    schematic.readingProcesses--;
                                }
                            }, input, maxDelayMs.asLong());
                        }
                        else {
                            schematic.setBlocks(input);
                            if (copyEntities) {
                                schematic.pasteEntities(location);
                            }
                            scriptEntry.setFinished(true);
                        }
                    };
                    if (angle != null) {
                        final CuboidBlockSet newSet = set.duplicate();
                        rotateSchem(newSet, angle.asInt(), delayed, () -> pasteRunnable.accept(newSet));
                    }
                    else {
                        pasteRunnable.accept(set);
                    }
                }
                catch (Exception ex) {
                    Debug.echoError(scriptEntry, "Exception pasting schematic file " + name.asString() + ".");
                    Debug.echoError(scriptEntry, ex);
                    scriptEntry.setFinished(true);
                    return;
                }
                break;
            }
            case SAVE: {
                if (!schematics.containsKey(name.asString().toUpperCase())) {
                    Debug.echoError(scriptEntry, "Schematic file " + name.asString() + " is not loaded.");
                    return;
                }
                set = schematics.get(name.asString().toUpperCase());
                if (set.isModifying) {
                    Debug.echoError("Cannot save schematic: schematic is currently processing another instruction.");
                    return;
                }
                String directory = URLDecoder.decode(System.getProperty("user.dir"));
                String extension = ".schem";
                File f = new File(directory + "/plugins/Denizen/schematics/" + fname + extension);
                if (!Utilities.canWriteToFile(f)) {
                    Debug.echoError("Cannot write to that file path due to security settings in Denizen/config.yml.");
                    scriptEntry.setFinished(true);
                    return;
                }
                Runnable saveRunnable = () -> {
                    try {
                        f.getParentFile().mkdirs();
                        FileOutputStream fs = new FileOutputStream(f);
                        SpongeSchematicHelper.saveToSpongeStream(set, fs);
                        fs.flush();
                        fs.close();
                    }
                    catch (Exception ex) {
                        Bukkit.getScheduler().runTask(Denizen.instance, () -> {
                            Debug.echoError(scriptEntry, "Error saving schematic file " + fname + ".");
                            Debug.echoError(scriptEntry, ex);
                        });
                    }
                    Bukkit.getScheduler().runTask(Denizen.instance, () -> {
                        set.readingProcesses--;
                        scriptEntry.setFinished(true);
                    });
                };
                if (delayed) {
                    set.readingProcesses++;
                    Bukkit.getScheduler().runTaskAsynchronously(Denizen.instance, saveRunnable);
                }
                else {
                    scriptEntry.setFinished(true);
                    saveRunnable.run();
                }
                break;
            }
        }
    }

    public void schematicTags(ReplaceableTagEvent event) {
        if (!event.matches("schematic")) {
            return;
        }
        Attribute attribute = event.getAttributes();
        String id = attribute.hasParam() ? attribute.getParam().toUpperCase() : null;
        attribute = attribute.fulfill(1);

        // <--[tag]
        // @attribute <schematic.list>
        // @returns ListTag
        // @description
        // Returns a list of all loaded schematics.
        // -->
        if (attribute.startsWith("list")) {
            event.setReplacedObject(new ListTag(schematics.keySet()).getObjectAttribute(attribute.fulfill(1)));
        }
        if (id == null) {
            return;
        }
        if (!schematics.containsKey(id)) {
            // Meta below
            if (attribute.startsWith("exists")) {
                event.setReplacedObject(new ElementTag(false)
                        .getObjectAttribute(attribute.fulfill(1)));
                return;
            }
            Debug.echoError(attribute.getScriptEntry(), "Schematic file " + id + " is not loaded.");
            return;
        }
        CuboidBlockSet set = schematics.get(id);

        // <--[tag]
        // @attribute <schematic[<name>].exists>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the schematic exists.
        // -->
        if (attribute.startsWith("exists")) {
            event.setReplacedObject(new ElementTag(true)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].height>
        // @returns ElementTag(Number)
        // @description
        // Returns the height (Y) of the schematic.
        // -->
        if (attribute.startsWith("height")) {
            event.setReplacedObject(new ElementTag(set.y_length)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].length>
        // @returns ElementTag(Number)
        // @description
        // Returns the length (Z) of the schematic.
        // -->
        if (attribute.startsWith("length")) {
            event.setReplacedObject(new ElementTag(set.z_height)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].width>
        // @returns ElementTag(Number)
        // @description
        // Returns the width (X) of the schematic.
        // -->
        if (attribute.startsWith("width")) {
            event.setReplacedObject(new ElementTag(set.x_width)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].block[<location>]>
        // @returns MaterialTag
        // @description
        // Returns the material for the block at the location in the schematic.
        // An input location of 0,0,0 corresponds to the minimum corner of the schematic.
        // -->
        if (attribute.startsWith("block")) {
            if (attribute.hasParam() && LocationTag.matches(attribute.getParam())) {
                LocationTag location = attribute.paramAsType(LocationTag.class);
                FullBlockData block = set.blockAt(location.getX(), location.getY(), location.getZ());
                event.setReplacedObject(new MaterialTag(block.data)
                        .getObjectAttribute(attribute.fulfill(1)));
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
            event.setReplacedObject(new LocationTag(null, set.center_x, set.center_y, set.center_z)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].blocks>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of blocks in the schematic.
        // -->
        if (attribute.startsWith("blocks")) {
            event.setReplacedObject(new ElementTag(set.blocks.length)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <schematic[<name>].cuboid[<origin_location>]>
        // @returns CuboidTag
        // @description
        // Returns a cuboid of where the schematic would be if it was pasted at an origin.
        // -->
        if (attribute.startsWith("cuboid") && attribute.hasParam()) {
            LocationTag origin = attribute.paramAsType(LocationTag.class);
            event.setReplacedObject(set.getCuboid(origin)
                    .getObjectAttribute(attribute.fulfill(1)));
            return;
        }
    }
}

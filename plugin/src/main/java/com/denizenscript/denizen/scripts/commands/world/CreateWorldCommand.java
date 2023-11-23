package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Supplier;

public class CreateWorldCommand extends AbstractCommand implements Holdable {

    public CreateWorldCommand() {
        setName("createworld");
        setSyntax("createworld [<name>] (generator:<id>) (worldtype:<type>) (environment:<environment>) (copy_from:<world>) (seed:<seed>) (settings:<json>) (generate_structures:true/false)");
        setRequiredArguments(1, 8);
        setPrefixesHandled("generator", "worldtype", "environment", "copy_from", "seed", "settings", "generate_structures");
        isProcedural = false;
    }

    // <--[command]
    // @Name CreateWorld
    // @Syntax createworld [<name>] (generator:<id>) (worldtype:<type>) (environment:<environment>) (copy_from:<world>) (seed:<seed>) (settings:<json>) (generate_structures:true/false)
    // @Required 1
    // @Maximum 8
    // @Short Creates a new world, or loads an existing world.
    // @Synonyms LoadWorld
    // @Group world
    //
    // @Description
    // This command creates a new minecraft world with the specified name, or loads an existing world by that name.
    //
    // Optionally specify a plugin-based world generator by its generator ID.
    // If you want an empty void world with a void biome, you can use "denizen:void".
    // If you want an empty void world with vanilla biomes, you can use "denizen:void_biomes".
    //
    // Optionally specify additional generator settings as JSON input.
    //
    // Optionally specify a world type which can be specified with 'worldtype:' (defaults to NORMAL).
    // For all world types, see: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>
    //
    // Optionally specify an environment (defaults to NORMAL, can also be NETHER, THE_END).
    //
    // Optionally choose whether to generate structures in this world.
    //
    // Optionally specify an existing world to copy files from.
    // The 'copy_from' argument is ~waitable. Refer to <@link language ~waitable>.
    //
    // It's often ideal to put this command inside <@link event server prestart>.
    //
    // @Tags
    // <server.world_types>
    // <server.worlds>
    //
    // @Usage
    // Use to create a normal world with name 'survival'
    // - createworld survival
    //
    // @Usage
    // Use to create a flat world with the name 'superflat'
    // - createworld superflat worldtype:FLAT
    //
    // @Usage
    // Use to create an end world with the name 'space'
    // - createworld space environment:THE_END
    //
    // @Usage
    // Use to create a new world named 'dungeon3' as a copy of an existing world named 'dungeon_template'.
    // - ~createworld dungeon3 copy_from:dungeon_template
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("environment:", World.Environment.values());
        tab.addWithPrefix("worldtype:", WorldType.values());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("world_name")) {
                scriptEntry.addObject("world_name", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("world_name")) {
            throw new InvalidArgumentsException("Must specify a world name.");
        }
    }

    public static HashSet<String> excludedExtensionsForCopyFrom = new HashSet<>(Collections.singleton("lock"));

    public static AsciiMatcher forbiddenSymbols = new AsciiMatcher("");

    static {
        for (int i = 0; i < 256; i++) {
            forbiddenSymbols.accepted[i] = !((i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z') || (i >= '0' && i <= '9') || (i == '_') || (i == '-') || (i == ' '));
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag worldName = scriptEntry.getElement("world_name");
        ElementTag generator = scriptEntry.argForPrefixAsElement("generator", null);
        ElementTag worldType = scriptEntry.argForPrefixAsElement("worldtype", "NORMAL");
        ElementTag environment = scriptEntry.argForPrefixAsElement("environment", "NORMAL");
        ElementTag copy_from = scriptEntry.argForPrefixAsElement("copy_from", null);
        ElementTag settings = scriptEntry.argForPrefixAsElement("settings", null);
        ElementTag seed = scriptEntry.argForPrefixAsElement("seed", null);
        ElementTag generateStructures = scriptEntry.argForPrefixAsElement("generate_structures", null);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), worldName, generator, environment, copy_from, settings, worldType, seed, generateStructures);
        }
        if (Bukkit.getWorld(worldName.asString()) != null) {
            Debug.echoDebug(scriptEntry, "CreateWorld doing nothing, world by that name already loaded.");
            scriptEntry.setFinished(true);
            return;
        }
        if (!Settings.cache_createWorldSymbols) {
            if (forbiddenSymbols.containsAnyMatch(worldName.asString())) {
                Debug.echoError("Cannot use world names with non-alphanumeric symbols due to security settings in Denizen/config.yml.");
                scriptEntry.setFinished(true);
                return;
            }
        }
        else if (!Settings.cache_createWorldWeirdPaths) {
            String cleaned = worldName.asLowerString().replace('\\', '/');
            while (cleaned.contains("//")) {
                cleaned = cleaned.replace("//", "/");
            }
            if (cleaned.startsWith("/")) {
                cleaned = cleaned.substring(1);
            }
            if (cleaned.startsWith("plugins/")) {
                Debug.echoError("CreateWorld cannot create a world inside the plugins folder due to security settings in Denizen/config.yml.");
                scriptEntry.setFinished(true);
                return;
            }
            if (cleaned.startsWith("..")) {
                Debug.echoError("CreateWorld cannot create a world with a raised path (contains '..') due to security settings in Denizen/config.yml.");
                scriptEntry.setFinished(true);
                return;
            }
        }
        final File newFolder = new File(Bukkit.getWorldContainer(), worldName.asString());
        if (!Utilities.canWriteToFile(newFolder)) {
            Debug.echoError("Cannot copy to that new folder path due to security settings in Denizen/config.yml.");
            scriptEntry.setFinished(true);
            return;
        }
        WorldType enumWorldType;
        World.Environment enumEnvironment;
        try {
            enumWorldType = WorldType.valueOf(worldType.asString().toUpperCase());
            enumEnvironment = World.Environment.valueOf(environment.asString().toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            Debug.echoError("Invalid worldtype or environment: " + ex.getMessage());
            scriptEntry.setFinished(true);
            return;
        }
        if (copy_from != null && !Settings.cache_createWorldSymbols && forbiddenSymbols.containsAnyMatch(copy_from.asString())) {
            Debug.echoError("Cannot use copy_from world names with non-alphanumeric symbols due to security settings in Denizen/config.yml.");
            scriptEntry.setFinished(true);
            return;
        }
        Supplier<Boolean> copyRunnable = () -> {
            try {
                File folder = new File(Bukkit.getWorldContainer(), copy_from.asString().replace("w@", ""));
                if (!Utilities.canReadFile(folder)) {
                    Debug.echoError(scriptEntry, "Cannot copy from that folder path due to security settings in Denizen/config.yml.");
                    return false;
                }
                if (!folder.exists() || !folder.isDirectory()) {
                    Debug.echoError(scriptEntry, "Invalid copy from world folder - does not exist!");
                    return false;
                }
                if (newFolder.exists()) {
                    Debug.echoError(scriptEntry, "Cannot copy to new world - that folder already exists.");
                    return false;
                }
                CoreUtilities.copyDirectory(folder, newFolder, excludedExtensionsForCopyFrom);
                Debug.echoDebug(scriptEntry, "Copied " + folder.getName() + " to " + newFolder.getName());
                File file = new File(Bukkit.getWorldContainer(), worldName.asString() + "/uid.dat");
                if (file.exists()) {
                    file.delete();
                }
                File file2 = new File(Bukkit.getWorldContainer(), worldName.asString() + "/session.lock");
                if (file2.exists()) {
                    file2.delete();
                }
            }
            catch (Throwable ex) {
                Debug.echoError(scriptEntry, ex);
                return false;
            }
            return true;
        };
        Runnable createRunnable = () -> {
            World world;
            WorldCreator worldCreator = WorldCreator.name(worldName.asString())
                    .environment(enumEnvironment)
                    .type(enumWorldType);
            if (generateStructures != null) {
                worldCreator.generateStructures(generateStructures.asBoolean());
            }
            if (generator != null) {
                worldCreator.generator(generator.asString());
            }
            if (seed != null) {
                worldCreator.seed(seed.asLong());
            }
            if (settings != null) {
                worldCreator.generatorSettings(settings.asString());
            }
            world = Bukkit.getServer().createWorld(worldCreator);
            if (world == null) {
                Debug.echoError("World is null, something went wrong in creation!");
            }
            else {
                Debug.echoDebug(scriptEntry, "Created new world " + world.getName());
            }
            scriptEntry.setFinished(true);
        };
        if (scriptEntry.shouldWaitFor() && copy_from != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Denizen.getInstance(), () -> {
                if (!copyRunnable.get()) {
                    scriptEntry.setFinished(true);
                    return;
                }
                Bukkit.getScheduler().runTask(Denizen.getInstance(), createRunnable);
            });
        }
        else {
            if (copy_from != null && !copyRunnable.get()) {
                return;
            }
            createRunnable.run();
        }
    }
}

package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
        setSyntax("createworld [<name>] (generator:<id>) (worldtype:<type>) (environment:<environment>) (copy_from:<world>) (seed:<seed>) (settings:<json>)");
        setRequiredArguments(1, 7);
        isProcedural = false;
    }

    // <--[command]
    // @Name CreateWorld
    // @Syntax createworld [<name>] (generator:<id>) (worldtype:<type>) (environment:<environment>) (copy_from:<world>) (seed:<seed>) (settings:<json>)
    // @Required 1
    // @Maximum 7
    // @Short Creates a new world, or loads an existing world.
    // @Synonyms LoadWorld
    // @Group world
    //
    // @Description
    // This command creates a new minecraft world with the specified name, or loads an existing world by that name.
    //
    // Optionally specify a plugin-based world generator by it's generator ID.
    // If you want an empty void world, you can use "generator:denizen:void".
    //
    // Optionally specify additional generator settings as JSON input.
    //
    // Optionally specify a world type which can be specified with 'worldtype:' (defaults to NORMAL).
    // For all world types, see: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>
    //
    // Optionally specify an environment (defaults to NORMAL, can also be NETHER, THE_END, or CUSTOM).
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
            if (!scriptEntry.hasObject("generator")
                    && arg.matchesPrefix("generator", "g")) {
                scriptEntry.addObject("generator", arg.asElement());
            }
            else if (!scriptEntry.hasObject("worldtype")
                    && arg.matchesPrefix("worldtype")
                    && arg.matchesEnum(WorldType.values())) {
                scriptEntry.addObject("worldtype", arg.asElement());
            }
            else if (!scriptEntry.hasObject("environment")
                    && arg.matchesPrefix("environment")
                    && arg.matchesEnum(World.Environment.values())) {
                scriptEntry.addObject("environment", arg.asElement());
            }
            else if (!scriptEntry.hasObject("copy_from")
                    && arg.matchesPrefix("copy_from")) {
                scriptEntry.addObject("copy_from", arg.asElement());
            }
            else if (!scriptEntry.hasObject("settings")
                    && arg.matchesPrefix("settings")) {
                scriptEntry.addObject("settings", arg.asElement());
            }
            else if (!scriptEntry.hasObject("seed")
                    && arg.matchesPrefix("seed", "s")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("seed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("world_name")) {
                scriptEntry.addObject("world_name", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("world_name")) {
            throw new InvalidArgumentsException("Must specify a world name.");
        }
        if (!scriptEntry.hasObject("worldtype")) {
            scriptEntry.addObject("worldtype", new ElementTag("NORMAL"));
        }
        scriptEntry.defaultObject("environment", new ElementTag("NORMAL"));
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
        ElementTag generator = scriptEntry.getElement("generator");
        ElementTag worldType = scriptEntry.getElement("worldtype");
        ElementTag environment = scriptEntry.getElement("environment");
        ElementTag copy_from = scriptEntry.getElement("copy_from");
        ElementTag settings = scriptEntry.getElement("settings");
        ElementTag seed = scriptEntry.getElement("seed");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), worldName, generator, environment, copy_from, settings, worldType, seed);
        }
        if (Bukkit.getWorld(worldName.asString()) != null) {
            Debug.echoDebug(scriptEntry, "CreateWorld doing nothing, world by that name already loaded.");
            scriptEntry.setFinished(true);
            return;
        }
        if (!Settings.cache_createWorldSymbols && forbiddenSymbols.containsAnyMatch(worldName.asString())) {
            Debug.echoError("Cannot use world names with non-alphanumeric symbols due to security settings in Denizen/config.yml.");
            scriptEntry.setFinished(true);
            return;
        }
        final File newFolder = new File(Bukkit.getWorldContainer(), worldName.asString());
        if (!Utilities.canWriteToFile(newFolder)) {
            Debug.echoError("Cannot copy to that new folder path due to security settings in Denizen/config.yml.");
            scriptEntry.setFinished(true);
            return;
        }
        Supplier<Boolean> copyRunnable = () -> {
            try {
                if (!Settings.cache_createWorldSymbols && forbiddenSymbols.containsAnyMatch(copy_from.asString())) {
                    Debug.echoError("Cannot use copy_from world names with non-alphanumeric symbols due to security settings in Denizen/config.yml.");
                    return false;
                }
                File folder = new File(Bukkit.getWorldContainer(), copy_from.asString().replace("w@", ""));
                if (!Utilities.canReadFile(folder)) {
                    Debug.echoError("Cannot copy from that folder path due to security settings in Denizen/config.yml.");
                    return false;
                }
                if (!folder.exists() || !folder.isDirectory()) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid copy from world folder - does not exist!");
                    return false;
                }
                if (newFolder.exists()) {
                    Debug.echoError("Cannot copy to new world - that folder already exists.");
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
                Debug.echoError(ex);
                return false;
            }
            return true;
        };
        Runnable createRunnable = () -> {
            World world;
            WorldCreator worldCreator = WorldCreator.name(worldName.asString())
                    .environment(World.Environment.valueOf(environment.asString().toUpperCase()))
                    .type(WorldType.valueOf(worldType.asString().toUpperCase()));
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
                NMSHandler.getWorldHelper().setDimension(world, World.Environment.valueOf(environment.asString().toUpperCase()));
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

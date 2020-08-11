package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;
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
    // @Group world
    //
    // @Description
    // This command creates a new minecraft world with the specified name, or loads an existing world by that name.
    // Optionally specify a plugin-based world generator by it's generator ID.
    // Optionally specify a world type which can be specified with 'worldtype:' (defaults to NORMAL).
    // For all world types, see: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/WorldType.html>
    // Optionally specify an environment (defaults to NORMAL, can also be NETHER or THE_END).
    // Optionally specify an existing world to copy files from.
    // Optionally specify additional generator settings as JSON input.
    //
    // The 'copy_from' argument is ~waitable. Refer to <@link language ~waitable>.
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
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
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
            Debug.report(scriptEntry, getName(), worldName.debug() +
                    (generator != null ? generator.debug() : "") +
                    environment.debug() +
                    (copy_from != null ? copy_from.debug() : "") +
                    (settings != null ? settings.debug() : "") +
                    worldType.debug() +
                    (seed != null ? seed.debug() : ""));
        }
        if (Bukkit.getWorld(worldName.asString()) != null) {
            Debug.echoDebug(scriptEntry, "CreateWorld doing nothing, world by that name already loaded.");
            scriptEntry.setFinished(true);
            return;
        }
        Supplier<Boolean> copyRunnable = () -> {
            try {
                if (copy_from.asString().contains("..")) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid copy from world name!");
                    return false;
                }
                File newFolder = new File(worldName.asString());
                File folder = new File(copy_from.asString().replace("w@", ""));
                if (!Utilities.canReadFile(folder)) {
                    Debug.echoError("Cannot copy from that folder path.");
                    return false;
                }
                if (!Utilities.canWriteToFile(newFolder)) {
                    Debug.echoError("Cannot copy to that new folder path.");
                    return false;
                }
                if (!folder.exists() || !folder.isDirectory()) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid copy from world folder - does not exist!");
                    return false;
                }
                CoreUtilities.copyDirectory(folder, newFolder);
                File file = new File(worldName.asString() + "/uid.dat");
                if (file.exists()) {
                    file.delete();
                }
                File file2 = new File(worldName.asString() + "/session.lock");
                if (file2.exists()) {
                    file2.delete();
                }
            }
            catch (Exception ex) {
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
                Debug.echoDebug(scriptEntry, "World is null, something went wrong in creation!");
            }
            scriptEntry.setFinished(true);
        };
        if (scriptEntry.shouldWaitFor() && copy_from != null) {
            Bukkit.getScheduler().runTaskAsynchronously(DenizenAPI.getCurrentInstance(), () -> {
                if (!copyRunnable.get()) {
                    scriptEntry.setFinished(true);
                    return;
                }
                Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), createRunnable);
            });
        }
        else {
            createRunnable.run();
        }
    }
}

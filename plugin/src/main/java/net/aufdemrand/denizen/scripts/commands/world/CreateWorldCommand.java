package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;

public class CreateWorldCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

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

            else if (!scriptEntry.hasObject("world_name")) {
                scriptEntry.addObject("world_name", arg.asElement());
            }

            else {
                arg.reportUnhandled();
            }
        }

        // Check for required information
        if (!scriptEntry.hasObject("world_name")) {
            throw new InvalidArgumentsException("Must specify a world name.");
        }

        if (!scriptEntry.hasObject("worldtype")) {
            scriptEntry.addObject("worldtype", new Element("NORMAL"));
        }

        scriptEntry.defaultObject("environment", new Element("NORMAL"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element World_Name = scriptEntry.getElement("world_name");
        Element Generator = scriptEntry.getElement("generator");
        Element worldType = scriptEntry.getElement("worldtype");
        Element environment = scriptEntry.getElement("environment");
        Element copy_from = scriptEntry.getElement("copy_from");

        dB.report(scriptEntry, getName(), World_Name.debug() +
                (Generator != null ? Generator.debug() : "") +
                environment.debug() +
                (copy_from != null ? copy_from.debug(): "") +
                worldType.debug());

        if (copy_from != null) {
            try {
                if (copy_from.asString().contains("..")) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Invalid copy from world name!");
                    return;
                }
                File newFolder = new File(World_Name.asString());
                File folder = new File(copy_from.asString().replace("w@", ""));
                if (!folder.exists() || !folder.isDirectory()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Invalid copy from world folder - does not exist!");
                    return;
                }
                FileUtils.copyDirectory(folder, newFolder);
                File file = new File(World_Name.asString() + "/uid.dat");
                if (file.exists()) {
                    file.delete();
                }
                File file2 = new File(World_Name.asString() + "/session.lock");
                if (file2.exists()) {
                    file2.delete();
                }
            }
            catch (Exception ex) {
                dB.echoError(ex);
                return;
            }
        }

        World world;

        if (Generator != null) {
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(World_Name.asString())
                    .generator(Generator.asString())
                    .environment(World.Environment.valueOf(environment.asString().toUpperCase()))
                    .type(WorldType.valueOf(worldType.asString().toUpperCase())));
        }

        else {
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(World_Name.asString())
                    .environment(World.Environment.valueOf(environment.asString().toUpperCase()))
                    .type(WorldType.valueOf(worldType.asString().toUpperCase())));
        }

        if (world == null) {
            dB.echoDebug(scriptEntry, "World is null, something went wrong in creation!");
        }

    }
}

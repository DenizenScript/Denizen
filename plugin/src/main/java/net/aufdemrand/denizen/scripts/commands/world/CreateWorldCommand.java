package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;

public class CreateWorldCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
            else if (!scriptEntry.hasObject("seed")
                    && arg.matchesPrefix("seed", "s")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("seed", arg.asElement());
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
    public void execute(ScriptEntry scriptEntry) {

        Element worldName = scriptEntry.getElement("world_name");
        Element generator = scriptEntry.getElement("generator");
        Element worldType = scriptEntry.getElement("worldtype");
        Element environment = scriptEntry.getElement("environment");
        Element copy_from = scriptEntry.getElement("copy_from");
        Element seed = scriptEntry.getElement("seed");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), worldName.debug() +
                    (generator != null ? generator.debug() : "") +
                    environment.debug() +
                    (copy_from != null ? copy_from.debug() : "") +
                    worldType.debug() +
                    (seed != null ? seed.debug() : ""));

        }

        if (copy_from != null) {
            try {
                if (copy_from.asString().contains("..")) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Invalid copy from world name!");
                    return;
                }
                File newFolder = new File(worldName.asString());
                File folder = new File(copy_from.asString().replace("w@", ""));
                if (!folder.exists() || !folder.isDirectory()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Invalid copy from world folder - does not exist!");
                    return;
                }
                Utilities.copyDirectory(folder, newFolder);
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
                dB.echoError(ex);
                return;
            }
        }

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

        world = Bukkit.getServer().createWorld(worldCreator);

        if (world == null) {
            dB.echoDebug(scriptEntry, "World is null, something went wrong in creation!");
        }

    }
}

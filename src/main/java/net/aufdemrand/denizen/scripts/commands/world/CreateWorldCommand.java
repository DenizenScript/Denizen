package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class CreateWorldCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("generator")
                    && arg.matchesPrefix("generator", "g"))
                scriptEntry.addObject("generator", arg.asElement());

            else if (!scriptEntry.hasObject("worldtype")
                    && arg.matchesPrefix("worldtype")
                    && arg.matchesEnum(WorldType.values()))
                scriptEntry.addObject("worldtype", arg.asElement());

            else if (!scriptEntry.hasObject("world_name"))
                scriptEntry.addObject("world_name", arg.asElement());

            else arg.reportUnhandled();
        }

        // Check for required information
        if (!scriptEntry.hasObject("world_name"))
            throw new InvalidArgumentsException("Must specify a world name.");

        if (!scriptEntry.hasObject("worldtype"))
            scriptEntry.addObject("worldtype", new Element("NORMAL"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element World_Name = scriptEntry.getElement("world_name");
        Element Generator = scriptEntry.getElement("generator");
        Element worldType = scriptEntry.getElement("worldtype");

        dB.report(scriptEntry, getName(), World_Name.debug() +
                (Generator != null ? Generator.debug() : "") +
                worldType.debug());

        World world;

        if (Generator != null)
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(World_Name.asString())
                    .generator(Generator.asString())
                    .type(WorldType.valueOf(worldType.asString().toUpperCase())));

        else
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(World_Name.asString())
                    .type(WorldType.valueOf(worldType.asString().toUpperCase())));

        if (world == null)
            dB.echoDebug(scriptEntry, "World is null! :(");

    }
}

package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class CreateWorldCommand extends AbstractCommand {



    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("generator")
                    && arg.matchesPrefix("generator, g"))
                scriptEntry.addObject("generator", arg.asElement());

            else if (!scriptEntry.hasObject("world_name"))
                scriptEntry.addObject("world_name", arg.asElement());

            else arg.reportUnhandled();
        }

        // Check for required information
        if (!scriptEntry.hasObject("world_name"))
            throw new InvalidArgumentsException("Must specify a world name.");
    }


    // TODO: Rewrite this entire everything, make it make sense.
    // TODO: At time of writing this todo, commandfile was full of Template stuff 0.o

    // TODO: also, save worlds through restart!
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Debug the execution
        // dB.report(getName(), required_integer.debug() + required_location.debug());

        World world;

        if (scriptEntry.hasObject("generator"))
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(scriptEntry.getElement("world_name").asString())
                    .generator(scriptEntry.getElement("generator").asString()));

        else
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(scriptEntry.getElement("world_name").asString()));

        if (world == null)
            dB.echoDebug(scriptEntry, "World is null! :(");

    }


}

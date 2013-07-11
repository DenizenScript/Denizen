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

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 *
 * If loading externally, implement dExternal and its load() method.
 *
 * @author Jeremy Schroeder
 */
public class CreateWorldCommand extends AbstractCommand /* implements dExternal */ {

    // @Override
    // public void load() {
    //     activate().as("MyCommand").withOptions("mycommand [#] [l@location]", 2);
    // }


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("generator")
                    && arg.matchesPrefix("generator, g"))
                scriptEntry.addObject("generator", arg.asElement());

            else if (!scriptEntry.hasObject("world_name"))
                scriptEntry.addObject("world_name", arg.asElement());

        }


        // Check for required information

        if (!scriptEntry.hasObject("world_name"))
            throw new InvalidArgumentsException("Must specify a world name.");
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        // Element required_integer = (Element) scriptEntry.getObject("required_integer");
        // dLocation required_location = (dLocation) scriptEntry.getObject("required_lcoation");


        // Debug the execution

        // dB.report(getName(), required_integer.debug() + required_location.debug());

        // Do the execution

        World world;

        if (scriptEntry.hasObject("generator"))
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(scriptEntry.getElement("world_name").asString())
                    .generator(scriptEntry.getElement("generator").asString()));

        else
            world = Bukkit.getServer().createWorld(WorldCreator
                    .name(scriptEntry.getElement("world_name").asString()));

        if (world == null)
            dB.echoDebug("World is null! :(");

    }


}
package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.interfaces.dExternal;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.Listener;

/**
 * Your command!
 * This class is a template for a Command in Denizen.
 *
 * If loading externally, implement dExternal and its load() method.
 *
 * @author Jeremy Schroeder
 */
public class _templateCommand extends AbstractCommand /* implements dExternal */ {

    // @Override
    // public void load() {
    // activate().as("MyCommand").withOptions("mycommand [#] [l@location]", 2);
    // }


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // if (!scriptEntry.hasObject("required_integer")
            //  && arg.matchesPrimitive(aH.PrimitiveType.Integer))
            //  scriptEntry.addObject("required_integer", arg.asElement());

            // if (!scriptEntry.hasObject("required_location")
            //  && arg.matchesArgumentType(dLocation.class))
            //  scriptEntry.addObject("required_location", arg.asType(dLocation.class));

        }


        // Check for required information

        // if (!scriptEntry.hasObject("required_object"))
        //  throw new InvalidArgumentsException("Must have required object!");

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        // Element required_integer = scriptEntry.getElement("required_integer");
        // dLocation required_location = (dLocation) scriptEntry.getObject("required_location");


        // Debug the execution

        // dB.report(getName(), required_integer.debug()
        //                    + required_location.debug());


        // Do the execution

        // INSERT
        // YOUR
        // CODE
        // HERE :)
    }

}

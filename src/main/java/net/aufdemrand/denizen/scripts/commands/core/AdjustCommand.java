package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

import java.lang.reflect.InvocationTargetException;


public class AdjustCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("object")) {
                scriptEntry.addObject("object", arg.asElement());

            } else if (!scriptEntry.hasObject("mechanism")) {
                if (arg.hasPrefix()) {
                    scriptEntry.addObject("mechanism", new Element(arg.getPrefix().getValue()));
                    scriptEntry.addObject("mechanism_value", arg.asElement());
                } else
                    scriptEntry.addObject("mechanism", arg.asElement());

            }

        }

        // Check for valid object

        // Check for valid mechanism

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String object = scriptEntry.getElement("object").asString();

        Class object_class = ObjectFetcher.getObjectClass(object.split("@")[0]);

        if (object_class == null)
            throw new CommandExecutionException("Unfetchable object found '" + object + '\'');

        dObject fetched;

        // Check to make sure this is a valid constructor by checking the 'matches' static method
        if (!ObjectFetcher.checkMatch(object_class, object))
            throw new CommandExecutionException('\'' + object + "' is returning null.");

        // Get the object with the 'valueOf' static method
        fetched = ObjectFetcher.getObjectFrom(object_class, object);

        // Make sure this object is Adjustable
        if (!(fetched instanceof Adjustable))
            throw new CommandExecutionException('\'' + object + "' is not adjustable.");

        // Do the adjustment!
        ((Adjustable) fetched).adjust(new Mechanism(scriptEntry.getElement("mechanism").asString()),
                scriptEntry.getElement("mechanism_value"));

        // :)

    }



}

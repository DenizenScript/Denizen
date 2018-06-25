package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.tags.core.ServerTags;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.core.UtilTags;


public class AdjustCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("object")) {
                scriptEntry.addObject("object", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("mechanism")) {
                if (arg.hasPrefix()) {
                    scriptEntry.addObject("mechanism", new Element(arg.getPrefix().getValue()));
                    scriptEntry.addObject("mechanism_value", arg.asElement());
                }
                else {
                    scriptEntry.addObject("mechanism", arg.asElement());
                    scriptEntry.addObject("mechanism_value", new Element(""));
                }

            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("object")) {
            throw new InvalidArgumentsException("You must specify an object!");
        }

        if (!scriptEntry.hasObject("mechanism")) {
            throw new InvalidArgumentsException("You must specify a mechanism!");
        }
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element mechanism = scriptEntry.getElement("mechanism");
        Element value = scriptEntry.getElement("mechanism_value");

        dList objects = scriptEntry.getdObject("object");

        dB.report(scriptEntry, getName(),
                objects.debug()
                        + mechanism.debug()
                        + value.debug());

        dList result = new dList();

        for (String object : objects) {
            if (object.equalsIgnoreCase("server")) {
                ServerTags.adjustServer(new Mechanism(mechanism, value));
                continue;
            }
            else if (object.equalsIgnoreCase("system")) {
                UtilTags.adjustSystem(new Mechanism(mechanism, value));
                continue;
            }

            Class object_class = ObjectFetcher.getObjectClass(object.split("@")[0]);

            if (object_class == null) {
                dB.echoError("Unfetchable object found '" + object + "'!");
                return;
            }

            dObject fetched;

            // Check to make sure this is a valid constructor by checking the 'matches' static method
            if (!ObjectFetcher.checkMatch(object_class, object)) {
                throw new CommandExecutionException('\'' + object + "' is returning null.");
            }

            // Get the object with the 'valueOf' static method
            fetched = ObjectFetcher.getObjectFrom(object_class, object);

            // Make sure this object is Adjustable
            if (fetched == null || !(fetched instanceof Adjustable)) {
                dB.echoError("'" + object + "' is not adjustable.");
                return;
            }

            // Do the adjustment!
            ((Adjustable) fetched).adjust(new Mechanism(mechanism, value));

            // Add it to the entry for later access
            if (objects.size() == 1) {
                scriptEntry.addObject("result", fetched);
            }
            result.add(fetched.identify());

            // :)
        }

        scriptEntry.addObject("result_list", result);

    }
}

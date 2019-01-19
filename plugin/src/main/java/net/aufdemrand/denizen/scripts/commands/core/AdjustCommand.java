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


    public dObject adjust(dObject object, Element mechanism, Element value, ScriptEntry entry) {
        String objectString = object.toString();
        if (objectString.equalsIgnoreCase("server")) {
            ServerTags.adjustServer(new Mechanism(mechanism, value));
            return object;
        }
        else if (objectString.equalsIgnoreCase("system")) {
            UtilTags.adjustSystem(new Mechanism(mechanism, value));
            return object;
        }
        if (object instanceof Element) {
            object = ObjectFetcher.pickObjectFor(objectString, entry.entryData.getTagContext());
            if (object instanceof Element) {
                dB.echoError("Unable to determine what object to adjust (missing object notation?), for: " + objectString);
                return object;
            }
        }
        // Make sure this object is Adjustable
        if (!(object instanceof Adjustable)) {
            dB.echoError("'" + objectString + "' is not an adjustable object type.");
            return object;
        }
        ((Adjustable) object).adjust(new Mechanism(mechanism, value));
        return object;
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element mechanism = scriptEntry.getElement("mechanism");
        Element value = scriptEntry.getElement("mechanism_value");

        dList objects = scriptEntry.getdObject("object");

        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    objects.debug()
                            + mechanism.debug()
                            + (value == null ? "" : value.debug()));
        }

        dList result = new dList();

        for (dObject object : objects.objectForms) {
            object = adjust(object, mechanism, value, scriptEntry);
            if (objects.size() == 1) {
                scriptEntry.addObject("result", object);
            }
            result.addObject(object);
        }

        scriptEntry.addObject("result_list", result);

    }
}

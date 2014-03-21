package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.HashMap;
import java.util.Map;

public class EventCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("events")) {
                scriptEntry.addObject("events", arg.asType(dList.class));
            }

            else if (!scriptEntry.hasObject("context")
                    && arg.matchesPrefix("context", "c")) {
                scriptEntry.addObject("context", arg.asType(dList.class));
            }

            else
                arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("events"))
            throw new InvalidArgumentsException("Must specify a list of event names!");

        scriptEntry.defaultObject("context", new dList());

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dList events = (dList) scriptEntry.getObject("events");
        dList context = (dList) scriptEntry.getObject("context");

        dB.report(scriptEntry, getName(), events.debug() + context.debug());

        if (context.size() % 2 == 1) { // Size is uneven!
            context.add("null");
        }

        // Change the context input to a list of objects
        Map<String, dObject> context_map = new HashMap<String, dObject>();
        for (int i = 0; i < context.size(); i += 2) {
            context_map.put(context.get(i), ObjectFetcher.pickObjectFor(context.get(i + 1)));
        }

        String Determination = EventManager.doEvents(events, scriptEntry.getNPC(), scriptEntry.getPlayer(), context_map, true);
        scriptEntry.addObject("determination", new Element(Determination));
    }

}

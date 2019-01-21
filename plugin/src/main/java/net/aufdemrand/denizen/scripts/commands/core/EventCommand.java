package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.ObjectFetcher;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("context")
                    && arg.matchesPrefix("context", "c")) {
                scriptEntry.addObject("context", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("events")) {
                scriptEntry.addObject("events", arg.asType(dList.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("events")) {
            throw new InvalidArgumentsException("Must specify a list of event names!");
        }

        scriptEntry.defaultObject("context", new dList());

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dList events = (dList) scriptEntry.getObject("events");
        dList context = (dList) scriptEntry.getObject("context");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), events.debug() + context.debug());

        }

        if (context.size() % 2 == 1) { // Size is uneven!
            context.add("null");
        }

        // Change the context input to a list of objects
        Map<String, dObject> context_map = new HashMap<String, dObject>();
        for (int i = 0; i < context.size(); i += 2) {
            context_map.put(context.get(i), ObjectFetcher.pickObjectFor(context.get(i + 1), scriptEntry.entryData.getTagContext()));
        }

        List<String> Determination = OldEventManager.doEvents(events,
                scriptEntry.entryData, context_map, true);
        scriptEntry.addObject("determinations", new dList(Determination));
    }
}

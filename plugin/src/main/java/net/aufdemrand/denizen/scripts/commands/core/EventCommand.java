package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldEventManager;
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

    // <--[command]
    // @Name Event
    // @Syntax event [<event name>|...] (context:<name>|<object>|...)
    // @Required 1
    // @Short Manually fires a world event.
    // @Group core
    //
    // @Description
    // This command will trigger a world event (an event within a 'world' type script) exactly the same
    // as if an actual serverside event had caused it.
    // You can specify as many event names as you want in the list, they will all be fired. It will also automatically
    // fire a duplicate of each event name with object identifiers (eg 'i@', see <@link language dobject>) removed.
    // The script's linked player and NPC will automatically be sent through to the event.
    // To add context information (tags like <context.location>) to the event, simply specify all context values in a list.
    // Note that there are some inherent limitations... EG, you can't directly add a list to the context currently.
    // To do this, the best way is to just escape the list value (see <@link language property escaping>).
    //
    // NOTE: This command is outdated and bound to be updated.
    //
    // @Tags
    // <server.has_event[<event_name>]>
    // <server.event_handlers[<event_name>]>
    // <entry[saveName].determinations> returns a list of the determined values (if any) from the event.
    //
    // @Usage
    // Use to trigger a custom event
    // - event "player triggers custom event"
    //
    // @Usage
    // Use to trigger multiple custom events with context
    // - event "player triggers custom event|player causes event" context:event|custom|npc|<player.selected_npc>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
    public void execute(ScriptEntry scriptEntry) {

        dList events = (dList) scriptEntry.getObject("events");
        dList context = (dList) scriptEntry.getObject("context");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), events.debug() + context.debug());

        }

        if (context.size() % 2 == 1) { // Size is uneven!
            context.add("null");
        }

        // Change the context input to a list of objects
        Map<String, dObject> context_map = new HashMap<>();
        for (int i = 0; i < context.size(); i += 2) {
            context_map.put(context.get(i), ObjectFetcher.pickObjectFor(context.get(i + 1), scriptEntry.entryData.getTagContext()));
        }

        List<String> Determination = OldEventManager.doEvents(events,
                scriptEntry.entryData, context_map, true);
        scriptEntry.addObject("determinations", new dList(Determination));
    }
}

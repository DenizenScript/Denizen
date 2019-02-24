package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.ObjectFetcher;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(dNPC.class)) {
                scriptEntry.addObject("npcs", arg.asType(dList.class).filter(dNPC.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("context")
                    && arg.matchesPrefix("context", "c")) {
                scriptEntry.addObject("context", arg.asType(dList.class));
            }
            else if (!scriptEntry.hasObject("actions")) {
                scriptEntry.addObject("actions", arg.asType(dList.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("actions")) {
            throw new InvalidArgumentsException("Must specify a list of action names!");
        }

        if (!scriptEntry.hasObject("npcs")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                scriptEntry.addObject("npcs", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC()));
            }
            else {
                throw new InvalidArgumentsException("Must specify an NPC to use!");
            }
        }

        scriptEntry.defaultObject("context", new dList());

    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        dList actions = (dList) scriptEntry.getObject("actions");
        dList context = (dList) scriptEntry.getObject("context");
        List<dNPC> npcs = (List<dNPC>) scriptEntry.getObject("npcs");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), actions.debug() + context.debug() + aH.debugList("npcs", npcs));

        }

        if (context.size() % 2 == 1) { // Size is uneven!
            context.add("null");
        }

        // Change the context input to a list of objects
        Map<String, dObject> context_map = new HashMap<String, dObject>();
        for (int i = 0; i < context.size(); i += 2) {
            context_map.put(context.get(i), ObjectFetcher.pickObjectFor(context.get(i + 1), scriptEntry.entryData.getTagContext()));
        }

        for (dNPC npc : npcs) {
            for (String action : actions) {
                npc.action(action, ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), context_map);
            }
        }
    }
}

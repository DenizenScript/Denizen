package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.Utilities;
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

    // <--[command]
    // @Name Action
    // @Syntax action [<action name>|...] (<npc>|...) (context:<name>|<object>|...)
    // @Required 1
    // @Short Manually fires an NPC action.
    // @Group npc
    //
    // @Description
    // This command will trigger an NPC action (an action within an 'assignment' type script attached to the NPC) exactly the same
    // as if an actual serverside event had caused it.
    // You can specify as many action names as you want in the list, they will all be fired.
    // You may also specify as many NPCs as you would like to run the action on, in a list.
    // If no NPCs are specified, the NPC linked to the script will be assumed.
    // The script's linked player and the specified NPC will automatically be sent through to the action.
    // To add context information (tags like <context.location>) to the action, simply specify all context values in a list.
    // Note that there are some inherent limitations... EG, you can't directly add a list to the context currently.
    // To do this, the best way is to just escape the list value (see <@link language property escaping>).
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to trigger a custom action
    // - action "custom action"
    //
    // @Usage
    // Use to trigger multiple custom action with context on a different NPC
    // - action "player dances|target enemy" n@10 context:action|custom|target|<player.selected_npc>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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
            if (Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("npcs", Arrays.asList(Utilities.getEntryNPC(scriptEntry)));
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
        Map<String, dObject> context_map = new HashMap<>();
        for (int i = 0; i < context.size(); i += 2) {
            context_map.put(context.get(i), ObjectFetcher.pickObjectFor(context.get(i + 1), scriptEntry.entryData.getTagContext()));
        }

        for (dNPC npc : npcs) {
            for (String action : actions) {
                npc.action(action, Utilities.getEntryPlayer(scriptEntry), context_map);
            }
        }
    }
}

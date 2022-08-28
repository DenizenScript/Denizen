package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.*;

public class ActionCommand extends AbstractCommand {

    public ActionCommand() {
        setName("action");
        setSyntax("action [<action name>|...] (<npc>|...) (context:<name>|<object>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Action
    // @Syntax action [<action name>|...] (<npc>|...) (context:<name>|<object>|...)
    // @Required 1
    // @Maximum 3
    // @Plugin Citizens
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
    // To do this, the best way is to just escape the list value (see <@link language Escaping System>).
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
    // - action "player dances|target enemy" <[some_npc]> context:action|custom|target|<player.selected_npc>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(NPCTag.class)) {
                scriptEntry.addObject("npcs", arg.asType(ListTag.class).filter(NPCTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("context")
                    && arg.matchesPrefix("context", "c")) {
                scriptEntry.addObject("context", arg.asType(ListTag.class)); // TODO: MapTag?
            }
            else if (!scriptEntry.hasObject("actions")) {
                scriptEntry.addObject("actions", arg.asType(ListTag.class));
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
                scriptEntry.addObject("npcs", Collections.singletonList(Utilities.getEntryNPC(scriptEntry)));
            }
            else {
                throw new InvalidArgumentsException("Must specify an NPC to use!");
            }
        }
        scriptEntry.defaultObject("context", new ListTag());
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ListTag actions = scriptEntry.getObjectTag("actions");
        ListTag context = scriptEntry.getObjectTag("context");
        List<NPCTag> npcs = (List<NPCTag>) scriptEntry.getObject("npcs");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), actions, context, db("npcs", npcs));
        }
        if (context.size() % 2 == 1) { // Size is uneven!
            context.add("null");
        }
        // Change the context input to a list of objects
        Map<String, ObjectTag> context_map = new HashMap<>();
        for (int i = 0; i < context.size(); i += 2) {
            context_map.put(context.get(i), ObjectFetcher.pickObjectFor(context.get(i + 1), scriptEntry.getContext()));
        }
        for (NPCTag npc : npcs) {
            for (String action : actions) {
                npc.action(action, Utilities.getEntryPlayer(scriptEntry), context_map);
            }
        }
    }
}

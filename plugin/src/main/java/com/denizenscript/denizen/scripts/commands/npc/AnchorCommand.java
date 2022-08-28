package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.util.Anchor;

import java.util.Arrays;

public class AnchorCommand extends AbstractCommand {

    public AnchorCommand() {
        setName("anchor");
        setSyntax("anchor [id:<name>] [remove/add <location>]");
        setRequiredArguments(2, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Anchor
    // @Syntax anchor [id:<name>] [remove/add <location>]
    // @Required 2
    // @Maximum 3
    // @Plugin Citizens
    // @Short Controls an NPC's Anchor Trait.
    // @Group npc
    //
    // @Description
    // The anchor system inside Citizens allows locations to be 'bound' to an NPC, saved by an 'id'.
    // The anchor command can add and remove new anchors.
    // The Anchors Trait can also be used as a sort of 'waypoints' system.
    // As the Anchor command is an NPC specific command, a valid npc object must be referenced in the script entry.
    // If none is provided by default, use the 'npc:<npc>' argument.
    //
    // @Tags
    // <NPCTag.anchor[anchor_name]>
    // <NPCTag.list_anchors>
    // <NPCTag.has_anchors>
    //
    // @Usage
    // Use to add and remove anchors to an NPC.
    // - define location_name <context.message>
    // - chat "I have saved this location as <[location_name]>.'
    // - anchor add <npc.location> id:<[location_name]>
    // -->

    private enum Action { ADD, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.class)) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("range")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("range", "r")) {
                scriptEntry.addObject("range", arg.asElement());
            }
            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id", "i")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("NPC linked was missing or invalid.");
        }
        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify an 'Anchor Action'. Valid: " + Arrays.asList(Action.values()));
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must specify an ID.");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        Action action = (Action) scriptEntry.getObject("action");
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag range = scriptEntry.getElement("range");
        ElementTag id = scriptEntry.getElement("id");
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), npc, db("action", action.name()), id, location, range);
        }
        Anchors anchors = npc.getCitizen().getOrAddTrait(Anchors.class);
        switch (action) {
            case ADD: {
                if (location == null) {
                    Debug.echoError("Must specify a location!");
                    return;
                }
                Anchor existing = anchors.getAnchor(id.asString());
                if (existing != null) {
                    anchors.removeAnchor(existing);
                }
                anchors.addAnchor(id.asString(), location);
                break;
            }
            case REMOVE: {
                Anchor n = anchors.getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry, "Invalid anchor name '" + id.asString() + "'");
                }
                else {
                    anchors.removeAnchor(n);
                }
                break;
            }
        }
    }
}

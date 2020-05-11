package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.util.Anchor;

import java.util.Arrays;

public class AnchorCommand extends AbstractCommand {

    public AnchorCommand() {
        setName("anchor");
        setSyntax("anchor [id:<name>] [remove/add <location>]");
        setRequiredArguments(2, 3);
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
    // The anchor system inside Citizens2 allows locations to be 'bound' to an NPC, saved by an 'id'.
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

    private enum Action {ADD, REMOVE, ASSUME, WALKTO, WALKNEAR}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
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
            Debug.report(scriptEntry, getName(),
                    npc.debug() + action.name() + id.debug()
                            + (location != null ? location.debug() : "")
                            + (range != null ? range.debug() : ""));
        }

        if (!npc.getCitizen().hasTrait(Anchors.class)) {
            npc.getCitizen().addTrait(Anchors.class);
        }

        switch (action) {
            case ADD:
                if (location == null) {
                    Debug.echoError("Must specify a location!");
                    return;
                }
                npc.getCitizen().getTrait(Anchors.class).addAnchor(id.asString(), location);
                return;
            case ASSUME: {
                Deprecations.anchorWalk.warn(scriptEntry);
                Anchor n = npc.getCitizen().getTrait(Anchors.class).getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid anchor name '" + id.asString() + "'");
                }
                else {
                    npc.getEntity().teleport(n.getLocation());
                }
            }
            return;
            case WALKNEAR: {
                Deprecations.anchorWalk.warn(scriptEntry);
                Anchor n = npc.getCitizen().getTrait(Anchors.class).getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid anchor name '" + id.asString() + "'");
                }
                else if (range == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a range!");
                }
                else {
                    npc.getNavigator().setTarget(Utilities.getWalkableLocationNear(n.getLocation(), range.asInt()));
                }
            }
            return;
            case WALKTO: {
                Deprecations.anchorWalk.warn(scriptEntry);
                Anchor n = npc.getCitizen().getTrait(Anchors.class).getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid anchor name '" + id.asString() + "'");
                }
                else {
                    npc.getNavigator().setTarget(n.getLocation());
                }
            }
            return;
            case REMOVE: {
                Anchor n = npc.getCitizen().getTrait(Anchors.class).getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid anchor name '" + id.asString() + "'");
                }
                else {
                    npc.getCitizen().getTrait(Anchors.class).removeAnchor(n);
                }
            }
        }
    }
}

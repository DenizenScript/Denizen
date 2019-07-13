package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.util.Anchor;

import java.util.Arrays;

public class AnchorCommand extends AbstractCommand {

    // <--[command]
    // @Name Anchor
    // @Syntax anchor [id:<name>] [assume/remove/add <location>/walkto/walknear (r:#)]
    // @Required 2
    // @Short Controls an NPC's Anchor Trait.
    // @Group npc
    //
    // @Description
    // The anchor system inside Citizens2 allows locations to be 'bound' to an NPC, saved by an 'id'. The anchor
    // command can add and remove new anchors, as well as the ability to teleport NPCs to anchors with the 'assume'
    // argument.
    // The Anchors Trait can also be used as a sort of 'waypoints' system. For ease of use, the anchor command
    // provides function for NPCs to walk to or walk near an anchor.
    // As the Anchor command is an NPC specific command, a valid npc object must be referenced in the script entry.
    // If none is provided by default, the use of the 'npc:n@id' argument, replacing the id with the npcid of the
    // NPC desired, can create a link, or alternatively override the default linked npc.
    //
    // @Tags
    // <n@npc.anchor[anchor_name]>
    // <n@npc.anchor.list>
    // <n@npc.has_anchors>
    //
    // @Usage
    // Use to add and remove anchors to an NPC.
    // - define location_name <context.message>
    // - chat "I have saved this location as <def[location_name]>.'
    // - anchor add <npc.location> "id:<def[location_name]>"
    //
    // @Usage
    // Use to make an NPC walk to or walk near a saved anchor.
    // - anchor walkto i:waypoint_1
    // - anchor walknear i:waypoint_2 r:5
    // -->

    private enum Action {ADD, REMOVE, ASSUME, WALKTO, WALKNEAR}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (Argument arg : ArgumentHelper.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("range")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("range", "r")) {
                scriptEntry.addObject("range", arg.asElement());
            }
            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id", "i")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check required arguments
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("NPC linked was missing or invalid.");
        }

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify an 'Anchor Action'. Valid: " + Arrays.asList(Action.values()));
        }

    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Get objects
        Action action = (Action) scriptEntry.getObject("action");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element range = (Element) scriptEntry.getObject("range");
        Element id = (Element) scriptEntry.getObject("id");
        dNPC npc = Utilities.getEntryNPC(scriptEntry);

        // Report to dB
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
                npc.getCitizen().getTrait(Anchors.class).addAnchor(id.asString(), location);
                return;

            case ASSUME: {
                Anchor n = npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid anchor name '" + id.asString() + "'");
                }
                else {
                    npc.getEntity().teleport(n.getLocation());
                }
            }
            return;

            case WALKNEAR: {
                Anchor n = npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid anchor name '" + id.asString() + "'");
                }
                else if (range == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a range!");
                }
                else {
                    npc.getNavigator().setTarget(
                            Utilities.getWalkableLocationNear(n.getLocation(), range.asInt()));
                }
            }
            return;

            case WALKTO: {
                Anchor n = npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString());
                if (n == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Invalid anchor name '" + id.asString() + "'");
                }
                else {
                    npc.getNavigator().setTarget(n.getLocation());
                }
            }
            return;

            case REMOVE: {
                Anchor n = npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString());
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

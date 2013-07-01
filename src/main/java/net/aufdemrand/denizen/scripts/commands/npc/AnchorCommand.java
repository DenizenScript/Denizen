package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.trait.Anchors;

import static net.aufdemrand.denizen.objects.aH.Argument;

/**
 *
 * Controls a NPC's 'Anchors' trait.
 *
 * @author aufdemrand
 *
 */
public class AnchorCommand extends AbstractCommand {

    public String getHelp() {
        return  "Controls NPCs' anchors. Anchors are 'bookmarked locations'" +
                "stored at the NPC-level. Valid actions are Add, Remove, " +
                "Assume Walk_To, and Walk_Near. \n" +
                " \n" +
                "Use to add or remove an anchor from a NPC. \n" +
                "- anchor add i:pride_rock <npc.location> \n" +
                "- anchor remove i:the_jungle \n" +
                "Use to 'teleport' the npc directly to an existing anchor. \n" +
                "- anchor assume i:bedroom \n" +
                "Use to make a NPC navigate to or near an anchor for easy" +
                "'waypoint behavior'. \n" +
                "- anchor walkto i:anchor_1 \n" +
                "- anchor walknear i:front_porch r:5";
    }

    public String getUsage() {
        return "- anchor [<action>] [id|i:id_name] ([<location>]) ([range|r:#])";
    }

    private enum Action { ADD, REMOVE, ASSUME, WALKTO, WALKNEAR }

    public static final	String  RANGE_ARG = "range, r";
    public static final	String     ID_ARG = "id, i";

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values()))
                // add Action
            	scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));


            else if (!scriptEntry.hasObject("range")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix(RANGE_ARG))
                // add range (for WALKNEAR)
                scriptEntry.addObject("range", arg.asElement());


            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix(ID_ARG))
                // add anchor ID
                scriptEntry.addObject("id", arg.asElement());


            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // add location (for ADD)
                scriptEntry.addObject("location", arg.asType(dLocation.class));


            else dB.echoError("Unhandled argument: '" + arg.raw_value + "'");
        }


        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify an 'Anchor Action'.");

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
    	Action action = (Action) scriptEntry.getObject("action");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element range = (Element) scriptEntry.getObject("range");
        Element id = (Element) scriptEntry.getObject("id");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("NPC", scriptEntry.getNPC().toString())
                        + action.name() + id.debug()
                        + (location != null ? location.debug() : "")
                        + (range != null ? range.debug() : "" ));

        dNPC npc = scriptEntry.getNPC();

        switch (action) {

            case ADD:
                npc.getCitizen().getTrait(Anchors.class).addAnchor(id.asString(), location);
                return;

            case ASSUME:
                npc.getEntity().teleport(npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString()).getLocation());
                return;

            case WALKNEAR:
                npc.getNavigator().setTarget(
                        Utilities.getWalkableLocationNear(npc.getCitizen().getTrait(Anchors.class)
                                .getAnchor(id.asString()).getLocation(), range.asInt()));
                return;

            case WALKTO:
                npc.getNavigator().setTarget(npc.getCitizen().getTrait(Anchors.class)
                        .getAnchor(id.asString()).getLocation());
                return;

            case REMOVE:
                npc.getCitizen().getTrait(Anchors.class)
                        .removeAnchor(npc.getCitizen().getTrait(Anchors.class)
                                .getAnchor(id.asString()));
                return;
        }


    }
}
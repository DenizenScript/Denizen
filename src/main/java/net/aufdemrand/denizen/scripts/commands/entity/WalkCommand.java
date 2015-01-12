package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.entity.EntityMovement;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationEvent;
import net.citizensnpcs.api.ai.flocking.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles NPC walking with the Citizens API.
 *
 * @author Jeremy Schroeder
 */
public class WalkCommand extends AbstractCommand implements Listener, Holdable {

    //                        percentage
    // walk [location] (speed:#.#) (auto_range)
    //

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Percentage)
                    && arg.matchesPrefix("s, speed"))
                scriptEntry.addObject("speed", arg.asElement());

            else if (!scriptEntry.hasObject("auto_range")
                    && arg.matches("auto_range"))
                scriptEntry.addObject("auto_range", Element.TRUE);

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("radius"))
                scriptEntry.addObject("radius", arg.asElement());

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class))
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));

            else
                arg.reportUnhandled();
        }


        // Check for required information

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");

        if (!scriptEntry.hasObject("entities")) {
            if (((BukkitScriptEntryData)scriptEntry.entryData).getNPC() == null
                    || !((BukkitScriptEntryData)scriptEntry.entryData).getNPC().isValid()
                    || !((BukkitScriptEntryData)scriptEntry.entryData).getNPC().isSpawned())
                throw new InvalidArgumentsException("Must have a valid spawned NPC attached.");
            else
                scriptEntry.addObject("entities", Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getNPC()));
        }


    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        dLocation loc = (dLocation) scriptEntry.getObject("location");
        Element speed = scriptEntry.getElement("speed");
        Element auto_range = scriptEntry.getElement("auto_range");
        Element radius = scriptEntry.getElement("radius");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");


        // Debug the execution

        dB.report(scriptEntry, getName(), loc.debug()
                + (speed != null ? speed.debug() : "")
                + (auto_range != null ? auto_range.debug() : "")
                + (radius != null ? radius.debug(): "")
                + (aH.debugObj("entities", entities)));

        // Do the execution

        List<dNPC> npcs = new ArrayList<dNPC>();
        for (dEntity entity : entities) {
            if (entity.isNPC()) {
                dNPC npc = entity.getDenizenNPC();
                npcs.add(npc);
                if (!npc.isSpawned()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "NPC " + npc.identify() + " is not spawned!");
                    continue;
                }
                if (auto_range != null
                        && auto_range == Element.TRUE) {
                    double distance = npc.getLocation().distance(loc);
                    if (npc.getNavigator().getLocalParameters().range() < distance + 10)
                        npc.getNavigator().getDefaultParameters().range((float) distance + 10);
                    // TODO: Should be using local params rather than default?
                }

                npc.getNavigator().setTarget(loc);

                if (speed != null)
                    npc.getNavigator().getLocalParameters().speedModifier(speed.asFloat());

                if (radius != null) {
                    NPCFlock flock = new RadiusNPCFlock(radius.asDouble());
                    Flocker flocker = new Flocker(npc.getCitizen(), flock, new SeparationBehavior(Flocker.LOW_INFLUENCE),
                            new CohesionBehavior(Flocker.LOW_INFLUENCE), new AlignmentBehavior(Flocker.HIGH_INFLUENCE));
                    npc.getNavigator().getLocalParameters().addRunCallback(flocker);
                }
            }
            else {
                EntityMovement.walkTo(entity.getBukkitEntity(), loc, speed != null ? speed.asDouble() : 0.3);
            }
        }

        if (scriptEntry.shouldWaitFor()) {
            held.add(scriptEntry);
            scriptEntry.addObject("tally", npcs);
            // TODO: make non-NPC entities waitable
        }

    }


    // Held script entries
    public static List<ScriptEntry> held = new ArrayList<ScriptEntry>();

    @EventHandler
    public void finish(NavigationCompleteEvent e) {

        if (held.isEmpty()) return;

        checkHeld(e);

    }

    @EventHandler
    public void cancel(NavigationCancelEvent e) {

        if (held.isEmpty()) return;

        checkHeld(e);

    }


    public void checkHeld(NavigationEvent e) {
        if (e.getNPC() == null)
            return;

        // Check each held entry -- the scriptExecuter is waiting on
        // the entry to be marked 'waited for'.
        for (int i = 0; i < held.size(); i++) {
            ScriptEntry entry = held.get(i);

            // Get all NPCs associated with the entry. They must all
            // finish navigation before the entry can be let go
            List<dNPC> tally = (List<dNPC>) entry.getObject("tally");
            // If the NPC is the NPC from the event, take it from the list.
            tally.remove(dNPC.mirrorCitizensNPC(e.getNPC()));

            // Check if tally is empty.
            if (tally.isEmpty()) {
                entry.setFinished(true);
                held.remove(i);
                i--;
            }
        }
    }

    @Override
    public void onEnable() {
        if (Depends.citizens != null) {
            DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                    .registerEvents(this, DenizenAPI.getCurrentInstance());
        }
    }
}

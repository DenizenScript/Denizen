package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.waypoint.Waypoints;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PauseCommand extends AbstractCommand {

    // <--[command]
    // @Name Pause
    // @Syntax pause [waypoints/activity] (<duration>)
    // @Required 1
    // @Short Pauses an NPC's waypoint navigation or goal activity temporarily or indefinitely.
    // @Group npc
    //
    // @Description
    // TODO: Document Command Details
    //
    // @Tags
    // <NPCTag.navigator.is_navigating>
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation indefinitely.
    // - pause waypoints
    //
    // @Usage
    // Use to pause an NPC's goal activity temporarily.
    // - pause activity 1m
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation and then resume it.
    // - pause waypoints
    // - resume waypoints
    // -->

    // <--[command]
    // @Name Resume
    // @Syntax resume [waypoints/activity] (<duration>)
    // @Required 1
    // @Plugin Citizens
    // @Short Resumes an NPC's waypoint navigation or goal activity temporarily or indefinitely.
    // @Group npc
    //
    // @Description
    // TODO: Document Command Details
    //
    // @Tags
    // <NPCTag.navigator.is_navigating>
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation indefinitely.
    // - pause waypoints
    //
    // @Usage
    // Use to pause an NPC's goal activity temporarily.
    // - pause activity 1m
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation and then resume it.
    // - pause waypoints
    // - resume waypoints
    // -->

    private Map<String, Integer> durations = new ConcurrentHashMap<>(8, 0.9f, 1);

    enum PauseType {ACTIVITY, WAYPOINTS, NAVIGATION}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (arg.matchesArgumentType(DurationTag.class)
                    && !scriptEntry.hasObject("duration")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            if (!scriptEntry.hasObject("pause_type")
                    && arg.matchesEnum(PauseType.values())) {
                scriptEntry.addObject("pause_type", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("pause_type")) {
            throw new InvalidArgumentsException("Must specify a pause type!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        DurationTag duration = scriptEntry.getObjectTag("duration");
        ElementTag pauseTypeElement = scriptEntry.getElement("pause_type");

        PauseType pauseType = PauseType.valueOf(pauseTypeElement.asString().toUpperCase());

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (duration == null ? "" : duration.debug()) + pauseTypeElement.debug());
        }

        NPCTag npc = null;
        if (Utilities.getEntryNPC(scriptEntry) != null) {
            npc = Utilities.getEntryNPC(scriptEntry);
        }
        pause(npc, pauseType, !scriptEntry.getCommandName().equalsIgnoreCase("RESUME"));

        // If duration...
        if (duration != null) {
            if (durations.containsKey(npc.getCitizen().getId() + pauseType.name())) {
                try {
                    DenizenAPI.getCurrentInstance().getServer().getScheduler().cancelTask(durations.get(npc.getCitizen().getId() + pauseType.name()));
                }
                catch (Exception e) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "There was an error pausing that!");
                    Debug.echoError(scriptEntry.getResidingQueue(), e);
                }

            }
            Debug.echoDebug(scriptEntry, "Running delayed task: Unpause " + pauseType.toString());

            final NPCTag theNpc = npc;
            final ScriptEntry se = scriptEntry;
            durations.put(npc.getId() + pauseType.name(), DenizenAPI.getCurrentInstance()
                    .getServer().getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                            new Runnable() {
                                @Override
                                public void run() {
                                    Debug.echoDebug(se, "Running delayed task: Pausing " + pauseType.toString());
                                    pause(theNpc, pauseType, false);

                                }
                            }, duration.getTicks()));
        }
    }

    public void pause(NPCTag denizen, PauseType pauseType, boolean pause) {
        switch (pauseType) {

            case WAYPOINTS:
                denizen.getCitizen().getTrait(Waypoints.class).getCurrentProvider().setPaused(pause);
                if (pause) {
                    denizen.getNavigator().cancelNavigation();
                }
                return;

            case ACTIVITY:
                denizen.getCitizen().getDefaultGoalController().setPaused(pause);
                return;

            case NAVIGATION:
                // TODO: Finish this
        }

    }
}

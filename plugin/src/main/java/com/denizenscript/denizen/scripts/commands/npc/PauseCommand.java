package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.waypoint.Waypoints;

import java.util.HashMap;
import java.util.Map;

public class PauseCommand extends AbstractCommand {

    public static class ResumeCommand extends PauseCommand {

        public ResumeCommand() {
            setName("resume");
            setSyntax("resume [waypoints/activity] (<duration>)");
        }
    }

    public PauseCommand() {
        setName("pause");
        setSyntax("pause [waypoints/activity] (<duration>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Pause
    // @Syntax pause [waypoints/activity] (<duration>)
    // @Required 1
    // @Maximum 2
    // @Plugin Citizens
    // @Short Pauses an NPC's waypoint navigation or goal activity temporarily or indefinitely.
    // @Group npc
    //
    // @Description
    // The pause command pauses an NPC's waypoint navigation or goal activity temporarily or indefinitely.
    // This works along side <@link command resume>.
    //
    // "Waypoints" refers to the NPC's path navigation, usually set via "/npc path".
    //
    // "Activity" refers to the Citizens AI Goal system, which may be used by some plugins but usually is not.
    //
    // If no duration is specified, the resume command must be used to unpause it.
    //
    // @Tags
    // <NPCTag.is_navigating>
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
    // The resume command resumes an NPC's waypoint navigation or goal activity temporarily or indefinitely.
    // This works along side <@link command pause>.
    // See the documentation of the pause command for more details.
    //
    // @Tags
    // <NPCTag.is_navigating>
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation and then resume it.
    // - pause waypoints
    // - resume waypoints
    // -->

    private Map<String, Integer> durations = new HashMap<>();

    enum PauseType {ACTIVITY, WAYPOINTS, NAVIGATION}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesArgumentType(DurationTag.class)
                    && !scriptEntry.hasObject("duration")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("pause_type")
                    && arg.matchesEnum(PauseType.class)) {
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
            Debug.report(scriptEntry, getName(), duration, pauseTypeElement);
        }
        NPCTag npc = null;
        if (Utilities.getEntryNPC(scriptEntry) != null) {
            npc = Utilities.getEntryNPC(scriptEntry);
        }
        pause(npc, pauseType, !scriptEntry.getCommandName().equalsIgnoreCase("RESUME"));
        if (duration != null) {
            if (durations.containsKey(npc.getCitizen().getId() + pauseType.name())) {
                try {
                    Denizen.getInstance().getServer().getScheduler().cancelTask(durations.get(npc.getCitizen().getId() + pauseType.name()));
                }
                catch (Exception e) {
                    Debug.echoError(scriptEntry, "There was an error pausing that!");
                    Debug.echoError(scriptEntry, e);
                }
            }
            Debug.echoDebug(scriptEntry, "Running delayed task: Unpause " + pauseType);
            final NPCTag theNpc = npc;
            final ScriptEntry se = scriptEntry;
            durations.put(npc.getId() + pauseType.name(), Denizen.getInstance()
                    .getServer().getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(),
                            () -> {
                                Debug.echoDebug(se, "Running delayed task: Pausing " + pauseType);
                                pause(theNpc, pauseType, false);

                            }, duration.getTicks()));
        }
    }

    public void pause(NPCTag denizen, PauseType pauseType, boolean pause) {
        switch (pauseType) {
            case WAYPOINTS:
                denizen.getCitizen().getOrAddTrait(Waypoints.class).getCurrentProvider().setPaused(pause);
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

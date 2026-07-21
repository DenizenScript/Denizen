package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.waypoint.Waypoints;

import java.util.HashMap;
import java.util.Map;

public class PauseCommand extends AbstractCommand {

    public PauseCommand() {
        setName("pause");
        setSyntax("pause [waypoints/activity] (<duration>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
        autoCompile();
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

    public static final Map<NPCData, Integer> durations = new HashMap<>();

    public enum Type { ACTIVITY, WAYPOINTS, NAVIGATION }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") Type type,
                                   @ArgName("duration") @ArgLinear @ArgDefaultNull DurationTag duration) {
        executeToggle(scriptEntry, type, duration, true);
    }

    public static void executeToggle(ScriptEntry scriptEntry, Type type, DurationTag duration, boolean pause) {
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsRuntimeException("Need to provide an NPC");
        }
        NPCData data = new NPCData(Utilities.getEntryNPC(scriptEntry), type);
        toggle(data, pause);
        if (duration != null) {
            if (durations.containsKey(data)) {
                try {
                    Denizen.getInstance().getServer().getScheduler().cancelTask(durations.get(data));
                }
                catch (Exception e) {
                    Debug.echoError(scriptEntry, "There was an error pausing that!");
                    Debug.echoError(scriptEntry, e);
                }
            }
            durations.put(data, Denizen.getInstance()
                    .getServer().getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(),
                            () -> {
                                Debug.echoDebug(scriptEntry, "Running delayed task: " + (!pause ? "Pausing" : "Resuming") + " " + type);
                                toggle(data, !pause);
                            }, duration.getTicks()));
        }
    }

    public static void toggle(NPCData data, boolean pause) {
        switch (data.type) {
            case WAYPOINTS -> {
                data.npc.getCitizen().getOrAddTrait(Waypoints.class).getCurrentProvider().setPaused(pause);
                if (pause) {
                    data.npc.getNavigator().cancelNavigation();
                }
            }
            case ACTIVITY -> data.npc.getCitizen().getDefaultBehaviorController().setPaused(pause);
            case NAVIGATION -> { /* TODO IMPLEMENT */ }
        }
    }

    public record NPCData(NPCTag npc, Type type) { }
}

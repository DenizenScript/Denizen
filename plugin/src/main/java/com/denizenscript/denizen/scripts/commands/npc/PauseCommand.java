package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.entity.Player;

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
    // <n@npc.navigator.is_navigating>
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation indefinitely.
    //
    // @Usage
    // - pause waypoints
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
    // <n@npc.navigator.is_navigating>
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation indefinitely.
    //
    // @Usage
    // - pause waypoints
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

    int duration;
    PauseType pauseType;
    dNPC dNPC;
    Player player;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Set defaults with information from the ScriptEntry
        duration = -1;
        pauseType = null;
        dNPC = null;
        player = null;
        if (Utilities.getEntryNPC(scriptEntry) != null) {
            dNPC = Utilities.getEntryNPC(scriptEntry);
        }
        if (Utilities.getEntryPlayer(scriptEntry) != null) {
            player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
        }

        // Parse arguments
        // TODO: UPDATE COMMAND PARSING
        for (String arg : scriptEntry.getArguments()) {

            if (ArgumentHelper.matchesDuration(arg)) {
                duration = ArgumentHelper.getIntegerFrom(arg);

            }
            else if (ArgumentHelper.matchesArg("WAYPOINTS", arg) || ArgumentHelper.matchesArg("NAVIGATION", arg)
                    || ArgumentHelper.matchesArg("ACTIVITY", arg) || ArgumentHelper.matchesArg("WAYPOINTS", arg)) {
                // Could also maybe do for( ... : PauseType.values()) ... not sure which is faster.
                pauseType = PauseType.valueOf(arg.toUpperCase());

            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "Unknown argument '" + arg + "'");
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        pause(dNPC, pauseType, !scriptEntry.getCommandName().equalsIgnoreCase("RESUME"));

        // If duration...
        if (duration > 0) {
            if (durations.containsKey(dNPC.getCitizen().getId() + pauseType.name())) {
                try {
                    DenizenAPI.getCurrentInstance().getServer().getScheduler().cancelTask(durations.get(dNPC.getCitizen().getId() + pauseType.name()));
                }
                catch (Exception e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "There was an error pausing that!");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                }

            }
            dB.echoDebug(scriptEntry, "Running delayed task: Unpause " + pauseType.toString());

            final ScriptEntry se = scriptEntry;
            durations.put(dNPC.getId() + pauseType.name(), DenizenAPI.getCurrentInstance()
                    .getServer().getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                            new Runnable() {
                                @Override
                                public void run() {
                                    dB.echoDebug(se, "Running delayed task: Pausing " + pauseType.toString());
                                    pause(dNPC, pauseType, false);

                                }
                            }, duration * 20));
        }
    }

    public void pause(dNPC denizen, PauseType pauseType, boolean pause) {
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

package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PauseCommand extends AbstractCommand {

    private Map<String, Integer> durations = new ConcurrentHashMap<String, Integer>(8, 0.9f, 1);

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
        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() != null)
            dNPC = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC();
        if (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() != null)
            player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity();

        // Parse arguments
        // TODO: UPDATE COMMAND PARSING
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesDuration(arg)) {
                duration = aH.getIntegerFrom(arg);

            }
            else if (aH.matchesArg("WAYPOINTS", arg) || aH.matchesArg("NAVIGATION", arg)
                    || aH.matchesArg("ACTIVITY", arg) || aH.matchesArg("WAYPOINTS", arg)) {
                // Could also maybe do for( ... : PauseType.values()) ... not sure which is faster.
                pauseType = PauseType.valueOf(arg.toUpperCase());

            }
            else
                dB.echoError(scriptEntry.getResidingQueue(), "Unknown argument '" + arg + "'");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

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
                if (pause) denizen.getNavigator().cancelNavigation();
                return;

            case ACTIVITY:
                denizen.getCitizen().getDefaultGoalController().setPaused(pause);
                return;

            case NAVIGATION:
                // TODO: Finish this
        }

    }
}

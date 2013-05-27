package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pauses/Resumes a NPC's various parts.
 * 
 * @author Jeremy Schroeder
 */

public class PauseCommand extends AbstractCommand {

	/* PAUSE (DURATION:#) (NPCID:#) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * 
	 */

	private Map<String, Integer> durations = new ConcurrentHashMap<String, Integer>(8, 0.9f, 1);
    enum PauseType { ACTIVITY, WAYPOINTS, NAVIGATION }
    
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
	    if (scriptEntry.getNPC() != null) dNPC = scriptEntry.getNPC();
		if (scriptEntry.getPlayer() != null) player = scriptEntry.getPlayer();

		// Parse arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesDuration(arg)) {
				duration = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);

            }	else if (aH.matchesArg("WAYPOINTS", arg) || aH.matchesArg("NAVIGATION", arg)
					|| aH.matchesArg("ACTIVITY", arg) || aH.matchesArg("WAYPOINTS", arg)) {
			    // Could also maybe do for( ... : PauseType.values()) ... not sure which is faster. 
				pauseType = PauseType.valueOf(arg.toUpperCase());
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);

            }	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}	
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		pause(dNPC, pauseType, !scriptEntry.getCommandName().equalsIgnoreCase("RESUME"));

		// If duration...
		if (duration > 0) {
			if (durations.containsKey(dNPC.getCitizen().getId() + pauseType.name())) {
				try { denizen.getServer().getScheduler().cancelTask(durations.get(dNPC.getCitizen().getId() + pauseType.name())); }
				catch (Exception e) { dB.echoError(Messages.ERROR_CANCELLING_DELAYED_TASK); }
			
			}	dB.echoDebug(Messages.DEBUG_SETTING_DELAYED_TASK, "UNPAUSE " + pauseType);

			durations.put(dNPC.getId() + pauseType.name(), denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen,
					new Runnable() {
				@Override public void run() {
					dB.echoDebug(Messages.DEBUG_RUNNING_DELAYED_TASK, "UNPAUSING " + pauseType);
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

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }

}
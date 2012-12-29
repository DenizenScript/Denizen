package net.aufdemrand.denizen.scripts.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable2;
import net.citizensnpcs.trait.waypoint.Waypoints;

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

	private Map<String, Integer> durations = new ConcurrentHashMap<String, Integer>();
    enum PauseType { ACTIVITY, WAYPOINTS, NAVIGATION }
    
	int duration;
	PauseType pauseType;
	DenizenNPC denizenNPC;
	Player player;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Set defaults with information from the ScriptEntry
	    duration = -1;
	    pauseType = null;
	    denizenNPC = null;
	    player = null;
	    if (scriptEntry.getNPC() != null) denizenNPC = scriptEntry.getNPC();
		if (scriptEntry.getPlayer() != null) player = scriptEntry.getPlayer();

		// Parse arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesDuration(arg)) {
				duration = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);
				continue;

			}	else if (aH.matchesArg("WAYPOINTS", arg) || aH.matchesArg("NAVIGATION", arg)
					|| aH.matchesArg("ACTIVITY", arg) || aH.matchesArg("WAYPOINTS", arg)) {
			    // Could also maybe do for( ... : PauseType.values()) ... not sure which is faster. 
				pauseType = PauseType.valueOf(arg.toUpperCase());
				dB.echoDebug(Messages.DEBUG_SET_TYPE, arg);
				continue;

			}	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}	
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		pause(denizenNPC, pauseType, scriptEntry.getCommand().equalsIgnoreCase("RESUME") ? false : true);

		// If duration...
		if (duration > 0) {
			if (durations.containsKey(denizenNPC.getCitizen().getId() + pauseType.name())) {
				try { denizen.getServer().getScheduler().cancelTask(durations.get(denizenNPC.getCitizen().getId() + pauseType.name())); }
				catch (Exception e) { dB.echoError(Messages.ERROR_CANCELLING_DELAYED_TASK); }
			
			}	dB.echoDebug(Messages.DEBUG_SETTING_DELAYED_TASK, "UNPAUSE " + pauseType);

			durations.put(denizenNPC.getId() + pauseType.name(), denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen, 
					new Runnable2<DenizenNPC, PauseType>(denizenNPC, pauseType) {
				@Override public void run(DenizenNPC npc, PauseType type) { 
					
					dB.echoDebug(Messages.DEBUG_RUNNING_DELAYED_TASK, "UNPAUSING " + pauseType);
					pause(npc, type, false);

				}
			}, duration * 20));
		}
	}

	public void pause(DenizenNPC denizen, PauseType pauseType, boolean pause) {
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
			return;
		}

	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }

}
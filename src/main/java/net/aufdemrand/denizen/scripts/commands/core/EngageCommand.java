package net.aufdemrand.denizen.scripts.commands.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.citizensnpcs.api.npc.NPC;

/**
 * Sets an NPC to ENGAGED in the Denizen Engage List. 
 * When ENGAGEd, a Denizen will not interact with a Player until DISENGAGEd (or timed out).
 * 
 * @author Jeremy Schroeder
 */

public class EngageCommand extends AbstractCommand {

	/* ENGAGE (# of Seconds) (NPCID:#)*/

	/* Arguments: [] - Required, () - Optional 
	 * (DURATION:#) Will automatically DISENGAGE after specified amount of seconds.
	 * 		If not set, the Denizen will remain ENGAGEd until a DISENGAGE command is
	 *   	used, or the Denizen config.yml engage_timeout_in_seconds setting. 
	 * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
	 */

	int duration;
	NPC npc;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

	    duration = 1;
	    if (scriptEntry.getDenizen() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);
        
		// Set some defaults based on the scriptEntry
		npc = scriptEntry.getDenizen().getCitizen();

		// Parse arguments
		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesInteger(arg) || aH.matchesDuration(arg)) {
				duration = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);
				continue;
				
			} else if (aH.matchesArg("NOW", arg)) {
			    continue;
			
			}	else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT);
		}	

	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {

		if (duration > 0) setEngaged(npc, duration);
		else setEngaged(npc, true);
	}

	
	/* 
	 * Engaged NPCs cannot interact with Players 
	 */
	private Map<NPC, Long> currentlyEngaged = new HashMap<NPC, Long>();

	/**
	 * Checks if the DenizenNPC is ENGAGED. Engaged NPCs do not respond to
	 * Player interaction.
	 * 
	 * @param denizenNPC
	 * 		the Denizen NPC being checked 
	 * @return
	 *  	if the DenizenNPC is currently engaged
	 */
	public boolean getEngaged(NPC npc) {
		if (currentlyEngaged.containsKey(npc)) 
			if (currentlyEngaged.get(npc) > System.currentTimeMillis())
				return true;
		return false;
	}

	/**
	 * Sets a DenizenNPC's ENGAGED status. Engaged NPCs do not respond to Player
	 * interaction. Note: Denizen NPC will automatically disengage after the
	 * engage_timeout_in_seconds which is set in the Denizen config.yml.
	 * 
	 * @param denizenNPC
	 * 		the DenizenNPC affected
	 * @param engaged
	 * 		true sets the DenizenNPC engaged, false sets the DenizenNPC as disengaged
	 */
	public void setEngaged(NPC npc, boolean engaged) {
		if (engaged) currentlyEngaged.put(npc, System.currentTimeMillis() 
				+ Denizen.settings.EngageTimeoutInSeconds() * 1000 );
		if (!engaged) currentlyEngaged.remove(npc);
	}

	/**
	 * Sets a DenizenNPC as ENGAGED for a specific amount of seconds. Engaged NPCs do not
	 * respond to Player interaction. If the NPC is previously engaged, using this will
	 * over-ride the previously set duration.
	 * 
	 * @param denizenNPC
	 * 		the DenizenNPC to set as engaged
	 * @param duration
	 * 		the number of seconds to engage the DenizenNPC
	 */
	public void setEngaged(NPC npc, Integer duration) {
		currentlyEngaged.put(npc, System.currentTimeMillis() + duration * 1000 );
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }
 }
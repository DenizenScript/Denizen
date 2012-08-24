package net.aufdemrand.denizen.commands.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets/unsets the Denizen's built in Engage List. 
 * When ENGAGEd, a Denizen will not interact with a Player until DISENGAGEd (or timed out).
 * 
 * @author Jeremy Schroeder
 */

public class EngageCommand extends AbstractCommand {

	/* ENGAGE (# of Seconds) */

	/* Arguments: [] - Required, () - Optional 
	 * (# of Seconds) Will automatically DISENGAGE after specified amount of seconds.
	 *   If not set, the Denizen will remain ENGAGEd until a DISENGAGE command is used.
	 *   
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
	 */

	/* DISENGAGE */

	/* Arguments: [] - Required, () - Optional
	 * None.
	 * 
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		Integer timedEngage = null;
		DenizenNPC theDenizen = theEntry.getDenizen();

		/* Get arguments */
		if (theEntry.arguments() != null) {
			for (String thisArg : theEntry.arguments()) {

				// If argument is a duration
				if (aH.matchesInteger(thisArg)) {
					timedEngage = Integer.valueOf(thisArg);
					aH.echoDebug("...engage duration set to '%s'.", thisArg);
				}

				// If argument is a NPCID: modifier
				else if (aH.matchesNPCID(thisArg)) {
					theDenizen = aH.getNPCIDModifier(thisArg);
					if (theDenizen != null)
						aH.echoDebug("...now referencing '%s'.", thisArg);
				}

				// Can't match to anything
				else aH.echoError("...unable to match '%s'!", thisArg);
			}	
		}
		
		// Catch TASK-type script usage.
		if (theDenizen == null) {
			aH.echoError("Seems this was sent from a TASK-type script. Must use NPCID:# to specify a Denizen NPC!");
			return false;
		}

		/* If a DISENGAGE, take the Denizen out of the engagedList. */
		if (theEntry.getCommand().equalsIgnoreCase("DISENGAGE")) {
			setEngaged(theEntry.getDenizen(), false);
			return true;
		}

		/* ENGAGE the Denizen. */
		if (timedEngage != null) 
			setEngaged(theDenizen, timedEngage);
		else 			
			setEngaged(theEntry.getDenizen(), true);
		return true;
	}


	/* Engaged NPCs cannot interact with Players */

	private Map<DenizenNPC, Long> engagedNPC = new HashMap<DenizenNPC, Long>();

	public boolean getEngaged(DenizenNPC theDenizen) {
		if (engagedNPC.containsKey(theDenizen)) 
			if (engagedNPC.get(theDenizen) > System.currentTimeMillis())
				return true;
		return false;
	}

	public void setEngaged(DenizenNPC theDenizen, boolean engaged) {
		if (engaged) engagedNPC.put(theDenizen, System.currentTimeMillis() 
				+ plugin.settings.EngageTimeoutInSeconds() * 1000 );
		if (!engaged) engagedNPC.remove(theDenizen);
	}

	public void setEngaged(DenizenNPC theDenizen, Integer duration) {
		engagedNPC.put(theDenizen, System.currentTimeMillis() + duration * 1000 );
	}


}
package net.aufdemrand.denizen.commands.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Sets/unsets the Denizen's built in Engage List. 
 * When Engaged, a Denizen will not interact with a Player until DISENGAGED (or a timeout).
 * 
 * @author Jeremy Schroeder
 *
 */

public class EngageCommand extends Command {

	/* ENGAGE (# of Seconds) */

	/* Arguments: [] - Required, () - Optional 
	 * (# of Seconds) Will automatically DISENGAGE after specified amount of seconds.
	 *   If not set, the Denizen will remain ENGAGED until a DISENGAGE command is used.
	 *   
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen to ENGAGE or DISENGAGE to the Citizens2 NPCID
	 */

	/* DISENGAGE */

	/* Modifiers:
	 * (NPCID:#) Changes the Denizen to ENGAGE or DISENGAGE to the Citizens2 NPCID
	 */

	@Override
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		/* Initialize variables */ 

		Integer timedEngage = null;
		NPC theDenizen = theCommand.getDenizen();

		/* Get arguments */
		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
					timedEngage = Integer.valueOf(thisArgument);

				if (thisArgument.toUpperCase().contains("NPCID:"))
					try {
						if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])) != null)
							theDenizen = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1]));	
					} catch (Throwable e) {
						throw new CommandException("NPCID specified could not be matched to a Denizen.");
					}
			}	
		}

		/* If a DISENGAGE, take the Denizen out of the engagedList. */
		if (theCommand.getCommand().equalsIgnoreCase("DISENGAGE")) {
			setEngaged(theCommand.getDenizen(), false);
			return true;
		}

		/* ENGAGE the Denizen. */
		if (timedEngage != null) 
			setEngaged(theDenizen, timedEngage);
		else 			
			setEngaged(theCommand.getDenizen(), true);

		return true;
	}


	/* Engaged NPCs cannot interact with Players */

	private Map<NPC, Long> engagedNPC = new HashMap<NPC, Long>();

	public boolean getEngaged(NPC theDenizen) {
		if (engagedNPC.containsKey(theDenizen)) 
			if (engagedNPC.get(theDenizen) > System.currentTimeMillis())
				return true;
		return false;
	}

	public void setEngaged(NPC theDenizen, boolean engaged) {
		if (engaged) engagedNPC.put(theDenizen, System.currentTimeMillis() 
				+ plugin.settings.EngageTimeoutInSeconds() * 1000 );
		if (!engaged) engagedNPC.remove(theDenizen);
	}

	public void setEngaged(NPC theDenizen, Integer duration) {
		engagedNPC.put(theDenizen, System.currentTimeMillis() + duration * 1000 );

	}


}
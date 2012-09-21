package net.aufdemrand.denizen.commands.core;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Handles Denizens talking via script entries.
 * 
 * @author Jeremy Schroeder
 */

public class TalkCommand extends AbstractCommand {

	/* CHAT|SHOUT|WHISPER|EMOTE|ANNOUNCE|NARRATE "[TEXT]" */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [TYPICAL] argument with a description if necessary.
	 * (ARGUMENTS) should be clear and concise.
	 *   
	 * Modifiers:
	 * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
	 * (NOPLAYER) Makes the chat by the Denizen not directed to the Player.
	 *   Note: Only applies to Chat, Shout and Whisper.
	 * 
	 * Example Usage:
	 * CHAT "That's a nice Helmet you have!"
	 * SHOUT "HELLO WORLD!"
	 * WHISPER 'NPCID:3' "Shhh!"
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		DenizenNPC theDenizen = null;
		String theMessage = null;
		Boolean noPlayer = false;
		Player thePlayer = null;

		/* Match arguments to expected variables */

		if (theEntry.arguments() == null)
			throw new CommandException("No talk text!");


		if (theEntry.arguments().length > 3) {
			aH.echoError("Woah! Lots of arguments detected in " + theEntry.getCommand() + ".  Perhaps you are failing to enclose your Talk Text in quotes?  Example usage: CHAT 'Hello world!'");
		} else {

			if (theEntry.getPlayer() == null) noPlayer = true;
			
			for (String thisArg : theEntry.arguments()) {
				
				// Fill replaceables
				if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
				
				if (thisArg.toUpperCase().equalsIgnoreCase("NOPLAYER")) {
					aH.echoError("...will not target Player.");
					noPlayer = true;
				}

				else if (thisArg.toUpperCase().equalsIgnoreCase("ASPLAYER")) {
					aH.echoError("...will make the Player talk.");
					noPlayer = true;
				}
				
				else if (aH.matchesNPCID(thisArg)) {
					theDenizen = aH.getNPCIDModifier(thisArg);
					if (theDenizen != null)
						aH.echoDebug("...specified '%s'.", thisArg);
				} 

				else {
					aH.echoDebug("...text: '%s'", thisArg);
					theMessage = thisArg;
				}
			}

		}	
		
		if (theEntry.getTexts()[0] != null) {
			theMessage = theMessage.replace("<*>", theEntry.getTexts()[0]);
		}

		// Set to Denizen carried over by the ScriptEntry, if possible.
		if (theDenizen == null && theEntry.getDenizen() != null) theDenizen = theEntry.getDenizen();
		if (!noPlayer) thePlayer = theEntry.getPlayer();
		
		// Catch TASK script trying to use CHAT/etc. without setting NPCID.
		// Exception is using NARRATE, which does NOT need an NPC.
		if (theDenizen == null && !theEntry.getCommand().equals("NARRATE")) {
				aH.echoError("Seems this was sent from a TASK-type script. Must specify NPCID:#!");
				return false;
		}
		
		if (theEntry.getCommand().equals("NARRATE") && theEntry.sendingQueue() == QueueType.ACTIVITY) {
			aH.echoError("NARRATE is not applicable to an ACTIVITY Task.");
			return false;
		}

		/* Execute the command, if all required variables are filled. */
		if (theMessage != null && theDenizen != null) {
			theDenizen.talk(TalkType.valueOf(theEntry.getCommand()), thePlayer, theMessage);
			return true;
		}
		
		else if (theMessage != null && theEntry.getCommand().equals("NARRATE")) {
			plugin.getSpeechEngine().talk(null, thePlayer, theMessage, TalkType.NARRATE);
			return true;
		}
		
		return false;
	}

}
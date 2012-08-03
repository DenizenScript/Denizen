package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.bookmarks.BookmarkHelper.BookmarkType;
import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
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

		if (theEntry.arguments() != null) {

			if (theEntry.arguments().length > 5) {
				if (plugin.debugMode) plugin.getLogger().info("Woah! Lots of arguments detected in " + theEntry.getCommand() + ".  Perhaps you are failing to enclose your Talk Text in quotes?  Example usage: CHAT 'Hello world!'");
			} else {
				for (String thisArgument : theEntry.arguments()) {

					// Do this routine for each argument supplied.

					if (plugin.debugMode) plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

					if (thisArgument.toUpperCase().equalsIgnoreCase("NOPLAYER")) {
						if (plugin.debugMode) plugin.getLogger().info("...will not target Player.");
						noPlayer = true;
					}
					
					if (thisArgument.toUpperCase().contains("NPCID:")) {
						if (plugin.debugMode) plugin.getLogger().info("...matched to NPCID.");
						try {
							if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])) != null)
								theDenizen = plugin.getDenizenNPCRegistry().getDenizen(CitizensAPI.getNPCRegistry().getById(Integer.valueOf(thisArgument.split(":")[1])));	
						} catch (Throwable e) {
							throw new CommandException("NPCID specified could not be matched to a Citizen NPC.");
						}
					} else {
						if (plugin.debugMode) plugin.getLogger().info("...using argument as TEXT.");
						theMessage = thisArgument;
					}
				}
			}	
		}

		if (theDenizen == null) theDenizen = theEntry.getDenizen();
		if (!noPlayer) thePlayer = theEntry.getPlayer();
		
		/* Execute the command, if all required variables are filled. */
		if (theMessage != null) {
				theDenizen.talk(TalkType.valueOf(theEntry.getCommand()), thePlayer, theMessage);
				return true;
		}

		// else...

		/* Error processing */

		// Processing has gotten to here, there's probably not been enough arguments. 
		// Let's alert the console.
		if (plugin.debugMode) if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage: SAMPLECOMMAND [TYPICAL] (ARGUMENTS)");

		return false;
	}

	// You can include more methods in this class if necessary. Or not. :)

}
package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.npc.NPC;

/**
 * Uses the Citizens SpeechController to 'chat', the default VocalChord
 * of an NPC.
 * 
 * @author Jeremy Schroeder
 * Version 1.0 Last Updated 11/29 12:21
 */

public class ChatCommand extends AbstractCommand {

	@Override
	public void onEnable() {
		// Nothing to do here.
	}

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * ['Text to chat'] sets the text.
	 * (TARGETS:#|player_name) sets the direct recipients, or targets, of the chat.
	 * 		Can be inn list format -- if more than one recipient, use | to separate
	 * 		targets. Can be either an NPCID or valid Player name.
	 * (TALKER:#|player_name) sets the entity doing the talking. Can be either an
	 * 		NPCID or player_name. 
	 * 
	 * Note: NPCID:# argument can be used to set an NPC TALKER as well.
	 * Note: Talking via a Player will require Converse
	 * 
	 * Example Usage:
	 * CHAT 'Hello there, <PLAYER.NAME>!'
	 * 
	 */

	SpeechContext context = null;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		context = new SpeechContext("");    	
		if (scriptEntry.getNPC() != null)
			context.setTalker(scriptEntry.getNPC().getEntity());

		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesValueArg("TARGET, TARGETS", arg, ArgumentType.Custom)) {
				for (String target : aH.getListFrom(arg)) {
					if (target.matches("\\d+")) {
						NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(target)); 
						if ( npc != null) {
							context.addRecipient(npc.getBukkitEntity());
							continue;
						}
					} else {
						Player player = Bukkit.getPlayer(target);
						if (player != null) {
							context.addRecipient(player);
							continue;
						}
					}

					dB.echoError("Invalid TARGET '%s'!", target);
				}
				dB.echoDebug("Set TARGET(s).");

			} else if (aH.matchesValueArg("TALKER", arg, ArgumentType.Custom)) {
				String talker = aH.getStringFrom(arg);
				if (talker.matches("\\d+")) {
					NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(talker)); 
					if ( npc != null) {
						dB.echoDebug("...set TALKER: '%s'", talker);
						context.setTalker(npc.getBukkitEntity());
						continue;
					}
				} 
				
			 // else { 
			 // TODO: add hooking into Converse to handle player talking
			 // }
			
				dB.echoError("Invalid TALKER! Perhaps the NPC doesn't exist?");

			} else {
				context.setMessage(arg);
				dB.echoDebug(Messages.DEBUG_SET_TEXT, arg);
			}
		}

		if (context.getTalker() == null) throw new InvalidArgumentsException("Must specify a valid TalkableEntity for TALKER!");

	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		if (CitizensAPI.getNPCRegistry().isNPC(context.getTalker().getEntity()))
			CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity())
				.getDefaultSpeechController().speak(context, "chat");
		
		// else
		// Chat via Player with Converse
	
	}

}
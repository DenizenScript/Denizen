package net.aufdemrand.denizen.scripts.commands.core;

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

/**
 * <p>Uses the Citizens SpeechController to 'chat', the default VocalChord for
 * of an NPC. Chat prefixes and setup is found in Citizen's config.yml file.</p>
 * 
 * <b>dScript Usage:</b><br>
 * <pre>CHAT  ['message to chat.'] (TARGET(S):list_of_LivingEntities) (TALKER:NPC.#)</pre>
 * 
 * <ol><tt>Arguments: [] - Required</ol></tt>
 * 
 * <ol><tt>['message to chat']</tt><br> 
 *         The chat message the Talker will use. This will be seen by all entities within range.</ol>
 * 
 * <ol><tt>(TARGET(S):NONE|List of LivingEntities{Interact Player})</tt><br> 
 *         The LivingEntities that the message is addressed to. Uses the dScript List format
 *         (item1|item2|etc). Valid entities are: PLAYER.player_name, NPC.npcid, or ENTITY.entity_name.
 *         If NONE is specified, the NPC speaking will have no target. Default target is set to 
 *         the Player doing the interaction (if that information is available to the command).</ol>
 *
 * <ol><tt>(TALKER:NPC.npcid{Interact NPC})</tt><br> 
 *         The NPC that will be doing the chatting. Defaults to the NPC interacted with (if that information is
 *         available to the command), but can be changed by using the NPC LivingEntity format (NPC.npcid).</ol>
 *
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - CHAT 'Be careful out there! The road is long and dark.' <br>
 *  - CHAT TARGET:NONE 'Beer here! Beer for sale! ...anybody need a beer?' <br>
 *  - CHAT TARGETS:PLAYER.aufdemrand|PLAYER.Jeebiss|PLAYER.DrBix 'Ah, a group of adventurers! Great!'
 *  - CHAT TALKER:NPC.13 TARGET:NPC.<NPC.ID> 'Shut up, old man!'
 * </ol></tt>
 * 
 * @author Jeremy Schroeder
 * 
 */
public class ChatCommand extends AbstractCommand {

	// TODO: Make this class abstract to minimize code duplication for Whisper/Shout/etc.
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		SpeechContext context = new SpeechContext("");    	
		boolean noTargets = false;
		
		if (scriptEntry.getNPC() != null)
			context.setTalker(scriptEntry.getNPC().getEntity());

		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesValueArg("TARGET, TARGETS", arg, ArgumentType.Custom)) {
				if (arg.equalsIgnoreCase("none")) {
					dB.echoDebug("Removed TARGET(s).");
					noTargets = true;
				}
				for (String target : aH.getListFrom(arg)) {
					if (aH.getLivingEntityFrom(target) != null) {
						context.addRecipient(aH.getLivingEntityFrom(target));
                    } else
						dB.echoError("Invalid TARGET: '%s'", target);
				}
				dB.echoDebug("Set TARGET(s).");

			} else if (aH.matchesValueArg("TALKER", arg, ArgumentType.LivingEntity)) {
				String talker = aH.getStringFrom(arg);
				if (talker.startsWith("NPC.") && aH.getLivingEntityFrom(talker) != null) {
						context.setTalker(aH.getLivingEntityFrom(talker));
                } else
				//
				// TODO: add hooking into Converse to handle player talking
				// 
				dB.echoError("Invalid TALKER! Perhaps the NPC doesn't exist?");

			} else {
				context.setMessage(arg);
				dB.echoDebug(Messages.DEBUG_SET_TEXT, arg);
			}
		}

		// Add default recipient as the scriptEntry Player if no recipients set otherwise
		if (!context.hasRecipients() && !noTargets && scriptEntry.getPlayer() != null)
			context.addRecipient(scriptEntry.getPlayer());
		
		// Verify essential fields are set
		if (context.getTalker() == null) 
			throw new InvalidArgumentsException("Must specify a valid TALKER.");
		if (context.getMessage().length() < 1) 
			throw new InvalidArgumentsException("Must specify a message.");

		// Add context to the ScriptEntry to pass along to execute().
		scriptEntry.addObject("context", context);

	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		SpeechContext context = (SpeechContext) scriptEntry.getObject("context");

		// If the talker is an NPC, use the NPC object to speak
		if (CitizensAPI.getNPCRegistry().isNPC(context.getTalker().getEntity()))
			CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity())
			.getDefaultSpeechController().speak(context, "chat");

		// else
		// TODO: Chat via Player with Converse

	}

}
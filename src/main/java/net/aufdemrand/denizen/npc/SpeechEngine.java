package net.aufdemrand.denizen.npc;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.SpeechEngine.Reason;
import net.aufdemrand.denizen.npc.SpeechEngine.TalkType;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpeechEngine {

	Denizen plugin;

	public SpeechEngine(Denizen plugin) {
		this.plugin = plugin;
	}

	public enum Reason {
		DenizenIsUnavailable, NoMatchingChatTriggers, NoMatchingClickTrigger, NoMatchingDamageTrigger	}

	public enum TalkType {
		Chat, Emote, Shout, Whisper, Narrate
	}

	/* TODO: MAJOR FRIKKIN CLEANUP!  */

	public void talk(DenizenNPC theDenizen, Player thePlayer, String theText, TalkType talkType) {

		String[] formattedText = formatChatText(theText, talkType, thePlayer, theDenizen);

		List<String> playerText = getMultilineText(formattedText[0]);
		List<String> bystanderText = getMultilineText(formattedText[1]);

		/* Spew the text to the world. */

		if (!playerText.isEmpty()) {
			for (String text : playerText) { 
				talkToPlayer(theDenizen, thePlayer, text, null, talkType);
			}
		}

		if (!bystanderText.isEmpty()) {
			for (String text : bystanderText) { /* now bystanderText */
				if (!playerText.isEmpty()) talkToPlayer(theDenizen, thePlayer, "shhh...don't speak!", text, talkType);
				else talkToPlayer(theDenizen, thePlayer, null, text, talkType);
			}
		}

	}


	public void talk(DenizenNPC theDenizen, Player thePlayer, Reason theReason,
			TalkType talkType) {

		String textToSend = null;
		
		switch (theReason) {

		case DenizenIsUnavailable:
			textToSend = plugin.getAssignments().getString("Denizen." + theDenizen.getName() + ".Texts.Denizen Unavailable");
			if (textToSend != null)	talk(theDenizen, thePlayer, textToSend, TalkType.Chat);
			else talk(theDenizen, thePlayer, plugin.settings.DefaultDenizenUnavailableText(), TalkType.Chat);
			break;

		case NoMatchingChatTriggers:
			textToSend = plugin.getAssignments().getString("Denizen." + theDenizen.getName() + ".Texts.No Chat Trigger");
			if (textToSend != null)	talk(theDenizen, thePlayer, textToSend, TalkType.Chat);
			else talk(theDenizen, thePlayer, plugin.settings.DefaultNoChatTriggerText(), TalkType.Chat);
			break;

		case NoMatchingClickTrigger:
			textToSend = plugin.getAssignments().getString("Denizen." + theDenizen.getName() + ".Texts.No Click Trigger");
			if (textToSend != null)	talk(theDenizen, thePlayer, textToSend, TalkType.Chat);
			else talk(theDenizen, thePlayer, plugin.settings.DefaultNoClickTriggerText(), TalkType.Chat);
			break;

		case NoMatchingDamageTrigger:
			textToSend = plugin.getAssignments().getString("Denizen." + theDenizen.getName() + ".Texts.No Damage Trigger");
			if (textToSend != null)	talk(theDenizen, thePlayer, textToSend, TalkType.Chat);
			else talk(theDenizen, thePlayer, plugin.settings.DefaultNoDamageTriggerText(), TalkType.Chat);
			break;

		}
	

	}


	/* 
	 * Takes chat/whisper/etc. text and formats it based on the config settings. 
	 * Returns a String[]. 
	 * 
	 * Element [0] contains formatted text for the player interacting.
	 * Element [1] contains formatted text for bystanders.
	 * 
	 * Either can be null if only one type of text is required.
	 */

	public String[] formatChatText (String theMessage, TalkType talkType, Player thePlayer, DenizenNPC theDenizen) {

		String playerMessageFormat = null;
		String bystanderMessageFormat = null;

		boolean toPlayer = true;
		if (thePlayer == null) toPlayer = false;
		String denizenName = ""; 
		if (theDenizen != null) denizenName = theDenizen.getName();

		switch (talkType) {

		case Shout:	
			playerMessageFormat = plugin.settings.NpcShoutToPlayer();
			bystanderMessageFormat = plugin.settings.NpcShoutToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcShoutToBystanders();
			break;

		case Whisper:
			playerMessageFormat = plugin.settings.NpcWhisperToPlayer();
			bystanderMessageFormat = plugin.settings.NpcWhisperToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcWhisperToBystanders();
			break;

		case Emote:
			toPlayer = false;
			bystanderMessageFormat = "<NPC> <TEXT>";
			break;

		case Narrate:
			playerMessageFormat = "<TEXT>";
			break;

		case Chat:
			playerMessageFormat = plugin.settings.NpcChatToPlayer();
			bystanderMessageFormat = plugin.settings.NpcChatToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcChatToBystanders();
			break;
		}

		if (playerMessageFormat != null)
			playerMessageFormat = playerMessageFormat
			.replace("<NPC>", denizenName)
			.replace("<TEXT>", theMessage)
			.replace("<PLAYER>", thePlayer.getName())
			.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
			.replace("<WORLD>", thePlayer.getWorld().getName())
			.replace("<HEALTH>", String.valueOf(thePlayer.getHealth()))
			.replace("%%", "\u00a7");

		if (bystanderMessageFormat != null)
			bystanderMessageFormat = bystanderMessageFormat
			.replace("<NPC>", denizenName)
			.replace("<TEXT>", theMessage)
			.replace("<PLAYER>", thePlayer.getName())
			.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
			.replace("<WORLD>", thePlayer.getWorld().getName())
			.replace("<HEALTH>", String.valueOf(thePlayer.getHealth()))
			.replace("%%", "\u00a7");

		String[] returnedText = {playerMessageFormat, bystanderMessageFormat};

		return returnedText;
	}



	/* 
	 * Takes long text and splits it into multiple string elements in a list
	 * based on the config setting for MaximumLength, default 55.
	 * 
	 * Text should probably be formatted first with formatChatText, since 
	 * formatting usually adds some length to the beginning and end of the text.
	 */

	public List<String> getMultilineText (String theText) {

		List<String> processedText = new ArrayList<String>();
		if (theText == null) return processedText;
		String[] text = theText.split(" ");

		/* Chop up text into multiple lines with a max length defines in config.yml */
		if (theText.length() > plugin.settings.MultiLineTextMaximumLength()) {
			processedText.add(0, "");
			int word = 0; int line = 0;

			while (word < text.length) {
				if (processedText.get(line).length() + text[word].length() < plugin.settings.MultiLineTextMaximumLength()) {
					processedText.set(line, processedText.get(line) + text[word] + " ");
					word++;
				} else { 
					line++; processedText.add("" + ChatColor.getLastColors(processedText.get(line - 1)));
				}
			}
		}

		else processedText.add(0, theText);

		return processedText;
	}



	/**
	 * Makes a Denizen talk to a Player.
	 *
	 * @param  theDenizen  the Citizens2 NPC object that is doing the talking.
	 * @param  thePlayer  the Bukkit Player object to talk to.
	 * @param  commandArgs 
	 */

	public void talkToPlayer(DenizenNPC theDenizen, Player thePlayer, String thePlayerMessage, String theBystanderMessage, TalkType talkType) {

		int theRange = 0;

		switch(talkType) {

		case Shout:
			theRange = plugin.settings.NpcToPlayerShoutRangeInBlocks();
			break;

		case Whisper:
			theRange = plugin.settings.NpcToPlayerWhisperRangeInBlocks();
			break;

		case Emote:
			theRange = plugin.settings.NpcEmoteRangeInBlocks();
			thePlayer.sendMessage(theBystanderMessage);
			break;

		default:
			theRange = plugin.settings.NpcToPlayerChatRangeInBlocks();
			break;

		}

		if (thePlayerMessage != null && !thePlayerMessage.equals("shhh...don't speak!")) thePlayer.sendMessage(thePlayerMessage);

		if ((plugin.settings.BystandersHearNpcToPlayerChat() || thePlayerMessage == null)  && theBystanderMessage != null) {
			if (theRange > 0) {
				for (Player otherPlayer : plugin.getDenizenNPCRegistry().getInRange(theDenizen.getEntity(), theRange, thePlayer)) {
					otherPlayer.sendMessage(theBystanderMessage);
				}
			}
		}

		return;
	}



	/**
	 * Talks to a NPC. Also has replaceable data, end-user, when using <NPC> <TEXT> <PLAYER> <FULLPLAYERNAME> <WORLD> or <HEALTH>.
	 *
	 * @param  theDenizen  the Citizens2 NPC object to talk to.
	 * @param  thePlayer  the Bukkit Player object that is doing the talking.
	 */

	public void talkToDenizen(DenizenNPC theDenizen, Player thePlayer, String theMessage) {

		thePlayer.sendMessage(plugin.settings.PlayerChatToNpc()
				.replace("<NPC>", theDenizen.getName())
				.replace("<TEXT>", theMessage)
				.replace("<PLAYER>", thePlayer.getName())
				.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
				.replace("<WORLD>", thePlayer.getWorld().getName())
				.replace("<HEALTH>", String.valueOf(thePlayer.getHealth())));

		if (plugin.settings.BystandersHearNpcToPlayerChat()) {
			int theRange = plugin.settings.PlayerToNpcChatRangeInBlocks();
			if (theRange > 0) {
				for (Player otherPlayer : plugin.getDenizenNPCRegistry().getInRange(theDenizen.getEntity(), theRange, thePlayer)) {
					otherPlayer.sendMessage(plugin.settings.PlayerChatToNpcBystander()
							.replace("<NPC>", theDenizen.getName())
							.replace("<TEXT>", theMessage)
							.replace("<PLAYER>", thePlayer.getName())
							.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
							.replace("<WORLD>", thePlayer.getWorld().getName())
							.replace("<HEALTH>", String.valueOf(thePlayer.getHealth())));
				}
			}
		}

		return;
	}






}

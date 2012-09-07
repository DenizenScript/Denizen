package net.aufdemrand.denizen.npc;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.ArgumentHelper;

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
		CHAT, EMOTE, SHOUT, WHISPER, NARRATE, CHAT_PLAYERONLY
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
			textToSend = plugin.getAssignments().getString("Denizens." + theDenizen.getName() + ".Texts.Denizen Unavailable");
			if (textToSend != null)	{
				if (textToSend.equals("")) return;
				talk(theDenizen, thePlayer, textToSend, talkType);
			}
			else talk(theDenizen, thePlayer, plugin.settings.DefaultDenizenUnavailableText(), talkType);
			break;

		case NoMatchingChatTriggers:
			textToSend = plugin.getAssignments().getString("Denizens." + theDenizen.getName() + ".Texts.No Chat Trigger");
			if (textToSend != null)	{
				if (textToSend.equals("")) return;
				talk(theDenizen, thePlayer, textToSend, talkType);
			}
			else talk(theDenizen, thePlayer, plugin.settings.DefaultNoChatTriggerText(), talkType);
			break;

		case NoMatchingClickTrigger:
			textToSend = plugin.getAssignments().getString("Denizens." + theDenizen.getName() + ".Texts.No Click Trigger");
			if (textToSend != null)	{
				if (textToSend.equals("")) return;
				talk(theDenizen, thePlayer, textToSend, talkType);
			}
			else talk(theDenizen, thePlayer, plugin.settings.DefaultNoClickTriggerText(), talkType);
			break;

		case NoMatchingDamageTrigger:
			textToSend = plugin.getAssignments().getString("Denizens." + theDenizen.getName() + ".Texts.No Damage Trigger");
			if (textToSend != null)	{
				if (textToSend.equals("")) return;
				talk(theDenizen, thePlayer, textToSend, talkType);
			}
			else talk(theDenizen, thePlayer, plugin.settings.DefaultNoDamageTriggerText(), talkType);
			break;

		}


	}

	private ArgumentHelper aH = null;

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
		String worldName = "";
		if (theDenizen != null){
			denizenName = theDenizen.getName();
			worldName= theDenizen.getWorld().getName();
		}

		switch (talkType) {

		case SHOUT:	
			playerMessageFormat = plugin.settings.NpcShoutToPlayer();
			bystanderMessageFormat = plugin.settings.NpcShoutToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcShoutToBystanders();
			break;

		case WHISPER:
			playerMessageFormat = plugin.settings.NpcWhisperToPlayer();
			bystanderMessageFormat = plugin.settings.NpcWhisperToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcWhisperToBystanders();
			break;

		case EMOTE:
			toPlayer = false;
			bystanderMessageFormat = "<NPC> <TEXT>";
			break;

		case NARRATE:
			playerMessageFormat = "<TEXT>";
			break;

		case CHAT:
			playerMessageFormat = plugin.settings.NpcChatToPlayer();
			bystanderMessageFormat = plugin.settings.NpcChatToPlayerBystander();
			if (!toPlayer){	bystanderMessageFormat = plugin.settings.NpcChatToBystanders();
			}
			break;

		case CHAT_PLAYERONLY:
			playerMessageFormat = plugin.settings.NpcChatToPlayer();
			break;

		}

		String playername = "";
		String playerdispname = "";
		String playerhealth = "";

		if (thePlayer!=null)
		{
			playerhealth = String.valueOf(thePlayer.getHealth());
			playerdispname =  thePlayer.getDisplayName();
			playername =  thePlayer.getName();
		}

		if (aH == null) aH = plugin.getCommandRegistry().getArgumentHelper();

		if (playerMessageFormat != null)
			playerMessageFormat = colorizeText(aH.fillFlags(thePlayer, playerMessageFormat
					.replace("<TEXT>", theMessage)
					.replace("<NPC>", denizenName)
					.replace("<PLAYER>", playername)
					.replace("<DISPLAYNAME>", playerdispname)
					.replace("<WORLD>", worldName)
					.replace("<HEALTH>", playerhealth)));

		if (bystanderMessageFormat != null)
			bystanderMessageFormat = colorizeText(aH.fillFlags(thePlayer, bystanderMessageFormat
					.replace("<TEXT>", theMessage)
					.replace("<NPC>", denizenName)
					.replace("<PLAYER>", playername)
					.replace("<DISPLAYNAME>", playerdispname)
					.replace("<WORLD>", worldName)
					.replace("<HEALTH>", playerhealth)));

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

		case SHOUT:
			theRange = plugin.settings.NpcToPlayerShoutRangeInBlocks();
			break;

		case WHISPER:
			theRange = plugin.settings.NpcToPlayerWhisperRangeInBlocks();
			break;

		case EMOTE:
			theRange = plugin.settings.NpcEmoteRangeInBlocks();
			thePlayer.sendMessage(theBystanderMessage);
			break;

		default:
			theRange = plugin.settings.NpcToPlayerChatRangeInBlocks();
			break;

		}

		if (thePlayerMessage != null && !thePlayerMessage.equals("shhh...don't speak!") && thePlayer != null) 
			thePlayer.sendMessage(thePlayerMessage);

		if ((plugin.settings.BystandersHearNpcToPlayerChat() || thePlayerMessage == null)  && theBystanderMessage != null) {
			if (theRange > 0) {
				for (Player otherPlayer : plugin.getDenizenNPCRegistry().getInRange(theDenizen.getEntity(), theRange, thePlayer)) {
					otherPlayer.sendMessage(theBystanderMessage);
				}
			}
		}

		return;
	}



	// Thanks geckon :)

	public String colorizeText(String text) {
		Integer i = 0;
		String[] code = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","k","l","m","n","o","r"};

		for (ChatColor color : ChatColor.values()) 
		{
			if (i > 22) break;
			text = text.replaceAll("(?i)<" + color.name() + ">", "" + color);
			text = text.replaceAll("(?i)<&" + code[i] + ">", "" + color);
			text = text.replaceAll("(?i)%%" + code[i], "" + color);
			i++;
		}
		return text;
	}



	/**
	 * Talks to a NPC. Also has replaceable data, end-user, when using <NPC> <TEXT> <PLAYER> <FULLPLAYERNAME> <WORLD> or <HEALTH>.
	 *
	 * @param  theDenizen  the Citizens2 NPC object to talk to.
	 * @param  thePlayer  the Bukkit Player object that is doing the talking.
	 */

	public void talkToDenizen(DenizenNPC theDenizen, Player thePlayer, String theMessage) {

		thePlayer.sendMessage(colorizeText(aH.fillFlags(thePlayer, plugin.settings.PlayerChatToNpc()
				.replace("<TEXT>", theMessage)
				.replace("<NPC>", theDenizen.getName())
				.replace("<PLAYER>", thePlayer.getName())
				.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
				.replace("<WORLD>", thePlayer.getWorld().getName())
				.replace("<HEALTH>", String.valueOf(thePlayer.getHealth())))));

		if (plugin.settings.BystandersHearNpcToPlayerChat()) {
			int theRange = plugin.settings.NpcToPlayerChatRangeInBlocks();
			if (theRange > 0) {
				for (Player otherPlayer : plugin.getDenizenNPCRegistry().getInRange(theDenizen.getEntity(), theRange, thePlayer)) {
					otherPlayer.sendMessage(colorizeText(aH.fillFlags(thePlayer, plugin.settings.PlayerChatToNpcBystander()
							.replace("<TEXT>", theMessage)
							.replace("<NPC>", theDenizen.getName())
							.replace("<PLAYER>", thePlayer.getName())
							.replace("<DISPLAYNAME>", thePlayer.getDisplayName())
							.replace("<WORLD>", thePlayer.getWorld().getName())
							.replace("<HEALTH>", String.valueOf(thePlayer.getHealth())))));
				}
			}
		}

		return;
	}

}

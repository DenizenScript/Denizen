package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenCharacter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GetDenizen {

	private Denizen plugin;

	public GetDenizen(Denizen denizen) {
		plugin = denizen;
	}

	/*
	 * checkCooldown
	 * 
	 * Checks against the interactCooldown for a Player to see if it has allowed enough time to interact.
	 * 
	 */



	public boolean checkCooldown(Player thePlayer) {

		if (!Denizen.interactCooldown.containsKey(thePlayer)) return true;
		if (System.currentTimeMillis() >= Denizen.interactCooldown.get(thePlayer)) return true;

		return false;
	}

	public boolean checkLocationCooldown(Player thePlayer) {
		if (!Denizen.locationCooldown.containsKey(thePlayer)) return true;
		if (System.currentTimeMillis() >= Denizen.locationCooldown.get(thePlayer)) return true;
		return false;
	}


	


	/*
	 * getClosest
	 * 
	 * Gets a NPC object of the closest Denizen to the specified Player.
	 * 
	 */

	public NPC getClosest (Player thePlayer, int Range) {

		Double closestDistance = Double.valueOf(String.valueOf(Range));
		NPC closestDenizen = null;

		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
		if (DenizenNPCs.isEmpty()) return null;

		List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (NPC aDenizen : DenizenList) {
			if (aDenizen.isSpawned()
					&& aDenizen.getBukkitEntity().getWorld().equals(thePlayer.getWorld())
					&& aDenizen.getBukkitEntity().getLocation().distance(thePlayer.getLocation()) < closestDistance ) {
				closestDenizen = aDenizen; 
				closestDistance = aDenizen.getBukkitEntity().getLocation().distance(thePlayer.getLocation());
			}
		}

		return closestDenizen;
	}

	
	


	/*
	 * getInRange
	 * 
	 * Gets a List<NPC> of Denizens within a range of the specified Player.
	 * 
	 */

	public List<NPC> getInRange (Player thePlayer, int theRange) {

		List<NPC> DenizensWithinRange = new ArrayList<NPC>();

		Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
		if (DenizenNPCs.isEmpty()) return DenizensWithinRange;

		List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
		for (NPC aDenizenList : DenizenList) {
			if (aDenizenList.isSpawned()
					&& aDenizenList.getBukkitEntity().getWorld().equals(thePlayer.getWorld()) 
					&& aDenizenList.getBukkitEntity().getLocation().distance(thePlayer.getLocation()) < theRange)

				DenizensWithinRange.add(aDenizenList);
		}

		return DenizensWithinRange;
	}



	
	



	/**
	 * Makes a Denizen talk to a Player.
	 *
	 * @param  theDenizen  the Citizens2 NPC object that is doing the talking.
	 * @param  thePlayer  the Bukkit Player object to talk to.
	 * @param  commandArgs 
	 */

	public void talkToPlayer(NPC theDenizen, Player thePlayer, String thePlayerMessage, String theBystanderMessage, String messageType) {

		int theRange = 0;
		
		if (messageType.equalsIgnoreCase("SHOUT")) {
			theRange = plugin.settings.NpcToPlayerShoutRangeInBlocks();
		}
		
		else if (messageType.equalsIgnoreCase("WHISPER")) {
			theRange = plugin.settings.NpcToPlayerWhisperRangeInBlocks();
		}
		
		else if (messageType.equalsIgnoreCase("EMOTE")) {
			theRange = plugin.settings.NpcEmoteRangeInBlocks();
			thePlayer.sendMessage(theBystanderMessage);
		}

		else {
			theRange = plugin.settings.NpcToPlayerChatRangeInBlocks();
		}

		if (thePlayerMessage != null && !thePlayerMessage.equals("shhh...don't speak!")) thePlayer.sendMessage(thePlayerMessage);

		if ((plugin.settings.BystandersHearNpcToPlayerChat() || thePlayerMessage == null)  && theBystanderMessage != null) {
			if (theRange > 0) {
				for (Player otherPlayer : plugin.getPlayer.getInRange(theDenizen.getBukkitEntity(), theRange, thePlayer)) {
					otherPlayer.sendMessage(theBystanderMessage);
				}
			}
		}

		return;
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

		if (theText.length() > plugin.settings.MultiLineTextMaximumLength()) {

			processedText.add(0, "");

			int word = 0; int line = 0;

			while (word < text.length) {
				if (processedText.get(line).length() + text[word].length() < plugin.settings.MultiLineTextMaximumLength()) {
					processedText.set(line, processedText.get(line) + text[word] + " ");
					word++;
				}
				else { line++; processedText.add(""); }
			}
		}

		else processedText.add(0, theText);

		return processedText;
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

	public String[] formatChatText (String theMessage, String messageType, Player thePlayer, NPC theDenizen) {

		String playerMessageFormat = null;
		String bystanderMessageFormat = null;

		boolean toPlayer;

		if (thePlayer == null) toPlayer = false;
		else toPlayer = true;

		if (messageType.equalsIgnoreCase("SHOUT")) {
			playerMessageFormat = plugin.settings.NpcShoutToPlayer();
			bystanderMessageFormat = plugin.settings.NpcShoutToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcShoutToBystanders();
		}

		else if (messageType.equalsIgnoreCase("WHISPER")) {
			playerMessageFormat = plugin.settings.NpcWhisperToPlayer();
			bystanderMessageFormat = plugin.settings.NpcWhisperToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcWhisperToBystanders();
		}

		else if (messageType.equalsIgnoreCase("EMOTE")) {
			toPlayer = false;
			bystanderMessageFormat = "<NPC> <TEXT>";
		}

		else if (messageType.equalsIgnoreCase("NARRATE")) {
			playerMessageFormat = "<TEXT>";
		}

		else { /* CHAT */
			playerMessageFormat = plugin.settings.NpcChatToPlayer();
			bystanderMessageFormat = plugin.settings.NpcChatToPlayerBystander();
			if (!toPlayer) bystanderMessageFormat = plugin.settings.NpcChatToBystanders();
		}

		String denizenName = ""; 

		if (theDenizen != null) denizenName = theDenizen.getName();

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



	
	
	
	
	
	
	
	
	
}

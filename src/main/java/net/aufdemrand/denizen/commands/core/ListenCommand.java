package net.aufdemrand.denizen.commands.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.BlockListener;
import net.aufdemrand.denizen.listeners.KillListener;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.events.ListenerCancelEvent;
import net.aufdemrand.events.ListenerFinishEvent;
import net.citizensnpcs.command.exception.CommandException;


public class ListenCommand extends AbstractCommand implements Listener {

	/* LISTEN (ID:ID_String) [LISTENER_TYPE] (ARGUMENTS) 
	 *
	 * LISTEN CANCEL [ID]
	 * 
	 * LISTEN COMPLETE [ID]
	 * 
	 */

	/*
	 * LISTENER_TYPEs:
	 * 
	 * KILL [TYPE:GROUP|PLAYER|ENTITY|NPC] [NAME:Name|NPCID:#] [QTY:#] [SCRIPT:Script to trigger]
	 * 
	 */

	//Player:ListenerId  //Listener Instance
	Map<String, AbstractListener> playerListeners = new ConcurrentHashMap<String, AbstractListener>();

	enum ListenerType { KILL, BLOCK }

	public ListenCommand() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		if (theEntry.arguments() == null) {
			aH.echoDebug("...No arguments!");
			return false;
		}

		ListenerType listenerType;
		try { listenerType = ListenerType.valueOf(theEntry.arguments()[0].toUpperCase()); } 
		catch (Exception e) { aH.echoError("Invalid LISTENER_TYPE!"); return false;	}

		switch (listenerType) {

		case KILL:

			// KILL [TYPE:GROUP|PLAYER|ENTITY|NPC] [NAME:Name|NPCID:#] (QTY:#) [SCRIPT:Script to trigger]

			String killType = null;
			String killName = "*";
			String killQty = "1";
			String killScript = null;
			String killNPCId = "-1";
			String killListenerId = null;

			for (String thisArg : theEntry.arguments()){

				// Fill replaceables
				if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);

				if (aH.matchesScript(thisArg)) {
					killScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to run on completion '" + killScript + "'.");	
				}
				
				else if (thisArg.equalsIgnoreCase("KILL")) {
					aH.echoDebug("...creating new KILL listener.");	
				}

				else if (aH.matchesQuantity(thisArg)) {
					killQty = aH.getStringModifier(thisArg);
					aH.echoDebug("...completion on '" + killQty + "' kill(s).");	
				}

				else if (thisArg.toUpperCase().contains("NAME:")) {
					killName = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is/are '" + thisArg + "'.");	
				}

				else if (thisArg.toUpperCase().contains("TYPE:")) {
					killType = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill type is '" + killType + "'.");	
				}

				else if (aH.matchesNPCID(thisArg)) {
					killNPCId = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is '" + thisArg + "'.");	
				}
				
				else if (thisArg.toUpperCase().contains("ID:")) {
					killListenerId = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is '" + thisArg + "'.");	
				}

				else aH.echoError("Could not match argument '%s'!", thisArg);
			}

			if (killListenerId == null && killScript != null) killListenerId = killScript;
			else if (killListenerId == null) killScript = "Kill_Listener_" + System.currentTimeMillis();

			if (killType == null || killName == null || killScript == null || killListenerId == null) {
				aH.echoError("Not enough arguments! Check syntax.");
				return false;
			}

			List<String> killList = plugin.getSaves().getStringList("Players." + theEntry.getPlayer().getName() + ".Listeners.List");

			if (!killList.contains(killListenerId)) {

				playerListeners.put(theEntry.getPlayer().getName() + ":" + killListenerId, new KillListener());
				playerListeners.get(theEntry.getPlayer().getName() + ":" + killListenerId).build(killListenerId, theEntry.getPlayer(), new String[] { killType, killName, killNPCId, killQty }, killScript);
				
				killList.add(killListenerId);
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Listeners.List", killList);
			} else {
				aH.echoError("Already listening!");
				return false;
			}
			return true;
			
			
		case BLOCK:

			// KILL [TYPE:GROUP|PLAYER|ENTITY|NPC] [NAME:Name|NPCID:#] (QTY:#) [SCRIPT:Script to trigger]

			String blockType = null;
			String blockName = null;
			String blockQty = "1";
			String blockScript = null;
			String blockListenerId = null;

			for (String thisArg : theEntry.arguments()){

				// Fill replaceables
				if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);

				if (thisArg.equalsIgnoreCase("BLOCK")) {
					aH.echoDebug("...creating new BLOCK listener.");	
				}
				
				else if (aH.matchesScript(thisArg)) {
					blockScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to run on completion '" + blockScript + "'.");	
				}

				else if (aH.matchesQuantity(thisArg)) {
					blockQty = aH.getStringModifier(thisArg);
					aH.echoDebug("...completion on '" + blockQty + "' block(s).");	
				}

				else if (thisArg.toUpperCase().contains("NAME:")) {
					blockName = aH.getStringModifier(thisArg);
					aH.echoDebug("...block(s) to listen for is/are '" + blockName + "'.");	
				}

				else if (thisArg.toUpperCase().contains("ID:")) {
					blockListenerId = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is '" + thisArg + "'.");	
				}

				else if (thisArg.toUpperCase().contains("TYPE:")) {
					blockType = aH.getStringModifier(thisArg);
					aH.echoDebug("...block event type is '" + blockType + "'.");	
				}

				else aH.echoError("Could not match argument '%s'!", thisArg);
			}

			if (blockListenerId == null && blockScript != null) blockListenerId = blockScript;
			else if (blockListenerId == null) blockScript = "Block_Listener_" + System.currentTimeMillis();

			if (blockType == null || blockName == null || blockScript == null || blockListenerId == null) {
				aH.echoError("Not enough arguments! Check syntax.");
				return false;
			}

			List<String> blockList = plugin.getSaves().getStringList("Players." + theEntry.getPlayer().getName() + ".Listeners.List");

			if (!blockList.contains(blockListenerId)) {

				playerListeners.put(theEntry.getPlayer().getName() + ":" + blockListenerId, new BlockListener());
				playerListeners.get(theEntry.getPlayer().getName() + ":" + blockListenerId).build(blockListenerId, theEntry.getPlayer(), new String[] { blockType, blockName, blockQty }, blockScript);
				
				blockList.add(blockListenerId);
				plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Listeners.List", blockList);
			} else {
				aH.echoError("Already listening!");
				return false;
			}
			return true;

			
			// case SCRIPT:

		}

		return false;
	}


	@EventHandler
	public void playerLogoff(PlayerQuitEvent event) {

		List<String> listenerList = plugin.getSaves().getStringList("Players." + event.getPlayer().getName() + ".Listeners.List");

		if (!listenerList.isEmpty()) {

			for (String listener : listenerList) {
				if (playerListeners.get(event.getPlayer().getName() + ":" + listener) != null) {
					
					aH.echoDebug(ChatColor.YELLOW + "// " + event.getPlayer().getName() + " has a LISTENER in progress. Saving " + listener + ".");
					
					playerListeners.get(event.getPlayer().getName() + ":" + listener).save();
					playerListeners.remove(event.getPlayer().getName() + ":" + listener);
				} else {
					
					// Stray entry? Let's remove it.
					
					List<String> newList = plugin.getSaves().getStringList("Players." + event.getPlayer().getName() + ".Listeners.List");
					if (newList.contains(listener)) {
						newList.remove(listener);
					}
					plugin.getSaves().set("Players." + event.getPlayer().getName() + ".Listeners.List", newList);
				}

			}

		}
	}


	@EventHandler
	public void playerLogon(PlayerJoinEvent event) {

		List<String> listenerList = plugin.getSaves().getStringList("Players." + event.getPlayer().getName() + ".Listeners.List");

		if (!listenerList.isEmpty()) {
			for (String listener : listenerList) {

				aH.echoDebug(ChatColor.YELLOW + "// " + event.getPlayer().getName() + " has a LISTENER in progress. Loading " + listener + ".");
				
				try {
					switch ( ListenerType.valueOf(plugin.getSaves().getString("Players." + event.getPlayer().getName() + ".Listeners.Saves." + listener + ".Listen Type"))) {
					
					case KILL:
						playerListeners.put(event.getPlayer().getName() + ":" + listener, new KillListener());
						playerListeners.get(event.getPlayer().getName() + ":" + listener).load(event.getPlayer(), listener);
						break;

					case BLOCK:
						playerListeners.put(event.getPlayer().getName() + ":" + listener, new BlockListener());
						playerListeners.get(event.getPlayer().getName() + ":" + listener).load(event.getPlayer(), listener);
						break;

						// case CHAT:

					}

				} catch (Exception e) { aH.echoError("Error loading player listener '%s'!", listener); }
			
			playerListeners.get(event.getPlayer().getName() + ":" + listener).report();
			}
		}
	}




	// Called when a listener is finished.

	public void finish(Player thePlayer, String listenerId, String theScriptName, AbstractListener theListener) {

		// Remove from saves
		List<String> newList = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Listeners.List");
		if (newList.contains(listenerId)) {
			newList.remove(listenerId);
		}
		plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.List", newList);
		plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId, null);


		// Call event
		ListenerFinishEvent event = new ListenerFinishEvent(thePlayer, theListener);
		Bukkit.getServer().getPluginManager().callEvent(event);

		// Run task script
		ScriptHelper sE = plugin.getScriptEngine().helper;
		List<String> theScript = sE.getScript(theScriptName + ".Script");

		if (theScript.isEmpty()) {
			return;
		}
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theScript, theScriptName), QueueType.TASK);

		// Remove from Map
		playerListeners.remove(thePlayer.getName() + ":" + listenerId);

	}

	public void cancel(Player thePlayer, String listenerId) {

		// Remove from saves
		List<String> newList = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Listeners.List");
		if (newList.contains(listenerId)) {
			newList.remove(listenerId);
		}
		plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.List", newList);

		// Call Event
		ListenerCancelEvent event = new ListenerCancelEvent(thePlayer, listenerId);
		Bukkit.getServer().getPluginManager().callEvent(event);

		// Remove from Map
		playerListeners.remove(thePlayer.getName() + ":" + listenerId);


	}

}



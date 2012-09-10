package net.aufdemrand.denizen.commands.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.KillListener;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.runnables.FourItemRunnable;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.events.ListenerCancelEvent;
import net.aufdemrand.events.ListenerFinishEvent;
import net.citizensnpcs.command.exception.CommandException;


public class ListenCommand extends AbstractCommand {

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

	enum ListenerType { KILL }

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
			String killName = null;
			String killQty = "1";
			String killScript = null;
			String killNPCId = null;
			String killListenerId = null;

			for (String thisArg : theEntry.arguments()){

				if (aH.matchesScript(thisArg)) {
					killScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to run on completion '" + killScript + "'.");	
				}

				else if (aH.matchesQuantity(thisArg)) {
					killQty = aH.getStringModifier(thisArg);
					aH.echoDebug("...completion on '" + killQty + "' kill(s).");	
				}

				else if (thisArg.toUpperCase().contains("NAME:")) {
					killName = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is/are '" + thisArg + "'.");	
				}

				else if (thisArg.toUpperCase().contains("ID:")) {
					killListenerId = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is '" + thisArg + "'.");	
				}

				else if (thisArg.toUpperCase().contains("TYPE:")) {
					killType = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill type is '" + killType + "'.");	
				}

				else if (aH.matchesNPCID(thisArg)) {
					killNPCId = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is '" + thisArg + "'.");	
				}

				else aH.echoError("Could not match argument '%s'!", thisArg);
			}

			if (killType == null || killName == null || killScript == null || killListenerId == null) {
				aH.echoError("Not enough arguments! Check syntax.");
				return false;
			}

			playerListeners.put(theEntry.getPlayer().getName() + ":" + killListenerId, new KillListener());
			playerListeners.get(theEntry.getPlayer().getName() + ":" + killListenerId).build(theEntry.getPlayer(), new String[] { killType, killName, killNPCId, killListenerId, killQty }, killScript);

			
			List<String> newList = plugin.getSaves().getStringList("Players." + theEntry.getPlayer().getName() + ".Listeners.List");
			newList.add(killListenerId);
			plugin.getSaves().set("Players." + theEntry.getPlayer().getName() + ".Listeners.List", newList);
			
			return true;

		}

		return false;
	}

	
	
	

	
	
	
	// Called when a listener is finished.

	public void finish(Player thePlayer, String listenerId, String theScriptName, AbstractListener theListener) {

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

		// Call Event
		ListenerCancelEvent event = new ListenerCancelEvent(thePlayer, listenerId);
		Bukkit.getServer().getPluginManager().callEvent(event);

		// Remove from Map
		playerListeners.remove(thePlayer.getName() + ":" + listenerId);


	}

}



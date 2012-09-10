package net.aufdemrand.denizen.commands.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.runnables.FourItemRunnable;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
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

	//Player    //Listener Instance
	Map<String, List<AbstractListener>> playerListeners = new ConcurrentHashMap<String, List<AbstractListener>>();

	enum ListenerType { KILL }

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Get arguments */
		if (theEntry.arguments() == null) {
			aH.echoDebug("...No arguments!");
			return false;
		}

		ListenerType listenerType;
		
		try {
			listenerType = ListenerType.valueOf(theEntry.arguments()[0].toUpperCase());
		} catch (Exception e) {
			aH.echoError("Invalid LISTENER_TYPE!");
			return false;
		}

		switch (listenerType) {

		case KILL:

			 // KILL [TYPE:GROUP|PLAYER|ENTITY|NPC] [NAME:Name|NPCID:#] (QTY:#) [SCRIPT:Script to trigger]
			String killType;
			String killName;
			Integer killQty = 1;
			String killScript;
			Integer killNPCId = -1;
			
			for (String thisArg : theEntry.arguments()){

				if (aH.matchesScript(thisArg)) {
					killScript = aH.getStringModifier(thisArg);
					aH.echoDebug("...script to run on completion '" + killScript + "'.");	
				}

				else if (aH.matchesQuantity(thisArg)) {
					killQty = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...completion on '" + killQty + "' kill(s).");	
				}

				else if (thisArg.toUpperCase().contains("NAME:")) {
					killName = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill target is '" + thisArg + "'.");	
				}

				else if (thisArg.toUpperCase().contains("TYPE:")) {
					killType = aH.getStringModifier(thisArg);
					aH.echoDebug("...kill type is '" + killType + "'.");	
				}

				else if (aH.matchesNPCID(thisArg)) {
					killNPCId = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...kill target is '" + thisArg + "'.");	
				}
				else aH.echoError("Could not match argument '%s'!", thisArg);
			}
			
			
	
			break;
			

		}


		
//		if (theScriptName == null){
//			aH.echoDebug("...No task specified!");
//			return false;
//		}
//
//		if (theEntry.getCommand().equalsIgnoreCase("CANCELTASK")){
//			if (theEntry.getPlayer().hasMetadata(theScriptName)){
//				int tid = theEntry.getPlayer().getMetadata(theScriptName).get(0).asInt();
//				if (tid > 0) {
//					plugin.getServer().getScheduler().cancelTask(tid);
//					theEntry.getPlayer().removeMetadata(theScriptName, plugin);
//				}		
//			}
//			else
//			{
//				aH.echoDebug("...Pending task " + theScriptName + " not found for this player");
//				return false;
//			}
//			return true;
//		}
//
//		if (executer ==null){
//			aH.echoDebug("...Invalid NPC specified!");
//			return false;
//		}
//
//		/* Make delayed task to reset step if duration is set */
//		if (secs>0) {
//
//			int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
//					new FourItemRunnable<Player, String, DenizenNPC, Integer>(theEntry.getPlayer(), theScriptName, executer, 0) {
//
//				@Override
//				public void run(Player player, String theScriptName, DenizenNPC denizen, Integer oldStep) { 
//					if (player.hasMetadata(theScriptName)){
//						int tid = player.getMetadata(theScriptName).get(0).asInt();
//						if (tid > 0) {
//							plugin.getServer().getScheduler().cancelTask(tid);
//							player.removeMetadata(theScriptName, plugin);
//						}	
//					}
//
//					ScriptHelper sE = plugin.getScriptEngine().helper;
//					List<String> theScript = sE.getScript(theScriptName + ".Script");
//					if (theScript.isEmpty()) return;
//					sE.queueScriptEntries(player, sE.buildScriptEntries(player, denizen, theScript, theScriptName, 1), QueueType.TASK);		
//					player.removeMetadata(theScriptName, plugin);
//				}		
//			}, secs * 20);
//
//			theEntry.getPlayer().setMetadata(theScriptName,new org.bukkit.metadata.FixedMetadataValue(plugin,id));
//
//			return true;
//		}
//
//		ScriptHelper sE = plugin.getScriptEngine().helper;
//		List<String> theScript = sE.getScript(theScriptName + ".Script");
//		if (theScript.isEmpty()) return false;
//		sE.queueScriptEntries(theEntry.getPlayer(), sE.buildScriptEntries(theEntry.getPlayer(), executer, theScript, theScriptName, 1), QueueType.TASK);	
//

		return true;
	}

}



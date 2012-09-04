package net.aufdemrand.denizen.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.citizensnpcs.api.npc.NPC;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.events.ScriptFinishEvent;
import net.aufdemrand.events.ScriptQueueEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


/**
 * Contains methods used to parse and execute scripts, 
 * initiated by some kind of event trigger or interaction.
 * 
 * @author Jeremy
 * 
 */

public class ScriptEngine {


	/* Denizen Constructor */

	private Denizen plugin;
	public ScriptHelper helper;

	public ScriptEngine(Denizen denizen) {
		plugin = denizen;
		helper = new ScriptHelper(plugin);
	}	


	public enum QueueType {
		TRIGGER, TASK, ACTIVITY, CUSTOM
	}

	private Map<Player, List<ScriptEntry>> triggerQue = new ConcurrentHashMap<Player, List<ScriptEntry>>();
	private Map<Player, List<ScriptEntry>>    taskQue = new ConcurrentHashMap<Player, List<ScriptEntry>>();
	private Map<NPC, List<ScriptEntry>>   activityQue = new ConcurrentHashMap<NPC, List<ScriptEntry>>();


	/* Processes commands from the Queues. */

	public void runQueues() {

		/* First the triggerQue, primary script queue for Players */

		if (!triggerQue.isEmpty()) {	

			/* Attempt to run a command for each player. The attempted command (and attached info) info is 
			 * in theEntry */
			for (Entry<Player, List<ScriptEntry>> theEntry : triggerQue.entrySet()) {
				if (!theEntry.getValue().isEmpty()) {

					/* Check the time of the command to see if it has been delayed with a WAIT command. Only 
					 * proceed for the player if the time on the command is less than the current time. 
					 * If it's more, then this entry will be skipped and saved for next time. */
					if (theEntry.getValue().get(0).getDelayedTime() < System.currentTimeMillis()) {

						/* Feeds the executer ScriptCommands as long as they are instant commands ("^"), otherwise
						 * runs one command, removes it from the queue, and moves on to the next player. */
						boolean instantly;

						do { 
							instantly = false;
							ScriptEntry theCommand = theEntry.getValue().get(0);
							theCommand.setSendingQueue(QueueType.TRIGGER);

							// Instant command check
							if (theEntry.getValue().size() > 1
									&& theEntry.getValue().get(0).isInstant())
								instantly = true; 
							// ----

							// Fire event for possible cancellation/alterations
							ScriptQueueEvent event = new ScriptQueueEvent(theCommand.getPlayer(), theCommand);
							Bukkit.getServer().getPluginManager().callEvent(event);

							// If event is altered, update the command.
							if (event.isAltered()) theCommand = event.getScriptEntry();
							
							theEntry.getValue().remove(0);

							/* Updates the triggerQue map */
							triggerQue.put(theEntry.getKey(), theEntry.getValue());

							// Catching errors here will kep the queue from locking up.
							try { 
								// Check if execution was cancelled by the event
								if (event.isCancelled())
									plugin.getLogger().info("Note: ScriptEntry cancelled!");
								// Nope! Execute the command!
								else plugin.executer.execute(theCommand); }
							catch (Throwable e) {
								plugin.getLogger().info("Woah! Bad command! Check syntax...");
								if (plugin.showStackTraces) e.printStackTrace();
							}

						} while (instantly);
					}
				}
			}
			/* Next Player */
		}


		/* Now the taskQue, the alternate script queue for Players */

		if (!taskQue.isEmpty()) {	
			for (Entry<Player, List<ScriptEntry>> theEntry : taskQue.entrySet()) {
				if (!theEntry.getValue().isEmpty()) {
					if (theEntry.getValue().get(0).getDelayedTime() < System.currentTimeMillis()) {
						boolean instantly;
						do { 
							instantly = false;
							ScriptEntry theCommand = theEntry.getValue().get(0);
							theCommand.setSendingQueue(QueueType.TASK);

							// Instant command check
							if (theEntry.getValue().size() > 1
									&& theEntry.getValue().get(0).isInstant())
								instantly = true; 
							// ----

							theEntry.getValue().remove(0);
							taskQue.put(theEntry.getKey(), theEntry.getValue());

							plugin.executer.execute(theCommand);

						} while (instantly);

					}
				}
			}
			/* Next Player */
		}


		/* 
		 * TODO: activityQue
		 */

	}




	/* Parses the script for a task trigger */

	public boolean parseTaskScript(Player thePlayer, String theScript) {

		List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

		/* Let's get the Script from the file and turn it into ScriptCommands */
		List<String> chatScriptItems = plugin.getScripts().getStringList(theScript + ".Script");
		for (String thisItem : chatScriptItems) {
			String[] scriptEntry = new String[2];
			scriptEntry = thisItem.split(" ", 2);
			try {
				/* Build new script commands */
				scriptCommands.add(new ScriptEntry(scriptEntry[0], helper.buildArgs(scriptEntry[1]), thePlayer, theScript));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* New ScriptCommand list built, now let's add it into the queue */
		List<ScriptEntry> scriptCommandList = taskQue.get(thePlayer);

		/* Keeps the commandQue from removing items while
		working on them here. They will be added back in. */ 
		taskQue.remove(thePlayer); 

		scriptCommandList.addAll(scriptCommands);
		taskQue.put(thePlayer, scriptCommandList);

		return true;
	}



	/** Injects commands into a QueueType  */

	public void injectToQueue(Player thePlayer, List<ScriptEntry> scriptCommands, QueueType queueType, int thePosition) {

		List<ScriptEntry> scriptCommandList = new ArrayList<ScriptEntry>();

		switch (queueType) {

		case TRIGGER:
			scriptCommandList = triggerQue.get(thePlayer);
			triggerQue.remove(thePlayer); 
			if (thePosition > scriptCommandList.size() || thePosition < 0) thePosition = 1;
			if (scriptCommandList.size() == 0) thePosition = 0;
			scriptCommandList.addAll(thePosition, scriptCommands);
			triggerQue.put(thePlayer, scriptCommandList);
			break;

		case TASK:
			scriptCommandList = taskQue.get(thePlayer);
			taskQue.remove(thePlayer); 
			if (thePosition > scriptCommandList.size() || thePosition < 0) thePosition = 1;
			if (scriptCommandList.size() == 0) thePosition = 0;
			scriptCommandList.addAll(thePosition, scriptCommands);
			taskQue.put(thePlayer, scriptCommandList);
			break;
		}

		return;
	}

	/** Injects commands into a QueueType  */

	public void injectToQueue(Player thePlayer, ScriptEntry scriptCommand, QueueType queueType, int thePosition) {

		List<ScriptEntry> scriptCommandList = new ArrayList<ScriptEntry>();

		switch (queueType) {

		case TRIGGER:
			scriptCommandList = triggerQue.get(thePlayer);
			triggerQue.remove(thePlayer); 
			if (thePosition > scriptCommandList.size() || thePosition < 0) thePosition = 1;
			if (scriptCommandList.size() == 0) thePosition = 0;
			scriptCommandList.add(thePosition, scriptCommand);
			triggerQue.put(thePlayer, scriptCommandList);
			break;

		case TASK:
			scriptCommandList = taskQue.get(thePlayer);
			taskQue.remove(thePlayer); 
			if (thePosition > scriptCommandList.size() || thePosition < 0) thePosition = 1;
			if (scriptCommandList.size() == 0) thePosition = 0;
			scriptCommandList.add(thePosition, scriptCommand);
			taskQue.put(thePlayer, scriptCommandList);
			break;
		}

		return;
	}


	public void injectToQue(Denizen theDenizen, List<ScriptEntry> scriptCommands, QueueType queueType, int thePosition) {

		/* 
		 * TODO: ActivityQue injection sequence
		 */

	}


	/** Retrieves a QueueType  */

	public Map<Player, List<ScriptEntry>> getQueue(QueueType queueType) {

		switch (queueType) {

		case TRIGGER:
			return triggerQue;

		case TASK:
			return taskQue;
		}

		return null;
	}


	/** Adds commands to a QueueType  */

	public void addToQue(Player thePlayer, List<ScriptEntry> scriptCommands, QueueType queueType) {

		List<ScriptEntry> scriptCommandList;

		switch (queueType) {

		case TRIGGER:
			scriptCommandList = triggerQue.get(thePlayer);
			triggerQue.remove(thePlayer); 
			scriptCommandList.addAll(scriptCommands);
			triggerQue.put(thePlayer, scriptCommandList);
			break;

		case TASK:
			scriptCommandList = taskQue.get(thePlayer);
			taskQue.remove(thePlayer); 
			scriptCommandList.addAll(scriptCommands);
			taskQue.put(thePlayer, scriptCommandList);
			break;
		}

		return;
	}

	public void addToQue(Denizen theDenizen, List<ScriptEntry> scriptCommands, QueueType queueType) {

		/* 
		 * TODO: ActivityQue add sequence
		 */

	}

	public List<ScriptEntry> getPlayerQueue(Player thePlayer, QueueType sendingQueue) {
		return getQueue(sendingQueue).get(thePlayer);
	}


	// Use with care! This could be confusing to the player if not properly used!
	public void replacePlayerQue(Player thePlayer, List<ScriptEntry> scriptCommands, QueueType queueType) {

		switch (queueType) {

		case TRIGGER:
			triggerQue.remove(thePlayer); 
			triggerQue.put(thePlayer, scriptCommands);
			break;

		case TASK:
			taskQue.remove(thePlayer); 
			taskQue.put(thePlayer, scriptCommands);
			break;
		}

		return;
	}




}
package net.aufdemrand.denizen.scripts.commands.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Randomly selects a random script entry from the proceeding
 * entries, discards the rest.
 * 
 * @author Jeremy Schroeder
 */

public class RandomCommand extends AbstractCommand {

	/* RANDOM [#] */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [#] of entries to randomly select from. Will select 1 of # to execute
	 *   and discard the rest.
	 *   
	 * Example Usage:
	 * RANDOM 3
	 * CHAT Random Message 1
	 * CHAT Random Message 2
	 * CHAT Random Message 3
	 * 
	 */
	
	Integer numberOfEntries;
	Player player;
	List<ScriptEntry> currentQueue = new ArrayList<ScriptEntry>();
	QueueType sendingQueue;
	DenizenNPC npc;
	
	@Override
	public void onEnable() {
		// nothing to do here
	}

	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		
		Integer numberOfEntries = null;
		sendingQueue = scriptEntry.getSendingQueue();
		npc = scriptEntry.getNPC();
		
		for (String arg : scriptEntry.getArguments()) {
			
			if (scriptEntry.getPlayer() != null) player = scriptEntry.getPlayer();

			if (aH.matchesInteger(arg)) {
				numberOfEntries = aH.getIntegerFrom(arg);
				dB.echoDebug("...will randomly select from the next %s entries.", arg);
			}
			
			else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}	

		if (numberOfEntries == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION);
		
		if (player != null) currentQueue = denizen.getScriptEngine().getPlayerQueue(player, sendingQueue);
		else currentQueue = denizen.getScriptEngine().getDenizenQueue(npc, sendingQueue);

		if (currentQueue.size() < numberOfEntries) throw new InvalidArgumentsException("Invalid Size! RANDOM [#] must not be larger than the script!");
		
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		Random random = new Random();
		int selected = random.nextInt(numberOfEntries);
		ScriptEntry sEtoKeep = null;
		
		dB.echoDebug("...random number generator selected '%s'", String.valueOf(selected + 1));
		
		for (int x = 0; x < numberOfEntries; x++) {
			if (x != selected) {
				dB.echoDebug("...removing '%s'", currentQueue.get(0).getCommand());
				currentQueue.remove(0);
			} else {
				dB.echoDebug("...selected '%s'", currentQueue.get(0).getCommand() + ": " + currentQueue.get(0).getArguments());
				sEtoKeep = currentQueue.get(0);
				currentQueue.remove(0);
			}
		}
		
		currentQueue.add(0, sEtoKeep);
		if (player != null) denizen.getScriptEngine().replaceQueue(player, currentQueue, sendingQueue);
		else denizen.getScriptEngine().replaceQueue(npc, currentQueue, sendingQueue);
		
	}
}
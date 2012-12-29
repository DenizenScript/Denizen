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
 * Randomly selects a random script entry from the proceeding entries, discards
 * the rest.
 *
 * 	<ol><tt>Usage:  RANDOM [#]</tt></ol>
 * 
 * [#] of entries to randomly select from. Will select 1 of # to execute and
 * discard the rest.<br/><br/>
 *   
 * Example Usage:<br/>
 * <ul style="list-style-type: none;">
 * <li><tt>Script:</tt></li>
 * <li><tt>- RANDOM 3</tt></li>
 * <li><tt>- CHAT Random Message 1</tt></li>
 * <li><tt>- CHAT Random Message 2</tt></li>
 * <li><tt>- CHAT Random Message 3 </tt></li>
 * </ul>
 * 
 * @author Jeremy Schroeder
 */

public class RandomCommand extends AbstractCommand {
	private	Integer numberOfEntries = null;
	private	Player player;
	private	List<ScriptEntry> currentQueue = new ArrayList<ScriptEntry>();
	private	QueueType sendingQueue;
	private	DenizenNPC npc;
	
	@Override
	public void onEnable() {
	}

	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		
		sendingQueue = scriptEntry.getSendingQueue();
		this.npc = scriptEntry.getNPC();
		
		if (scriptEntry.getPlayer() != null) {
			this.player = scriptEntry.getPlayer();
		}
		
		for (String arg : scriptEntry.getArguments()) {
			//
			// Make sure the random number is an integer.
			//
			if (aH.matchesInteger(arg)) {
				this.numberOfEntries = aH.getIntegerFrom(arg);
				dB.echoDebug("...will randomly select from the next %s entries.", arg);
			} else {
				throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
			}
		}	

		if (this.numberOfEntries == null) {
			throw new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION);
		}

		currentQueue = (this.player != null) ?
			denizen.getScriptEngine().getPlayerQueue(player, sendingQueue) :
			denizen.getScriptEngine().getDenizenQueue(npc, sendingQueue);

		if (currentQueue.size() < this.numberOfEntries) {
			throw new InvalidArgumentsException("Invalid Size! RANDOM [#] must not be larger than the script!");
		}
		
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		Random random = new Random();
		int selected = random.nextInt(this.numberOfEntries);
		ScriptEntry sEtoKeep = null;
		
		dB.echoDebug("...random number generator selected '%s'", String.valueOf(selected + 1));
		
		for (int x = 0; x < this.numberOfEntries; x++) {
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
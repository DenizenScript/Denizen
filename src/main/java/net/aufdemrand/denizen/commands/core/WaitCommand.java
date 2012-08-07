package net.aufdemrand.denizen.commands.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.citizensnpcs.command.exception.CommandException;

import org.bukkit.entity.Player;

/**
 * Holds processing of a QueueType which results in a 'pause'.
 * Used to pause processing of commands.
 * 
 * @author Jeremy Schroeder
 *
 */

public class WaitCommand extends AbstractCommand {

	/* WAIT [# OF SECONDS] */   

	/* Arguments: [] - Required, () - Optional 
	 * [# OF SECONDS] Number of seconds to hold the queue. 
	 * 
	 * Modifiers: 
	 * (QUEUETYPE:TASK|TRIGGER) Specifies which queue to hold. Be default, it holds the same
	 *   queue that triggered the command.
	 * 
	 * Example usage:
	 * WAIT 3
	 * WAIT 20 QUEUETYPE:TRIGGER
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* The WAIT command ultimately sends itself back to the que with an appropriate delay. 
		 * if the delay is more than the time initiated, we can assume it's the second time 
		 * around, and therefore finish the command right now. */

		if (theEntry.getDelayedTime() > theEntry.getInitiatedTime()) {
			if (plugin.debugMode) 
				plugin.getLogger().log(Level.INFO, "...and we've waited. Resuming.");
			return true;
		}

		/* Initialize variables */

		QueueType queueToHold = theEntry.sendingQueue();
		Player thePlayer = theEntry.getPlayer();
		theEntry.setInstant();

		/* Process arguments */

		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				if (plugin.debugMode) 
					plugin.getLogger().log(Level.INFO, "Processing command " + theEntry.getCommand() + " argument: " + thisArgument);
				
				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...setting Delay.");
					theEntry.setDelay(System.currentTimeMillis() + (Long.valueOf(thisArgument) * 1000));
				}
				
				if (thisArgument.toUpperCase().contains("QUEUETYPE:")) {
					if (plugin.debugMode) 
						plugin.getLogger().log(Level.INFO, "...setting QueueType.");
					try {
						queueToHold = QueueType.valueOf(thisArgument.split(":")[1]);
					} catch (Throwable e) {
						throw new CommandException("Invalid QUEUETYPE.");
					}
				}
			}
		}

		/* Put itself back into the queue */

		List<ScriptEntry> theList = new ArrayList<ScriptEntry>();
		theList.add(theEntry);

		if (queueToHold == QueueType.TASK) {
			if (plugin.debugMode) 
				plugin.getLogger().log(Level.INFO, "...now Waiting.");
			plugin.getScriptEngine().injectToQueue(thePlayer, theList, QueueType.TASK, 1);
			return true;
		}

		if (queueToHold == QueueType.TRIGGER) {
			if (plugin.debugMode) 
				plugin.getLogger().log(Level.INFO, "...now Waiting.");
			plugin.getScriptEngine().injectToQueue(thePlayer, theList, QueueType.TRIGGER, 1);
			return true;
		}

		throw new CommandException("...Usage: WAIT [# OF SECONDS]");
	}

}

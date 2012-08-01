package net.aufdemrand.denizen.commands.core;

import java.util.ArrayList;
import java.util.List;

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
	public boolean execute(ScriptEntry theCommand) throws CommandException {

		/* The WAIT command ultimately sends itself back to the que with an appropriate delay. 
		 * if the delay is more than the time initiated, we can assume it's the second time 
		 * around, and therefore finish the command right now. */

		if (theCommand.getDelayedTime() > theCommand.getInitiatedTime()) {
			return true;
		}

		/* Initialize variables */

		QueueType queueToHold = theCommand.sendingQueue();
		Player thePlayer = theCommand.getPlayer();
		theCommand.setInstant();

		/* Process arguments */

		if (theCommand.arguments() != null) {
			for (String thisArgument : theCommand.arguments()) {

				if (thisArgument.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) 
					theCommand.setDelay(System.currentTimeMillis() + (Long.valueOf(theCommand.arguments()[0]) * 1000));

				if (thisArgument.toUpperCase().contains("QUEUETYPE:"))
					try {
						queueToHold = QueueType.valueOf(thisArgument.split(":")[1]);
					} catch (Throwable e) {
						throw new CommandException("Invalid QUEUETYPE.");
					}
			}
		}

		/* Put itself back into the queue */

		List<ScriptEntry> theList = new ArrayList<ScriptEntry>();
		theList.add(theCommand);

		if (queueToHold == QueueType.TASK) {
			plugin.getScriptEngine().injectToQue(thePlayer, theList, QueueType.TASK, 1);
			return true;
		}

		if (queueToHold == QueueType.TRIGGER) {
			plugin.getScriptEngine().injectToQue(thePlayer, theList, QueueType.TRIGGER, 1);
			return true;
		}

		throw new CommandException("Unknown error, check syntax!");
	}

}

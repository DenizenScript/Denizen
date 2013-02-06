package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import java.util.Random;

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

	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {

        int possibilities = 1;
        ScriptQueue queue;

		queue = ScriptQueue._getQueue(scriptEntry.getResidingQueue());
		
		for (String arg : scriptEntry.getArguments()) {
			//
			// Make sure the random number is an integer.
			//
			if (aH.matchesInteger(arg)) {
				possibilities = aH.getIntegerFrom(arg);
			} else {
				throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
			}
		}	

		if (possibilities <= 1) {
			throw new InvalidArgumentsException("Must randomly select more than one item.");
		}

		if (queue.getQueueSize() < possibilities) {
			throw new InvalidArgumentsException("Invalid Size! RANDOM [#] must not be larger than the script!");
		}

        scriptEntry.addObject("possibilities", possibilities);
        scriptEntry.addObject("queue", queue);
		
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Integer possibilities = (Integer) scriptEntry.getObject("possibilities");
        ScriptQueue queue = (ScriptQueue) scriptEntry.getObject("queue");

		Random random = new Random();
		int selected = random.nextInt(possibilities);
		ScriptEntry keeping = null;
		
		dB.echoDebug("...random number generator selected '%s'", String.valueOf(selected + 1));
		
		for (int x = 0; x < possibilities; x++) {
			if (x != selected) {
				dB.echoDebug("...removing '%s'", queue.getEntry(0).getCommandName());
				queue.removeEntry(0);
			} else {
				dB.echoDebug("...selected '%s'", queue.getEntry(0).getCommandName() + ": "
                        + queue.getEntry(0).getArguments());
				keeping = queue.getEntry(0);
				queue.removeEntry(0);
			}
		}
		
		queue.injectEntry(keeping, 0);
	}
}
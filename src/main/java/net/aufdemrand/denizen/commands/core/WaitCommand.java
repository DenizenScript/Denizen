package net.aufdemrand.denizen.commands.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.QueueType;

import org.bukkit.entity.Player;

public class WaitCommand extends Command {

	@Override
	public boolean execute(ScriptCommand theCommand) {

		if (theCommand.arguments().length > 1 || theCommand.arguments().length < 1) {
			theCommand.error("Wrong number of arguments!");
			return false;
		}

		if (!Character.isDigit(theCommand.arguments()[0].charAt(0))) {
			theCommand.error("You must specify a number!");
		}
		
		if (theCommand.getDelayedTime() > theCommand.getInitiatedTime()) {
			/* Second time around, so we've already waited! */
			return true;
		}

		Player thePlayer = theCommand.getPlayer();

		/* WAIT [# OF SECONDS]*/

		theCommand.setDelay(System.currentTimeMillis() + (Long.valueOf(theCommand.arguments()[0]) * 1000));
		theCommand.setInstant();
		List<ScriptCommand> theList = new ArrayList<ScriptCommand>();
		theList.add(theCommand);

		if (theCommand.sendingQueue() == QueueType.TASK) {
			plugin.scriptEngine.injectToQue(thePlayer, theList, QueueType.TASK, 1);
			return true;
		}

		if (theCommand.sendingQueue() == QueueType.TRIGGER) {
			plugin.scriptEngine.injectToQue(thePlayer, theList, QueueType.TRIGGER, 1);
			return true;
		}

		return false;
	}

}

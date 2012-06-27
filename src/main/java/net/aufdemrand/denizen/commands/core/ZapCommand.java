package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;
import net.aufdemrand.denizen.scriptEngine.ScriptEngine.CommandHas;

import org.bukkit.entity.Player;

public class ZapCommand extends Command {

	@Override
	public boolean execute(ScriptCommand theCommand) {

		if (theCommand.size() == CommandHas.SOME) {
			theCommand.error("ZAP cannot be used with a Task Script.");
			return false;
		}
		
		if (theCommand.arguments().length > 1) {
			theCommand.error("Too many arguments!");
			return false;
		}
		
		Player thePlayer = theCommand.getPlayer();
		String theScript = theCommand.getScript();
		Integer theStep = theCommand.getStep();
		
		/* ZAP */
		
		if (theCommand.arguments().length == 0) {
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Current Step", theStep + 1);
			plugin.saveSaves();
			return true;
		}
		
		Integer newStep = null;
		
		/* ZAP [STEP #]*/
		
		if (theCommand.arguments().length == 1) { 
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Current Step", newStep); 
			plugin.saveSaves();
			return true;
		}
	
		return false;
	}

}

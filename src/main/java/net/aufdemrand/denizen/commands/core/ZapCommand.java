package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.Command;
import net.aufdemrand.denizen.scriptEngine.ScriptCommand;

import org.bukkit.entity.Player;

public class ZapCommand extends Command {

	@Override
	public boolean execute(ScriptCommand theCommand) {

		Player thePlayer = theCommand.getPlayer();
		if (thePlayer == null) theCommand.error("ZAP command requires a Player.");
		
		String theScript = theCommand.getScript();
		if (theScript == null) theCommand.error("ZAP command requires a Script.");
		
		Integer theStep = theCommand.getStep();
		String newStep = null;
		
		if (theCommand.arguments().length >= 1) newStep = theCommand.arguments()[0];
		
		if (newStep == null) {
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Current Step", theStep + 1);
			plugin.saveSaves();
		}
		else { 
			plugin.getSaves().set("Players." + thePlayer.getName() + "." + theScript + ".Current Step", Integer.parseInt(newStep)); 
			plugin.saveSaves();
		}

		return true;
	}



}

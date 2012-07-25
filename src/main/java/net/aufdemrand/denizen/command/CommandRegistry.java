package net.aufdemrand.denizen.command;

import java.rmi.activation.ActivationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.command.core.EngageCommand;
import net.aufdemrand.denizen.command.core.SpawnCommand;
import net.aufdemrand.denizen.command.core.WaitCommand;
import net.aufdemrand.denizen.command.core.ZapCommand;
import net.aufdemrand.denizen.scriptEngine.AbstractTrigger;

public class CommandRegistry {

	private Map<String, Command> commands = new HashMap<String, Command>();

	public Denizen plugin;


	public CommandRegistry(Denizen denizen) {
		plugin = denizen;
	}


	public boolean registerCommand(String commandName, Command commandClass) {
		this.commands.put(commandName.toUpperCase(), commandClass);
		plugin.getLogger().log(Level.INFO, "Loaded " + commandName + " successfully!");
		return true;
	}


	public Map<String, Command> listCommands() {
		return commands;
	}

	public Command getCommand(String commandName) {
		if (commands.containsKey(commandName.toUpperCase()))
			return commands.get(commandName);
		else
			return null;
	}

	public void registerCoreCommands() {

		ZapCommand zapCommand = new ZapCommand();
		EngageCommand engageCommand = new EngageCommand();
		SpawnCommand spawnCommand = new SpawnCommand();
		WaitCommand waitCommand = new WaitCommand();
		
		try {
			zapCommand.activateAs("ZAP");
			engageCommand.activateAs("ENGAGE");
			engageCommand.activateAs("DISENGAGE");
			spawnCommand.activateAs("SPAWN");
			waitCommand.activateAs("WAIT");
		} catch (ActivationException e) {
			plugin.getLogger().log(Level.SEVERE, "Oh no! Denizen has run into a problem registering the core commands!");
			e.printStackTrace();
		}
		
		
	}


}

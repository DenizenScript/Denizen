package net.aufdemrand.denizen.commands;

import java.rmi.activation.ActivationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.core.ZapCommand;

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

	public void registerCoreCommands() throws ActivationException {
		ZapCommand zapCommand = new ZapCommand();
		zapCommand.activateAs("ZAP");
		
	}


}

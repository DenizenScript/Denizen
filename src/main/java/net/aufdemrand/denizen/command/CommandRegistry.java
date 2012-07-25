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


public class CommandRegistry {

	private Map<String, Command> commands = new HashMap<String, Command>();
	private Map<Class<? extends Command>, String> commandsClass = new HashMap<Class<? extends Command>, String>();
	public Denizen plugin;


	public CommandRegistry(Denizen denizen) {
		plugin = denizen;
	}


	public boolean registerCommand(String commandName, Command commandClass) {
		this.commands.put(commandName.toUpperCase(), commandClass);
		this.commandsClass.put(commandClass.getClass(), commandName.toUpperCase());
		plugin.getLogger().log(Level.INFO, "Loaded " + commandName + " successfully!");
		return true;
	}


	public Map<String, Command> listCommands() {
		return commands;
	}

	
	public <T extends Command> T getCommand(Class<T> theClass) {
		if (commandsClass.containsKey(theClass))
			return (T) theClass.cast(commands.get(commandsClass.get(theClass)));
		else
			return null;
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

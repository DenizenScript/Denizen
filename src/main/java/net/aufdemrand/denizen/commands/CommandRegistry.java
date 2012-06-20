package net.aufdemrand.denizen.commands;

import java.util.Map;

import net.aufdemrand.denizen.Denizen;

public class CommandRegistry {
	
	private Map<String, Command> commands;
	
	public Denizen plugin;
	
	
	public CommandRegistry(Denizen denizen) {
    	plugin = denizen;
	}


	public boolean registerCommand(String commandName, Command commandClass) {
        
		this.commands.put(commandName, commandClass);
        return true;
    }
    
    
	public Map<String, Command> listCommands() {
    	return commands;
    }
    
    
	public Command getCommand(String commandName) {
    	Command thisCommand = commands.get(commandName);
    	return thisCommand;
    }
    
    
    
}

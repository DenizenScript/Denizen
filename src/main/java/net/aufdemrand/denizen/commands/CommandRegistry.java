package net.aufdemrand.denizen.commands;

import java.util.Map;

public class CommandRegistry {
	
	private Map<String, Command> commands;

    public void registerCommand(String commandName, Command commandClass) {
        this.commands.put(commandName, commandClass);
    }
    
    public Map<String, Command> listCommands() {
    	return commands;
    }
    
    public Command getCommand(String commandName) {
    	Command thisCommand = commands.get(commandName);
    	return thisCommand;
    }
    
    
    
}

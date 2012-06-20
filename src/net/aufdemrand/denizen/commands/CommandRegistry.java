package net.aufdemrand.denizen.commands;

import java.util.Map;

public class CommandRegistry {
	
	private Map<String, Class<? extends Command>> commands;

    public void registerCommand(String commandName, Class<? extends Command> commandClass) {
        this.commands.put(commandName, commandClass);
    }
    
    
    
    
    public Map<String, Class<? extends Command>> getCommands() {
    	return commands;
    }
    
    
    
}

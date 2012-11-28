package net.aufdemrand.denizen.scripts.commands;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper;
import net.aufdemrand.denizen.utilities.debugging.Debugger;

import org.bukkit.Bukkit;

public abstract class AbstractCommand implements RegistrationableInstance {

	public Denizen denizen;
	
	protected ArgumentHelper aH;
	protected Debugger dB;
	
	protected String name;
    public CommandOptions commandOptions = new CommandOptions();

    public class CommandOptions { 
        public String USAGE_HINT = ""; 
        public int REQUIRED_ARGS = -1;

        public CommandOptions() { }
        
        public CommandOptions(String usageHint, int numberOfRequiredArgs) {
            this.USAGE_HINT = usageHint;
            this.REQUIRED_ARGS = numberOfRequiredArgs;
        }
    }

	@Override
	public AbstractCommand activate() {
		this.denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		// Reference Helper Classes
		aH = denizen.getScriptEngine().getArgumentHelper();
		dB = denizen.getDebugger();
		return this;
	}

	@Override
	public AbstractCommand as(String commandName) {
		// Register command with Registry with a Name
		name = commandName.toUpperCase();
		denizen.getCommandRegistry().register(commandName, (RegistrationableInstance) this);
		onEnable();
		return this;
	}
	
    public AbstractCommand withOptions(String usageHint, int numberOfRequiredArgs) {
        this.commandOptions = new CommandOptions(usageHint, numberOfRequiredArgs);
        return this;
    }
    
    public CommandOptions getOptions() {
        return commandOptions;
    }
	
	@Override
	public String getName() {
	    return name;
	}
	
	public String getUsageHint(String commandName) {
		return !commandOptions.USAGE_HINT.equals("") ? commandOptions.USAGE_HINT : "No usage defined! See documentation for more information!";
	}
	
	public abstract void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException;

	public abstract void execute(String commandName) throws CommandExecutionException;
	
}

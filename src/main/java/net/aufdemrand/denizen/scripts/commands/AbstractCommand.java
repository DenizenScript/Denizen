package net.aufdemrand.denizen.scripts.commands;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper;

import org.bukkit.Bukkit;

public abstract class AbstractCommand implements RegistrationableInstance {

	/**
	 * Contains required options for a Command in a single class for the
	 * ability to add optional options in the future.
	 *
	 */
	public class CommandOptions { 
        public String USAGE_HINT = ""; 
        public int REQUIRED_ARGS = -1;
        
        public CommandOptions(String usageHint, int numberOfRequiredArgs) {
            this.USAGE_HINT = usageHint;
            this.REQUIRED_ARGS = numberOfRequiredArgs;
        }
    }
	
	public Denizen denizen;
		
	protected ArgumentHelper aH;
    protected String name;

    public CommandOptions commandOptions;

	@Override
	public AbstractCommand activate() {
		this.denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		// Reference Helper Classes
		aH = denizen.getScriptEngine().getArgumentHelper();
		return this;
	}

	@Override
	public AbstractCommand as(String commandName) {
		// Register command with Registry with a Name
		name = commandName.toUpperCase();
		denizen.getCommandRegistry().register(this.name, this);
		onEnable();
		return this;
	}
	
    public abstract void execute(String commandName) throws CommandExecutionException;
    
    @Override
	public String getName() {
	    return name;
	}
	
	public CommandOptions getOptions() {
        return commandOptions;
    }
	
	public String getUsageHint() {
		return !commandOptions.USAGE_HINT.equals("") ? commandOptions.USAGE_HINT : "No usage defined! See documentation for more information!";
	}
	
	/**
	 * Part of the Plugin disable sequence.
	 * 
	 * Can be '@Override'n by a Command which requires a method when bukkit sends a
	 * onDisable() to Denizen. (ie. Server shuts down or restarts)
	 * 
	 */
	public void onDisable() {
	
	}

	public abstract void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException;
	
	public AbstractCommand withOptions(String usageHint, int numberOfRequiredArgs) {
        this.commandOptions = new CommandOptions(usageHint, numberOfRequiredArgs);
        return this;
    }
	
}

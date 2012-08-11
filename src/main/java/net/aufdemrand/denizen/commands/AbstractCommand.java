package net.aufdemrand.denizen.commands;

import java.rmi.activation.ActivationException;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

import org.bukkit.Bukkit;

public abstract class AbstractCommand {

	public Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	public ArgumentRegex aRegex = plugin.getCommandRegistry().getArgumentRegex();
	
	public void echoDebug(String message, String argument) {
		if (plugin.debugMode)
			plugin.getLogger().info(String.format(message, argument));
	}

	public void echoError(String message) {
			plugin.getLogger().log(Level.WARNING, message);
	}
	
	public String getModifier(String argument) {
		if (argument.split(":").length >= 2)
			return argument.split(":")[1];
		else return argument;
	}
	
	public Integer getIntegerModifier(String argument) {
		if (argument.split(":").length >= 2)
			return Integer.valueOf(argument.split(":")[1]);
		else return Integer.valueOf(argument);
	}
	
	
	/* Activates the command class as a Denizen Command. Should be called on startup. */
	
	public void activateAs(String commandName) throws ActivationException {
	
		/* Use Bukkit to reference Denizen Plugin */
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		/* Register command with Registry */
		if (plugin.getCommandRegistry().registerCommand(commandName, this)) return;
		else 
			throw new ActivationException("Error activating Command with Command Registry.");
	}


	
	/* Execute is the method called when the Denizen Command is called from a script.
	 * If the command runs successfully, the method should return true. */

	public abstract boolean execute(ScriptEntry theCommand) throws CommandException;

}

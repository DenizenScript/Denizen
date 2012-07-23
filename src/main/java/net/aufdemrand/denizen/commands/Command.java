package net.aufdemrand.denizen.commands;

import java.rmi.activation.ActivationException;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scriptEngine.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

import org.bukkit.Bukkit;

public abstract class Command {

	public Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	
	/* Activates the command class as a Denizen Command. Should be called on startup. */
	
	public void activateAs(String commandName) throws ActivationException {
	
		/* Use Bukkit to reference Denizen Plugin */
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		/* Register command with Registry */
		if (plugin.commandRegistry.registerCommand(commandName, this)) return;
		else 
			throw new ActivationException("Error activating Command with Command Registry.");
	}


	
	/* Execute is the method called when the Denizen Command is called from a script.
	 * If the command runs successfully, the method should return true. */

	public abstract boolean execute(ScriptEntry theCommand) throws CommandException;

}

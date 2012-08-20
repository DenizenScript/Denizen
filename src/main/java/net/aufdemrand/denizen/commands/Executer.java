package net.aufdemrand.denizen.commands;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

public class Executer {

	
	private CommandSender cs = null;
	private Denizen plugin;

	public Executer(Denizen denizen) {
		plugin = denizen;
	}


	/*
	 * Executes a command defined in theCommand 
	 */

	public boolean execute(ScriptEntry theCommand) {
		
		cs = plugin.getServer().getConsoleSender();
		
		if (plugin.getCommandRegistry().getCommand(theCommand.getCommand()) != null) {

			AbstractCommand command = plugin.getCommandRegistry().getCommand(theCommand.getCommand());

			if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Executing command: " + theCommand.getCommand() + "/" + theCommand.getPlayer().getName() + " -+");

			try {
				if (command.execute(theCommand)) {
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + "Command has reported success.");
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
				}
				else {
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + "Command has reported an error! Check syntax.");
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
					}
			} catch (Exception e) {
				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.RED + "SEVERE! " + ChatColor.WHITE + "Command has called an exception! /denizen stacktrace for more.");
				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, e.getMessage()); 
				if (plugin.showStackTraces) e.printStackTrace();
			}
			return true;

		}

		else {
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Executing command " + theCommand.getCommand());
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...invalid Command!");
		}
		return false;

	}



}
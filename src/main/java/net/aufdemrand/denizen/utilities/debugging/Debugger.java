package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.Denizen;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Debugger {

	Denizen plugin;
	
	ConsoleSender cs = new ConsoleSender();
	
	public boolean debugMode = true;
	public boolean showStackTraces = true;

	public Debugger(Denizen plugin) {
		this.plugin = plugin;
	}

	public enum DebugElement {
		Header,	Footer, Spacer
	}
	
	private class ConsoleSender {
		
		CommandSender commandSender = null;
		
		public void sendMessage(String string) {
			if (commandSender == null) commandSender = plugin.getServer().getConsoleSender(); 
			String[] words = string.split(" ");
			String buffer = "";
			int modifier = 1;
			for (String word : words) {
				if (buffer.length() + word.length() < (80 * modifier) - 12) buffer = buffer + word + " ";
				else {
					modifier++;
					buffer = buffer + "\n" + "                   " + word + " ";
				}
			}
			commandSender.sendMessage(buffer);
		}
		
	}
	
	public enum Messages {
		
		ERROR_NO_NPCID ("No 'NPCID:#' argument."),
		ERROR_NO_PLAYER ("No 'PLAYER:player_name' argument."),
		ERROR_NO_TEXT("No 'TEXT' argument."),
		ERROR_NO_PLAYER_IN_SCRIPTENTRY("No Player object in the ScriptEntry. Use 'PLAYER:player_name' argument."),
		ERROR_INVALID_ANCHOR("Missing 'TEXT' argument."),
		ERROR_INVALID_NOTABLE("Missing 'TEXT' argument."),
		ERROR_MISSING_LOCATION("Missing 'LOCATION'."),
		ERROR_NO_SCRIPT("Missing 'SCRIPT' argument."),
		
		ERROR_MISSING_OTHER("Missing '%s' argument."),
		
		ERROR_LOTS_OF_ARGUMENTS("Woah!! Seems like there are a lot of arguments. Did you forget to use quotes?"), 
		ERROR_UNKNOWN_ARGUMENT("Woah!! Unknown argument! Check syntax."),
		
		DEBUG_SET_TEXT("...set TEXT: '%s'"),
		DEBUG_TOGGLE("...TOGGLE: '%s'"), 
		DEBUG_SET_DURATION("...set DURATION: '%s'"), 
		DEBUG_SET_GLOBAL("...set GLOBAL."), 
		DEBUG_SET_SCRIPT("...set SCRIPT: '%s'"), 
		DEBUG_SET_QUANTITY("...set QUANTITY: '%s'"), 
		DEBUG_SET_LOCATION("...set LOCATION: '%s'"),
		DEBUG_SET_TYPE("...set TYPE: '%s'"),
		DEBUG_SET_RANGE("...set RANGE: '%s'"),
		DEBUG_SET_ITEM("...set ITEM: '%s'"),
		DEBUG_SET_NAME("...set NAME: '%s'"),
		DEBUG_SET_RADIUS("...set RADIUS: '%s'"),
		DEBUG_SET_COOLDOWN("...set COOLDOWN: '%s'"),
		DEBUG_SET_COMMAND("...set COMMAND: '%s'"),
		DEBUG_SET_FLAG_ACTION("...set FLAG ACTION: '%s'"),
		DEBUG_SET_FLAG_TYPE("...set FLAG TYPE: '%s'"), 
		ERROR_INVALID_ITEM("Invalid item!"), 
		ERROR_PLAYER_NPCS_ONLY("NPC must be Human (Player)!"), 
		DEBUG_SET_STEP("...set STEP: '%s'"), 
		DEBUG_RANDOMLY_SELECTED_STEP("...randomly selected step '%s'"), 
		ERROR_CANCELLING_DELAYED_TASK("Unable to cancel previously delayed task!"), 
		DEBUG_RUNNING_DELAYED_TASK(ChatColor.YELLOW + "// DELAYED // Running delayed task '%s'"), 
		DEBUG_SETTING_DELAYED_TASK(ChatColor.YELLOW + "// DELAYED // Setting delayed task '%s'"), 
		ERROR_NO_WORLD("No valid world specified!"), 
		ERROR_INVALID_WORLD("Invalid world!"); 
		
		@Override
		public String toString() {
			return error;
		}
		
		private String error;
		
		private Messages(String error) {
            this.error = error;
		}
    }

	/*
	 *  Communicate with the Logger
	 */

	public void echoDebug(DebugElement element) {
		echoDebug(element, null);
	}
	
	public void echoDebug(Messages message, String arg) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + String.format(message.toString(), arg));
	}
	
	public void echoDebug(DebugElement element, String string) {
		if (!debugMode) return;
		if (element == DebugElement.Footer) 
			cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
		else if (element == DebugElement.Header)
			cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- " + string + " ------+");
		else if (element == DebugElement.Spacer)
			cs.sendMessage(ChatColor.LIGHT_PURPLE + "");
	}
	
	public void echoDebug(String message, String arg) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + String.format(message, arg));
	}

	public void echoDebug(String message) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + message);
	}

	public void echoApproval(String message, String arg) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + String.format(message, arg));
	}

	public void echoApproval(String message) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + message);
	}
	
	public void echoError(String message) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + message);
	}

	public void echoError(Messages message) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + message.toString());
	}

	public void echoError(Messages message, String arg) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + String.format(message.toString(), arg));
	}
	
	public void echoError(String message, String arg) {
		if (!debugMode) return;
		cs.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + String.format(message, arg));
	}

	/* 
	 * These methods do NOT require DebugMode to be enabled 
	 */
	
	public void log(String message, String arg) {
		cs.sendMessage(ChatColor.YELLOW + "+> " + ChatColor.WHITE + String.format(message, arg));
	}

	public void log(String message) {
		cs.sendMessage(ChatColor.YELLOW + "+> " + ChatColor.WHITE + message);
	}

	public void notify(Player player, String message, String arg) {
		player.sendMessage(ChatColor.YELLOW + "+> " + ChatColor.WHITE + String.format(message, arg));
	}

	public void notify(Player player, String message) {
		player.sendMessage(ChatColor.YELLOW + "+> " + ChatColor.WHITE + ChatColor.WHITE + message);
	}

    public void toggle() {
        debugMode = !debugMode;
    }
	
	
}
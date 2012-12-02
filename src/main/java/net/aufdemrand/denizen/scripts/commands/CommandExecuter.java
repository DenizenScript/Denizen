package net.aufdemrand.denizen.scripts.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ScriptEntryExecuteEvent;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.Debugger;
import net.aufdemrand.denizen.utilities.debugging.Debugger.DebugElement;
import net.citizensnpcs.api.CitizensAPI;

public class CommandExecuter {

	private Denizen plugin;
	private Debugger dB;

	public CommandExecuter(Denizen denizen) {
		plugin = denizen;
		dB = plugin.getDebugger();

	}

	/*
	 * Executes a command defined in scriptEntry 
	 */

	public boolean execute(ScriptEntry scriptEntry) {
		if (plugin.getCommandRegistry().get(scriptEntry.getCommand()) == null) {
			dB.echoError(scriptEntry.getCommand() + " is an invalid dScript command!");
			dB.echoError("   Are you sure the command loaded?");
			return false;
		}

		// Get the command instance ready for the execution of the scriptEntry
		AbstractCommand command = plugin.getCommandRegistry().get(scriptEntry.getCommand());

		// Debugger information
		if (scriptEntry.getPlayer() != null) dB.echoDebug(DebugElement.Header, "Executing command: " + scriptEntry.getCommand() + "/" + scriptEntry.getPlayer().getName());
		else dB.echoDebug(DebugElement.Header, "Executing command: " + scriptEntry.getCommand() + "/" + scriptEntry.getDenizen().getName());

		try {

			// Throw exception if arguments are required for this command, but not supplied.
			if (command.getOptions().REQUIRED_ARGS > scriptEntry.getArguments().size()) throw new InvalidArgumentsException("");

			/*  If using NPCID:# or PLAYER:Name arguments, these need to be changed out immediately because...
			 *  1) Denizen/Player flags need the desired NPC/PLAYER before parseArgs's getFilledArguments() so that
		  	 *     the Player/Denizen flags will read from the correct Object. If using PLAYER or NPCID arguments,
			 *     the desired Objects are obviously not the same objects that were sent with the ScriptEntry.
			 *  2) These arguments should be valid for EVERY ScriptCommand, so why not just take care of it
			 *     here, instead of requiring each command to take care of the argument.
			 */

			scriptEntry.setArguments(plugin.tagManager().fillArguments(scriptEntry.getArguments(), scriptEntry)); // Replace tags
			
			List<String> newArgs = new ArrayList<String>(); 

			for (String arg : scriptEntry.getArguments()) {

				// Fill player/off-line player
				if (arg.startsWith("PLAYER:")) {
					boolean foundNewPlayer = false;
					dB.echoDebug("...replacing the Player link.");
					for (Player playa : Bukkit.getServer().getOnlinePlayers())
						if (playa.getName().equalsIgnoreCase(arg.split(":")[1])) {
							foundNewPlayer = true;
							scriptEntry.setPlayer(playa);
						}
					if (foundNewPlayer) dB.echoDebug("...player set to '%s'.", arg.split(":")[1]);
					else {
						dB.echoDebug("This player is not online! Searching for offline player...");
						for (OfflinePlayer playa : Bukkit.getServer().getOfflinePlayers())
							if (playa.getName().equalsIgnoreCase(arg.split(":")[1])) {
								scriptEntry.setPlayer(null);
								scriptEntry.setOfflinePlayer(playa);
								foundNewPlayer = true;
							}
					}
					if (foundNewPlayer) {
					    dB.echoDebug("Found an offline player with this name!");
					    dB.echoDebug("   Saving offlinePlayer object.");
					}
					else { dB.echoError("Could not find a valid player!"); scriptEntry.setPlayer(null); }
					continue;	
				}

				// Fill Denizen with NPCID
				else if (arg.startsWith("NPCID:")) {
					dB.echoDebug("...replacing the NPCID link.");
					try {
						if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(arg.split(":")[1])) != null)
							scriptEntry.setDenizen(plugin.getNPCRegistry().getDenizen(CitizensAPI.getNPCRegistry().getById(Integer.valueOf(arg.split(":")[1]))));
						dB.echoDebug("...DenizenNPC set to '%s'.", arg.split(":")[1]);
					} catch (Exception e) {
						dB.echoError("NPCID specified could not be matched to a Denizen!");
						scriptEntry.setDenizen(null);
					}

				}

				else {
					newArgs.add(arg);
				}
			}

			// Add the arguments back to the scriptEntry.
			scriptEntry.setArguments(newArgs);

			// Parse the rest of the arguments for execution. 
			command.parseArgs(scriptEntry);
		}	catch (Exception e) {

			// Give usage hint if InvalidArgumentsException was called.
			if (e instanceof InvalidArgumentsException) {
				dB.echoError("Woah! Invalid arguments were specified!");
				dB.echoError(command.getUsageHint());
				dB.echoDebug(DebugElement.Footer);

			} else { // Else, print stack-trace (if enabled in Debugger)
				dB.echoError("Woah! An exception has been called with this command!");
				if (!dB.showStackTraces)
					dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
				else e.printStackTrace(); 
				dB.echoDebug(DebugElement.Footer);
				
			} 
		} finally {
			try {
                // Fire event for last minute cancellation/alterations
                ScriptEntryExecuteEvent event = new ScriptEntryExecuteEvent(scriptEntry.getPlayer(), scriptEntry);
                Bukkit.getServer().getPluginManager().callEvent(event);
                
                // If event is altered, update the scriptEntry.
                if (event.isAltered()) scriptEntry = event.getScriptEntry();
                    
				// Run the execute method in the command
                if (!event.isCancelled()) command.execute(scriptEntry.getCommand());
                
                else dB.echoDebug("ScriptEntry has been cancelled.");
			} catch (Exception e) {
				dB.echoError("Woah!! An exception has been called with this command!");
				if (!dB.showStackTraces)
					dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
				else e.printStackTrace(); 
			}
		}
		dB.echoDebug(DebugElement.Footer);

		return true;

	}

}
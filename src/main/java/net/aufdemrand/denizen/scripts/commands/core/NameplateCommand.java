/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.aufdemrand.denizen.scripts.commands.core;

import java.util.List;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.NameplateTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

/**
 * Modifies the nameplate of the given NPC
 * 
 * @author spaceemotion
 */
public class NameplateCommand extends AbstractCommand {

	/* Example usge:
	 * - NAMEPLATE COLOR:RED
	 * - NAMEPLATE COLOR:GOLD PLAYER:Notch
	 * 
	 * Arguments: [] - Required, () - Optional
	 * 
	 * [COLOR] The color to set. See the Bukkit documentation for available colors.
	 * 
	 * (PLAYER) The player to apply the change to (can be per-player!).
	 * 
	 */
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		dNPC npc = scriptEntry.getNPC();
		
		if(npc.getCitizen().hasTrait(NameplateTrait.class)) {
			ChatColor color = null;
			String playerName = null;
			
			List<String> args = scriptEntry.getArguments();
			
			for(String arg : args) {
				if(aH.matchesValueArg("COLOR", arg, ArgumentType.String)) {
					String cString = aH.getStringFrom(arg).toUpperCase();
					
					try {
						color = ChatColor.valueOf(cString.toUpperCase());
						dB.echoDebug("...COLOR set: '%s'", cString);
					} catch( Exception e)  {
						dB.echoDebug("...COLOR could not be set: '%s' is an invalid color!", cString);
					}
				} else if(aH.matchesValueArg("PLAYER", arg, ArgumentType.String)) {
					playerName = aH.getStringFrom(arg);
					dB.echoDebug("...PLAYER set: '%s'", arg);
				}
			}
			
			scriptEntry.addObject("color", color);
			scriptEntry.addObject("player", playerName);
		}
	}
	
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		dNPC npc = scriptEntry.getNPC();
		
		ChatColor color = (ChatColor) scriptEntry.getObject("color");
		
		if(color != null && npc.getCitizen().hasTrait(NameplateTrait.class)) {
			NameplateTrait trait = npc.getCitizen().getTrait(NameplateTrait.class);	
			String playerName = (String) scriptEntry.getObject("player");
			
			if(playerName != null) {
				trait.setColor(color, playerName);
			} else {
				trait.setColor(color);
			}
		}
	}
	
}

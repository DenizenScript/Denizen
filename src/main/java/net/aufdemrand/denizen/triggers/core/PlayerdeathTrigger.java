package net.aufdemrand.denizen.triggers.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerdeathTrigger extends net.aufdemrand.denizen.triggers.AbstractTrigger  implements Listener {

	CommandSender cs;

	@EventHandler
	public void onDeath(PlayerDeathEvent event){

		Player thePlayer = event.getEntity();
		List<DenizenNPC> denizenList = new ArrayList<DenizenNPC>(plugin.getDenizenNPCRegistry().getDenizens().values());

		ScriptHelper sE = plugin.getScriptEngine().helper;
		if (cs == null) cs = Bukkit.getConsoleSender();

		for (DenizenNPC thisDenizen : denizenList) {

			// Skip this Denizen if it isn't spawned, is null, or not in the same World as the Player.
			if (thisDenizen != null && thisDenizen.isSpawned() && thisDenizen.getLocation().getWorld() != thePlayer.getLocation().getWorld()) continue;

			// Check if the PlayerDeath trigger is enabled
			if (thisDenizen.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled("Playerdeath")) { 

				// Get Interact Script
				String theScriptName = thisDenizen.getInteractScript(thePlayer, PlayerdeathTrigger.class);

				// No interact script? Next Denizen!
				if (theScriptName == null) {
					continue;
				}

				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing PlayerDeath trigger: " + thisDenizen.getName() + "/" + thePlayer.getName() + " -+");
				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting current step:");
				Integer theStep = sE.getCurrentStep(thePlayer, theScriptName);

				// Get radius
				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Getting radius:");
				String sradius = plugin.getScripts().getString(sE.getTriggerPath(theScriptName, theStep, triggerName) + "Radius");
				if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Radius is '" + sradius + "'.");

				int radius;

				// Invalid radius? Next Denizen!
				try {
					radius = Integer.valueOf(sradius);
				} catch (Exception e) {
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + "Invalid radius!");
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
					continue;
				}

				// Check radius to determine if this should trigger.
				if ( radius == -1 || (thisDenizen.isSpawned() && thisDenizen.getLocation().distance(thePlayer.getLocation()) < radius )){
					List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName ) +  sE.scriptString);
					sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, thisDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);
				}

				else {
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.YELLOW + "NOPE! " + ChatColor.WHITE + "Player is not in range!");
					if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
				}
			}


		}    
	}
}
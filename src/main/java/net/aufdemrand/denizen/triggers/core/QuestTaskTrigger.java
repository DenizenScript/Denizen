package net.aufdemrand.denizen.triggers.core;

import java.util.List;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestTaskTrigger extends AbstractTrigger {

	/* Parses the script for a QUESTS "finish step" trigger */
	public boolean parseQuestTaskTrigger(String theScriptName, Player thePlayer) {

		CommandSender cs = Bukkit.getConsoleSender();
		ScriptHelper sE = plugin.getScriptEngine().helper;

		if (theScriptName == null) return false; // Should probably provide some feedback from whatever is calling this method if this returns false.

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Parsing QUESTS task trigger: " + thePlayer.getName() + " -+");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "INFO: Attempting to run TASK script '" + theScriptName + "'.");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "Maybe you could put some information on what exactly triggered the");
		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.WHITE + "script here. Denizen users really rely on this input to debug scripts."); 
        if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
		
		/* Get the contents of the Script. */
		List<String> theScript = sE.getScript(theScriptName + ".Script");

		if (theScript.isEmpty()) return false; // Should probably provide some feedback from whatever is calling this method if this returns false.

		/* Build scriptEntries from theScript and add it into the queue */
		sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theScript, theScriptName), QueueType.TASK);

		// That's it!
		return true;
	}

}

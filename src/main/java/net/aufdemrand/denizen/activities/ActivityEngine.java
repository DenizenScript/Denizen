package net.aufdemrand.denizen.activities;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;

public class ActivityEngine {

	Denizen plugin;
	CommandSender cs = null;

	public ActivityEngine(Denizen denizen) {
		this.plugin = denizen;
	}


	/* 
	 * Schedules activity scripts to Denizens based on their schedule defined in the config.
	 * Runs every 30 seconds, but can only schedule on the per Minecraft hour. 
	 * 
     */

	public void scheduleScripts(boolean forceable) {
		
		if (plugin.getDenizenNPCRegistry().getDenizens().isEmpty()) return;

		for (DenizenNPC theDenizen : plugin.getDenizenNPCRegistry().getDenizens().values()) {

			if (forceable) plugin.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script", null);
			if (forceable) plugin.getActivityRegistry().removeAllActivities(theDenizen.getCitizensEntity());
			
			// No need to set activities for un-spawned Denizens.
			if (!theDenizen.isSpawned())
				continue;

			int denizenTime = Math.round(theDenizen.getWorld().getTime() / 1000);
			List<String> denizenActivities = 
					plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities");

			// plugin.getLogger().info("DEBUG: Scheduling for " + denizenTime + ":00");

			if (denizenActivities.isEmpty())
				continue;

			// See if any activities match the time.
			
			for (String activity : denizenActivities) {
				if (activity.startsWith(String.valueOf(denizenTime))) {
					String activityScript = activity.split(" ", 2)[1];

					if (!plugin.getSaves().contains("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script"))
						setActivityScript(theDenizen, activityScript);
						
					else if (!plugin.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script").toUpperCase().equals(activityScript.toUpperCase()))
						setActivityScript(theDenizen, activityScript);
					
				}
			}
		}
	}
	
	
	public void setActivityScript(DenizenNPC theDenizen, String activityScript) {
		
		if (cs == null) cs = plugin.getServer().getConsoleSender();

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+- Updating activity: " + theDenizen.getName() + "/" + theDenizen.getId() + " --------+");

		plugin.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script", activityScript);

		if (!plugin.getScripts().contains(activityScript + ".Activities.List")) {
			if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "| " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + "Tried to load the Activity Script '" + activityScript + "', but it couldn't be found. Perhaps something is spelled wrong, or the script doesn't exist?");
			return;
		}

		// Remove all activities for the DenizenNPC
		plugin.getActivityRegistry().removeAllActivities(theDenizen.getCitizensEntity());

		for (String activity : plugin.getScripts().getStringList(activityScript + ".Activities.List")) {
			String[] arguments = plugin.getScriptEngine().helper.buildArgs(activity.split(" ", 3)[2]);
			int priority = Integer.parseInt(activity.split(" ", 3)[0]);
			activity = activity.split(" ", 3)[1];
			plugin.getActivityRegistry().addActivity(activity, theDenizen.getCitizensEntity(), arguments, priority);
		}

		if (plugin.debugMode) cs.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
		// Cool!

	}
}



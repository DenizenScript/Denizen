package net.aufdemrand.denizen.activities;

import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;

public class ActivityEngine {

	Denizen plugin;

	public ActivityEngine(Denizen denizen) {
		this.plugin = denizen;
	}


	/* Schedules activity scripts to Denizens based on their schedule defined in the config.
	 * Runs every Minecraft hour. 
	 * 
	 * This will be the backbone to automated activity scripts. Currently this is not used
	 * any further than what's in this method, but will be built upon soon.	 */

	public void scheduleScripts() {

		if (plugin.getDenizenNPCRegistry().getDenizens().isEmpty()) return;

		for (DenizenNPC theDenizen : plugin.getDenizenNPCRegistry().getDenizens().values()) {

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

					setActivityScript(theDenizen, activityScript);

				}
			}

		}
	}


	public void setActivityScript(DenizenNPC theDenizen, String activityScript) {

		if (plugin.debugMode) plugin.getLogger().info("Updating activity Script for" + theDenizen.getName());
		plugin.getSaves().set("Denizens." + theDenizen.getName() + ".Active Activity Script", activityScript);

		if (!plugin.getScripts().contains(activityScript + ".Activities.List")) {
			plugin.getLogger().info("Tried to load the Activity Script '" + activityScript + "', but it couldn't be found. Perhaps something is spelled wrong, or the script doesn't exist?");
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

		// Cool!

	}
}



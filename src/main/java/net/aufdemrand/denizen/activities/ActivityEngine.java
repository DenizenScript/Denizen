package net.aufdemrand.denizen.activities;

import java.util.Iterator;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.api.ai.GoalController.GoalEntry;

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
			
			if (denizenActivities.isEmpty())
				continue;

			// See if any activities match the time.
			for (String activity : denizenActivities) {
				if (activity.startsWith(String.valueOf(denizenTime))) {
					String activityScript = activity.split(" ", 2)[1];
					if (!plugin.getSaves().getString("Denizens." + theDenizen.getName() + ".Active Activity Script")
							.equals(activityScript))
						// If so, setActivity for the Denizen!
						setActivityScript(theDenizen, activityScript);
				}
			}

		}
	}


	public void setActivityScript(DenizenNPC theDenizen, String activityScript) {

		plugin.getLogger().info("Updating activity Script for" + theDenizen.getName());
		plugin.getSaves().set("Denizen." + theDenizen.getName() + ".Active Activity Script", activityScript);

		if (!plugin.getScripts().contains(activityScript + ".Activities.List")) {
			plugin.getLogger().info("Tried to load the Activity Script '" + activityScript + "', but it couldn't be found. Perhaps something is spelled wrong, or the script doesn't exist?");
			return;
		}
		
		Iterator<GoalEntry> activeGoal = theDenizen.getCitizensEntity().getDefaultGoalController().iterator();
		// Remove current Goals from the NPC
		while (activeGoal.hasNext()) {
			theDenizen.getCitizensEntity().getDefaultGoalController().removeGoal(activeGoal.next().getGoal());
		}
		
		for (String activity : plugin.getScripts().getStringList(activityScript + ".Activities.List")) {
			String[] arguments = plugin.getScriptEngine().helper.buildArgs(activity.split(" ", 3)[2]);
			int priority = Integer.parseInt(activity.split(" ", 3)[0]);
			activity = activity.split(" ", 3)[1];
			plugin.getActivityRegistry().addActivity(activity, theDenizen.getCitizensEntity(), arguments, priority);
		}
		
		// Cool!

	}
}



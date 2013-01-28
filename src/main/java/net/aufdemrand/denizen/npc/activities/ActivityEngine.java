package net.aufdemrand.denizen.npc.activities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import org.bukkit.event.Listener;

public class ActivityEngine implements Listener {

	final Denizen denizen;

	// TODO: This requires some rewriting before the launch of 0.8.
	
	public ActivityEngine(Denizen denizen) {
		this.denizen = denizen;
	}

	/* 
	 * Schedules activity scripts to Denizens based on their schedule defined in the assignment.
	 * Runs every 30 seconds, but can only schedule on the Minecraft hour. 
	 */

	public void scheduler(boolean forceable) {

//		if (denizen.getNPCRegistry().getSpawnedNPCs().isEmpty()) return;
//
//		for (dNPC theDenizen : denizen.getNPCRegistry().getSpawnedNPCs().values()) {
//
//			// By default, this only sets a new activity if the activity is different than what
//			// is already set. If 'forceable', it will reset the activity either way.
//			if (forceable) {
//				denizen.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script", null);
//				denizen.getActivityRegistry().removeAllActivities(theDenizen.getCitizen());
//				setDefaultActivity(theDenizen);
//				continue;
//			}
//
//			int denizenTime = Math.round(theDenizen.getWorld().getTime() / 1000);
//			List<String> denizenActivities = denizen.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities");
//
//			// No need to continue for NPCs w/o activities.
//			if (denizenActivities.isEmpty())
//				continue;
//
//			// See if any activities match the current time.
//			for (String activity : denizenActivities)
//				if (activity.startsWith(String.valueOf(denizenTime))) {
//					String activityScript = activity.split(" ", 2)[1];
//
//					if (!denizen.getSaves().contains("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script")) {
//						setActivityScript(theDenizen, activityScript);
//						continue;
//					}
//
//					else if (!denizen.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script").toUpperCase().equals(activityScript.toUpperCase())) {
//						setActivityScript(theDenizen, activityScript);
//						continue;
//					}
//				}
//
//		}
	}

	public void setActivityScript(dNPC dNPC, String scriptName) {

		dB.echoDebug(DebugElement.Header, "Updating activity: " + dNPC.getName() + "/" + dNPC.getId());
		denizen.getSaves().set("Denizens." + dNPC.getName() + "." + dNPC.getId() + ".Active Activity Script", scriptName);

		if (!denizen.getScripts().contains(scriptName + ".Activities.List")) {
			dB.echoError("Tried to load the Activity Script '" + scriptName + ".Activities.List', but it couldn't be found. Perhaps something is spelled wrong, or the script doesn't exist?");
			return;
		}

		denizen.getActivityRegistry().removeAllActivities(dNPC.getCitizen());

		for (String activity : denizen.getScripts().getStringList(scriptName + ".Activities.List")) {
			String[] arguments = denizen.getScriptEngine().getScriptBuilder().buildArgs(activity.split(" ", 3)[2]);
			int priority = Integer.parseInt(activity.split(" ", 3)[0]);
			activity = activity.split(" ", 3)[1];
			denizen.getActivityRegistry().addActivity(activity, dNPC, arguments, priority);
		}

		dB.echoDebug(DebugElement.Footer);
	}

}



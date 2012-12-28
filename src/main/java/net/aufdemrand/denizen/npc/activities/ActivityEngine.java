package net.aufdemrand.denizen.npc.activities;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import net.aufdemrand.denizen.utilities.runnables.Runnable1;

import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;

public class ActivityEngine implements Listener {

	Denizen denizen;

	// TODO: This requires some rewriting before the launch of 0.8.
	
	public ActivityEngine(Denizen denizen) {
		this.denizen = denizen;
	}

	@EventHandler
	public void setDefaultActivity(NPCSpawnEvent event) {
		if (event.getNPC().hasTrait(TriggerTrait.class))
			denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen, new Runnable1<NPC>(event.getNPC()) {
				@Override public void run(NPC theNPC) { 
					try { setDefaultActivity(denizen.getNPCRegistry().getDenizen(theNPC)); } 
					catch (Exception e) { if (dB.showStackTraces) e.printStackTrace(); }
				}
			}, 30);
	}

	public void setDefaultActivity(DenizenNPC theDenizen) {
		if (denizen.getAssignments().contains("Denizens." + theDenizen.getName() + ".Default Activity"))
			setActivityScript(theDenizen, denizen.getAssignments().getString("Denizens." + theDenizen.getName() + ".Default Activity"));
	}

	/* 
	 * Schedules activity scripts to Denizens based on their schedule defined in the assignment.
	 * Runs every 30 seconds, but can only schedule on the Minecraft hour. 
	 */

	public void scheduler(boolean forceable) {

		if (denizen.getNPCRegistry().getDenizens().isEmpty()) return;

		for (DenizenNPC theDenizen : denizen.getNPCRegistry().getDenizens().values()) {

			// No need to set activities for despawned Denizens.
			if (!theDenizen.isSpawned()) continue;

			// By default, this only sets a new activity if the activity is different than what
			// is already set. If 'forceable', it will reset the activity either way.
			if (forceable) {
				denizen.getSaves().set("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script", null);
				denizen.getActivityRegistry().removeAllActivities(theDenizen.getCitizen());
				setDefaultActivity(theDenizen);
				continue;
			}

			int denizenTime = Math.round(theDenizen.getWorld().getTime() / 1000);
			List<String> denizenActivities = denizen.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities");

			// No need to continue for NPCs w/o activities.
			if (denizenActivities.isEmpty())
				continue;

			// See if any activities match the current time.
			for (String activity : denizenActivities) 
				if (activity.startsWith(String.valueOf(denizenTime))) {
					String activityScript = activity.split(" ", 2)[1];

					if (!denizen.getSaves().contains("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script")) {
						setActivityScript(theDenizen, activityScript);
						continue;
					}

					else if (!denizen.getSaves().getString("Denizens." + theDenizen.getName() + "." + theDenizen.getId() + ".Active Activity Script").toUpperCase().equals(activityScript.toUpperCase())) {
						setActivityScript(theDenizen, activityScript);
						continue;
					}
				}

		}
	}

	public void setActivityScript(DenizenNPC denizenNPC, String scriptName) {

		dB.echoDebug(DebugElement.Header, "Updating activity: " + denizenNPC.getName() + "/" + denizenNPC.getId());
		denizen.getSaves().set("Denizens." + denizenNPC.getName() + "." + denizenNPC.getId() + ".Active Activity Script", scriptName);

		if (!denizen.getScripts().contains(scriptName + ".Activities.List")) {
			dB.echoError("Tried to load the Activity Script '" + scriptName + ".Activities.List', but it couldn't be found. Perhaps something is spelled wrong, or the script doesn't exist?");
			return;
		}

		denizen.getActivityRegistry().removeAllActivities(denizenNPC.getCitizen());

		for (String activity : denizen.getScripts().getStringList(scriptName + ".Activities.List")) {
			String[] arguments = denizen.getScriptEngine().getScriptBuilder().buildArgs(activity.split(" ", 3)[2]);
			int priority = Integer.parseInt(activity.split(" ", 3)[0]);
			activity = activity.split(" ", 3)[1];
			denizen.getActivityRegistry().addActivity(activity, denizenNPC, arguments, priority);
		}

		dB.echoDebug(DebugElement.Footer);
	}

}



package net.aufdemrand.denizen.activities;

import java.rmi.activation.ActivationException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.activities.core.WanderActivity;
import net.citizensnpcs.api.npc.NPC;


public class ActivityRegistry {

	private Map<String, AbstractActivity> activities = new HashMap<String, AbstractActivity>();
	private Map<Class<? extends AbstractActivity>, String> activitiesClass = new HashMap<Class<? extends AbstractActivity>, String>();

	public Denizen plugin;

	public ActivityRegistry(Denizen denizen) {
		plugin = denizen;
	}

	public boolean registerActivity(String activityName, AbstractActivity activityClass) {
		this.activities.put(activityName.toUpperCase(), activityClass);
		this.activitiesClass.put(activityClass.getClass(), activityName.toUpperCase());
		activityClass.activityName = activityName.substring(0, 1).toUpperCase() + activityName.substring(1).toLowerCase();
		plugin.getLogger().log(Level.INFO, "Loaded " + activityClass.activityName + " Activity successfully!");
		return true;
	}


	public Map<String, AbstractActivity> listTriggers() {
		if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Contents of TriggerList: " + activities.keySet().toString());
		return activities;
	}

	
	public AbstractActivity getActivity(String activityName) {
		if (activities.containsKey(activityName.toUpperCase()))
			return activities.get(activityName.toUpperCase());
		else
			return null;
	}

	public <T extends AbstractActivity> T getActivity(Class<T> theClass) {
		if (activitiesClass.containsKey(theClass))
			return (T) theClass.cast(activities.get(activitiesClass.get(theClass)));
		else
			return null;
	}
	
	
	public void registerCoreActivities() {

		WanderActivity wanderActivity = new WanderActivity();
		
		/* Activate Denizen Triggers */
		try {
			
			wanderActivity.activateAs("WANDER");

		} catch (ActivationException e) {
			plugin.getLogger().log(Level.SEVERE, "Oh no! Denizen has run into a problem registering the core triggers!");
			e.printStackTrace();
		}
	}
	
	
	public void addActivity(String activity, NPC theDenizen, String[] args, int priority) {
		if (activities.containsKey(activity.toUpperCase()))
			activities.get(activity.toUpperCase()).addGoal(plugin.getDenizenNPCRegistry().getDenizen(theDenizen), args, priority);
		else
			plugin.getLogger().log(Level.SEVERE, "'" + activity + "' is an invalid activity!");
	}
	
	public void removeActivity(String activity, NPC theDenizen) {
		if (activities.containsKey(activity.toUpperCase()))
			activities.get(activity.toUpperCase()).removeGoal(plugin.getDenizenNPCRegistry().getDenizen(theDenizen));
		else
			plugin.getLogger().log(Level.SEVERE, "Invalid activity!");
	}
	
	
}

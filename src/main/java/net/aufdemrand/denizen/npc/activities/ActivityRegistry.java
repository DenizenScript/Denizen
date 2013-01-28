package net.aufdemrand.denizen.npc.activities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.activities.core.TaskActivity;
import net.aufdemrand.denizen.npc.activities.core.WanderActivity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ActivityRegistry implements DenizenRegistry {

    public final Denizen denizen;

    private Map<String, AbstractActivity> instances = new HashMap<String, AbstractActivity>();

    private Map<Class<? extends AbstractActivity>, String> classes = new HashMap<Class<? extends AbstractActivity>, String>();
    public ActivityRegistry(Denizen denizenPlugin) {
        denizen = denizenPlugin;
    }

    public void addActivity(String activity, dNPC dNPC, String[] args, int priority) {
        if (instances.containsKey(activity.toUpperCase()))
            instances.get(activity.toUpperCase()).addGoal(dNPC, args, priority);
        else denizen.getLogger().log(Level.SEVERE, "'" + activity + "' is an invalid activity!");
    }

    @Override
	public void disableCoreMembers() {
		for (RegistrationableInstance member : instances.values())
			try { 
				member.onDisable(); 
			} catch (Exception e) {
				dB.echoError("Unable to disable '" + member.getClass().getName() + "'!");
				if (dB.showStackTraces) e.printStackTrace();
			}
	}

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (classes.containsKey(clazz))	return (T) clazz.cast(instances.get(classes.get(clazz)));
        else return null;
    }

    @Override
    public AbstractActivity get(String activityName) {
        if (instances.containsKey(activityName.toUpperCase()))	return instances.get(activityName.toUpperCase());
        else return null;
    }

    @Override
    public Map<String, AbstractActivity> list() {
        return instances;
    }
    
    @Override
    public boolean register(String activityName, RegistrationableInstance activityClass) {
        this.instances.put(activityName.toUpperCase(), (AbstractActivity) activityClass);
        this.classes.put(((AbstractActivity) activityClass).getClass(), activityName.toUpperCase());
        return true;
    }

    @Override
    public void registerCoreMembers() {

        WanderActivity wanderActivity = new WanderActivity();
        TaskActivity taskActivity = new TaskActivity();

        // Activate Denizen Activities
        wanderActivity.activate().as("WANDER");
        taskActivity.activate().as("TASK");

        dB.echoApproval("Loaded core activities: " + instances.keySet().toString());
    }

    public void removeActivity(String activity, NPC denizenNPC) {
        if (instances.containsKey(activity.toUpperCase()))
            instances.get(activity.toUpperCase()).removeGoal(denizen.getNPCRegistry().getDenizen(denizenNPC), true);
        else denizen.getLogger().log(Level.SEVERE, "Invalid activity!");
    }

	public void removeAllActivities(NPC denizenNPC) {
        for (AbstractActivity theActivity : instances.values()) {
            theActivity.removeGoal(denizen.getNPCRegistry().getDenizen(denizenNPC), false);
        }
    }
    
}

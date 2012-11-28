package net.aufdemrand.denizen.npc.activities;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.activities.core.TaskActivity;
import net.aufdemrand.denizen.npc.activities.core.WanderActivity;
import net.citizensnpcs.api.npc.NPC;

public class ActivityRegistry implements DenizenRegistry {

    public Denizen denizen;

    public ActivityRegistry(Denizen denizenPlugin) {
        denizen = denizenPlugin;
    }

    private Map<String, AbstractActivity> instances = new HashMap<String, AbstractActivity>();
    private Map<Class<? extends AbstractActivity>, String> classes = new HashMap<Class<? extends AbstractActivity>, String>();

    @Override
    public boolean register(String activityName, RegistrationableInstance activityClass) {
        this.instances.put(activityName.toUpperCase(), (AbstractActivity) activityClass);
        this.classes.put(((AbstractActivity) activityClass).getClass(), activityName.toUpperCase());
        return true;
    }

    @Override
    public Map<String, AbstractActivity> list() {
        return instances;
    }

    @Override
    public AbstractActivity get(String activityName) {
        if (instances.containsKey(activityName.toUpperCase()))	return instances.get(activityName.toUpperCase());
        else return null;
    }

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (classes.containsKey(clazz))	return (T) clazz.cast(instances.get(classes.get(clazz)));
        else return null;
    }

    @Override
    public void registerCoreMembers() {

        WanderActivity wanderActivity = new WanderActivity();
        TaskActivity taskActivity = new TaskActivity();

        // Activate Denizen Activities
        wanderActivity.activate().as("WANDER");
        taskActivity.activate().as("TASK");

        denizen.getDebugger().echoApproval("Loaded core activities: " + instances.keySet().toString());

        // Activate Listeners for
        
    }

    
    
    public void addActivity(String activity, DenizenNPC denizenNPC, String[] args, int priority) {
        if (instances.containsKey(activity.toUpperCase()))
            instances.get(activity.toUpperCase()).addGoal(denizenNPC, args, priority);
        else denizen.getLogger().log(Level.SEVERE, "'" + activity + "' is an invalid activity!");
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

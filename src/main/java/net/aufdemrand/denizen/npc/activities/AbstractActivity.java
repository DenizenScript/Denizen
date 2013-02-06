package net.aufdemrand.denizen.npc.activities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.npc.dNPC;
import org.bukkit.Bukkit;

public abstract class AbstractActivity implements RegistrationableInstance {

	/**
	 * Contains required options for an Activity in a single class for the
	 * ability to add optional options in the future.
	 *
	 */
    public class ActivityOptions { 
        public String USAGE_HINT = ""; 
        public int REQUIRED_ARGS = -1;

        public ActivityOptions(String usageHint, int numberOfRequiredArgs) {
            this.USAGE_HINT = usageHint;
            this.REQUIRED_ARGS = numberOfRequiredArgs;
        }
    }

    public Denizen denizen;
    protected String name;

    public ActivityOptions activityOptions;
    
    @Override
    public AbstractActivity activate() {
        this.denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
        return this;
    }

    abstract public boolean addGoal(dNPC npc, String[] arguments, int priority);

    @Override
    public AbstractActivity as(String activityName) {
        this.name = activityName.toUpperCase();
        denizen.getActivityRegistry().register(activityName, this);
        onEnable();
        return this;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public ActivityOptions getOptions() {
        return activityOptions;
    }
    
    public String getUsageHint(String commandName) {
        return !activityOptions.USAGE_HINT.equals("") ? activityOptions.USAGE_HINT : "No usage defined! See documentation for more information!";
    }

    
    /**
	 * Part of the Plugin disable sequence.
	 * 
	 * Can be '@Override'n by a Command which requires a method when bukkit sends a
	 * onDisable() to Denizen. (ie. Server shuts down or restarts)
	 * 
	 */
	public void onDisable() {
	
	}

    abstract public boolean removeGoal(dNPC npc, boolean verbose);

	public AbstractActivity withOptions(String usageHint, int numberOfRequiredArgs) {
        this.activityOptions = new ActivityOptions(usageHint, numberOfRequiredArgs);
        return this;
    }

}

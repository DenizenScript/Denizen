package net.aufdemrand.denizen.npc.activities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper;
import net.aufdemrand.denizen.scripts.helpers.ScriptHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Bukkit;

public abstract class AbstractActivity implements RegistrationableInstance {

    public Denizen denizen;

    protected ArgumentHelper aH;
    protected ScriptHelper sH;
    protected ScriptBuilder sB;

    protected String name;
    public ActivityOptions activityOptions = new ActivityOptions();

    public class ActivityOptions { 
        public String USAGE_HINT = ""; 
        public int REQUIRED_ARGS = -1;

        public ActivityOptions() { }
        
        public ActivityOptions(String usageHint, int numberOfRequiredArgs) {
            this.USAGE_HINT = usageHint;
            this.REQUIRED_ARGS = numberOfRequiredArgs;
        }
    }
    
    @Override
    public AbstractActivity activate() {
        this.denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");

        // Reference Helper Classes
        aH = denizen.getScriptEngine().getArgumentHelper();
        sH = denizen.getScriptEngine().getScriptHelper();
        sB = denizen.getScriptEngine().getScriptBuilder();
        return this;
    }

    @Override
    public AbstractActivity as(String activityName) {
        this.name = activityName.toUpperCase();
        denizen.getActivityRegistry().register(activityName, this);
        onEnable();
        return this;
    }

    public AbstractActivity withOptions(String usageHint, int numberOfRequiredArgs) {
        this.activityOptions = new ActivityOptions(usageHint, numberOfRequiredArgs);
        return this;
    }
    
    public ActivityOptions getOptions() {
        return activityOptions;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public String getUsageHint(String commandName) {
        return !activityOptions.USAGE_HINT.equals("") ? activityOptions.USAGE_HINT : "No usage defined! See documentation for more information!";
    }

    
    abstract public boolean addGoal(DenizenNPC npc, String[] arguments, int priority);

    abstract public boolean removeGoal(DenizenNPC npc, boolean verbose);


}

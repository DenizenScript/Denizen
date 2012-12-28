package net.aufdemrand.denizen.scripts.requirements;

import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper;
import net.aufdemrand.denizen.scripts.helpers.ScriptHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class AbstractRequirement implements RegistrationableInstance {

	protected Denizen plugin;
	
	protected ArgumentHelper aH;
	protected ScriptHelper sH;
	protected ScriptBuilder sB;

	protected String name;
    public RequirementOptions requirementOptions = new RequirementOptions();

    public class RequirementOptions { 
        public String USAGE_HINT = ""; 
        public int REQUIRED_ARGS = -1;

        public RequirementOptions() { }
        
        public RequirementOptions(String usageHint, int numberOfRequiredArgs) {
            this.USAGE_HINT = usageHint;
            this.REQUIRED_ARGS = numberOfRequiredArgs;
        }
    }
	
    @Override
	public AbstractRequirement activate() {
		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		// Reference Helper Classes
		aH = plugin.getScriptEngine().getArgumentHelper();
		sH = plugin.getScriptEngine().getScriptHelper();
		sB = plugin.getScriptEngine().getScriptBuilder();
		return this;
	}
	
    @Override
	public AbstractRequirement as(String requirementName) {
	    // Register command with Registry
		plugin.getRequirementRegistry().register(requirementName, this);
		onEnable();
		return this;
	}
    
    public AbstractRequirement withOptions(String usageHint, int numberOfRequiredArgs) {
        this.requirementOptions = new RequirementOptions(usageHint, numberOfRequiredArgs);
        return this;
    }
    
    public RequirementOptions getOptions() {
        return requirementOptions;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public String getUsageHint() {
        return !requirementOptions.USAGE_HINT.equals("") ? requirementOptions.USAGE_HINT : "No usage defined! See documentation for more information!";
    }

	public abstract boolean check(Player player, DenizenNPC npc, String scriptName, List<String> args) throws RequirementCheckException;

	/**
	 * Part of the Plugin disable sequence.
	 * 
	 * Can be '@Override'n by a Requirement which requires a method when bukkit sends a
	 * onDisable() to Denizen. (ie. Server shuts down or restarts)
	 * 
	 */
	public void onDisable() {
	
	}

}

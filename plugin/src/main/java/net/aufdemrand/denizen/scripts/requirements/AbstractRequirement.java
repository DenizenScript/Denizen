package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizencore.interfaces.RegistrationableInstance;
import org.bukkit.Bukkit;

import java.util.List;

public abstract class AbstractRequirement implements RegistrationableInstance {

    /**
     * Contains required options for a Requirement in a single class for the
     * ability to add optional options in the future.
     */
    public class RequirementOptions {
        public String USAGE_HINT = "";
        public int REQUIRED_ARGS = -1;

        public RequirementOptions(String usageHint, int numberOfRequiredArgs) {
            this.USAGE_HINT = usageHint;
            this.REQUIRED_ARGS = numberOfRequiredArgs;
        }
    }

    protected Denizen plugin;
    protected String name;

    public RequirementOptions requirementOptions;

    @Override
    public AbstractRequirement activate() {
        plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
        return this;
    }

    @Override
    public AbstractRequirement as(String requirementName) {
        this.name = requirementName;

        // Register command with Registry
        plugin.getRequirementRegistry().register(requirementName, this);
        onEnable();
        return this;
    }

    public abstract boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException;

    @Override
    public String getName() {
        return name;
    }

    public RequirementOptions getOptions() {
        return requirementOptions;
    }

    public String getUsageHint() {
        return !requirementOptions.USAGE_HINT.equals("") ? requirementOptions.USAGE_HINT : "No usage defined! See documentation for more information!";
    }

    /**
     * Part of the Plugin disable sequence.
     * <p/>
     * Can be '@Override'n by a Requirement which requires a method when bukkit sends a
     * onDisable() to Denizen. (ie. Server shuts down or restarts)
     */
    public void onDisable() {

    }

    /**
     * Part of the Plugin enable sequence. This is called when the requirement is
     * instanced by the RequirementRegistry, which is generally on a server startup.
     * <p/>
     * Can be '@Override'n by a Requirement which requires a method when starting, such
     * as registering as a Bukkit Listener.
     */
    public void onEnable() {

    }

    public AbstractRequirement withOptions(String usageHint, int numberOfRequiredArgs) {
        this.requirementOptions = new RequirementOptions(usageHint, numberOfRequiredArgs);
        return this;
    }
}

package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InteractScriptContainer extends ScriptContainer {

    public InteractScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        for (String step : getConfigurationSection("STEPS").getKeys(false)) {
            if (step.contains("*")) {
                ConfigurationSection defaultStepSection = getConfigurationSection("STEPS." + step);
                step = step.replace("*", "");
                set("STEPS." + step, defaultStepSection);
                set("STEPS." + step + "*", null);
                defaultStep = step;
            }
            if (step.equalsIgnoreCase("1")) defaultStep = step;
            if (step.equalsIgnoreCase("DEFAULT")) defaultStep = step;
            steps.add(step);
        }
    }

    private String defaultStep;
    private List<String> steps;

    public List<String> getStepNames() {
        return steps;
    }

    public String getDefaultStepName() {
        return defaultStep;
    }

    public boolean containsTriggerInStep(String step, Class<? extends AbstractTrigger> trigger) {
        String triggerName = DenizenAPI.getCurrentInstance().getTriggerRegistry().get(trigger).getName().toUpperCase();
        if (contains("STEPS." + step.toUpperCase() + "." + triggerName + " TRIGGER"))
            return true;
        else return false;
    }

    public List<ScriptEntry> getEntriesForTrigger(Player player, dNPC npc, String step, Class<? extends AbstractTrigger> trigger, String id) {
        String triggerName = DenizenAPI.getCurrentInstance().getTriggerRegistry().get(trigger).getName().toUpperCase();
        if (contains("STEPS." + triggerName + " TRIGGER." + (id == null ? "SCRIPT" : "ID.SCRIPT")))
            return getEntries(player, npc, "STEPS." + triggerName + " TRIGGER." + (id == null ? "SCRIPT" : "ID.SCRIPT"));
        else return new ArrayList<ScriptEntry>();
    }

}

package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class InteractScriptContainer extends ScriptContainer {

    public InteractScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);

        try {
            // Find steps/default step in the script
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String defaultStep;
    private List<String> steps = new ArrayList<String>();

    public List<String> getStepNames() {
        return steps;
    }

    /**
     * <p>Gets the name of the default step for this interact script container. Default step
     * is specified by a '*' character on the end of the step name.</p>
     *
     * <b>Example:</b>
     * <tt>
     * Example Interact Script: <br/>
     *   Type: interact <br/>
     *   Steps: <br/>
     *     Step Name*:   <--- Default step for this interact script <br/>
     *       ...  <br/>
     * <br/>
     *     Another Step Name:  <--- Not the default step, must use ZAP <br/>
     *       ...  <br/>
     * </tt>
     *
     * <p>Note: For the sake of compatibility with v0.76, a step named '1' can also
     * be used to specify a default step.</p>
     *
     * @return name of the default step
     *
     */
    public String getDefaultStepName() {
        return defaultStep;
    }

    /**
     * Checks if the step in this script contains an entry for the specified trigger.
     *
     * @param step  the name of the step to check
     * @param trigger  the trigger to check for
     *
     * @return true if the trigger is present in the step, false otherwise
     *
     */
    public boolean containsTriggerInStep(String step, Class<? extends AbstractTrigger> trigger) {
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();

        if (contains("STEPS." + step.toUpperCase() + "." + triggerName + " TRIGGER"))
            return true;

        else return false;
    }

    /**
     * <p>Gets a list of ScriptEntries from a Trigger's script key. In order to get the correct
     * entries, pass along the id of the exact script for the Trigger, which should be identified
     * by using getPossibleTriggersFor(). If no id is specified by passing null, the
     * base Script key will be used for the Trigger. If no script matches the trigger or
     * trigger/id combination, an empty list is returned.</p>
     *
     * <b>Example:</b>
     * <tt>
     * Example Interact Script: <br/>
     *   Type: interact <br/>
     *   Steps: <br/>
     *     Current Step:        <--- checked with Player object <br/>
     *       Click Trigger:     <--- obtained with the Trigger class <br/>
     * <br/>
     *         id:              <--- id of the specific trigger script/script options <br/>
     *           Script: <br/>
     *           - ...          <--- entries obtained if id matches <br/>
     *           - ... <br/>
     * <br/>
     *         Script:          <--- script that is referenced if NO id is specified <br/>
     *         - ...            <--- entries returned <br/>
     *         - ... <br/>
     * </tt>
     *
     * <p>Note: This is handled internally with the parse() method in AbstractTrigger, so for
     * basic triggers, you probably don't need to even call this.</p>
     *
     * @param trigger  the class of the Trigger to use
     * @param player  the Player involved
     * @param npc  the NPC involved
     * @param id  the id of the Trigger Script, optional
     *
     * @return  a list of ScriptEntries from the script or an empty list if no script was found
     *
     */
    public List<ScriptEntry> getEntriesFor(Class<? extends AbstractTrigger> trigger,
                                           Player player, dNPC npc, String id) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Check for entries
        if (contains("STEPS." + InteractScriptHelper.getCurrentStep(player, getName()) + "."
                + triggerName + " TRIGGER."
                + (id == null ? "SCRIPT" : id.toUpperCase() + ".SCRIPT"))) {
            // Entries exist, so get them and return the list of ScriptEntries
            return getEntries(player, npc,
                    "STEPS." + InteractScriptHelper.getCurrentStep(player, getName()) + "."
                            + triggerName + " TRIGGER."
                            + (id == null ? "SCRIPT" : id.toUpperCase() + ".SCRIPT"));
            // No entries, so just return an empty list to avoid NPEs
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Gets the available IDs with its trigger value in the form of a Map. The 'key' is
     * the name of the ID and the value is the value of the 'Trigger' key that is owned
     * by the ID key.
     *
     * <b>Example:</b>
     * <tt>
     * Example Interact Script: <br/>
     *   Type: interact <br/>
     *   Steps: <br/>
     *     Current Step:
     *       Click Trigger:           <--- obtained with the Trigger class <br/>
     * <br/>
     *         id:                    <--- id of the specific trigger script/script options <br/>
     *           Trigger: iron_sword  <--- value of the id key <br/>
     *           Script: <br/>
     *           - ...
     *           - ... <br/>
     * <br/>
     *         Script:                <--- since this is an id-less entry for the click trigger, <br/>
     *         - ...                       it will be ignored and not in the Map. <br/>
     *         - ... <br/>
     * </tt>
     *
     * <p>Note: This is handled internally with the parse() method in AbstractTrigger, so for
     * basic triggers, you probably don't need to even call this.</p>
     *
     * @param trigger
     * @param player
     * @return
     */

    public Map<String, String> getIdMapFor(Class<? extends AbstractTrigger> trigger,
                                           Player player) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        // Check for entries
        if (contains("STEPS." + step + "." + triggerName + " TRIGGER")) {
            // Trigger exists in Player's current step, get ids.
            Map<String, String> idMap = new HashMap<String, String>();
            // Iterate through IDs to build the idMap
            for (String id : getConfigurationSection("STEPS." + step + "."
                    + triggerName + " TRIGGER").getKeys(false))
                if (!id.equalsIgnoreCase("SCRIPT"))
                    idMap.put(id, getString("STEPS." + step + "."
                            + triggerName + " TRIGGER." + id + ".TRIGGER", ""));
            return idMap;
        }
        // No entries, so just return an empty list to avoid NPEs
        else return Collections.emptyMap();
    }

    public boolean checkSpecificTriggerScriptRequirementsFor(Class<? extends AbstractTrigger> trigger,
                                                             Player player, dNPC npc, String id) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        return checkRequirements(player, npc, "STEPS." + step + "." + triggerName + " TRIGGER"
                + (id == null ? "" : "." + id.toUpperCase()));
    }

    public String getTriggerOptionFor(Class<? extends AbstractTrigger> trigger,
                                      Player player, String id, String option) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        return getString("STEPS." + step + "." + triggerName + " TRIGGER"
                + (id == null ? "" : "." + id.toUpperCase()) + "." + option.toUpperCase());
    }

    public boolean hasTriggerOptionFor(Class<? extends AbstractTrigger> trigger,
                                       Player player, String id, String option) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        return contains("STEPS." + step + "." + triggerName + " TRIGGER"
                + (id == null ? "" : "." + id.toUpperCase()) + "." + option.toUpperCase());
    }

}

package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import net.aufdemrand.denizencore.utilities.text.StringHolder;

import java.util.*;

public class InteractScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Interact Script Containers
    // @group Script Container System
    // @description
    // Interact script containers are used to handle NPC triggers.
    //
    // Interact scripts must be referenced from an assignment script container to be of any use.
    //
    // The only required key on a task script container is the 'steps:' key.
    //
    // Within the steps key is a list of steps,
    // where the first step is '1', 'default', or any step that contains a '*' symbol.
    // After that, any steps must be 'zapped' to via the zap command: <@link command zap>.
    //
    // Each step contains a list of trigger types that it handles, and the relevant handling that the given
    // trigger makes available.
    //
    // <code>
    // Interact_Script_Name:
    //
    //   type: interact
    //
    //   steps:
    //
    //     # The first step
    //     1:
    //       # Any trigger type here
    //       click trigger:
    //         script:
    //         # Handle what happens when the NPC is clicked during step 1
    //         - some commands
    //       # Other triggers here
    //     # other steps here
    //
    // </code>
    //
    // -->

    public InteractScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);

        try {
            // Find steps/default step in the script
            Set<StringHolder> keys;
            keys = getConfigurationSection("STEPS").getKeys(false);

            // TODO: Throw a warning if 'requirements' section exists
            if (contains("REQUIREMENTS")) {
                dB.echoError("Interact script '" + getName() + "' is outdated: 'requirements' do not exist in modern Denizen!");
            }

            if (keys.isEmpty()) {
                throw new ExceptionInInitializerError("Could not find any STEPS in " + getName() + "! Is the type on this script correct?");
            }

            for (StringHolder step1 : keys) {
                String step = step1.str;
                if (step.contains("*")) {
                    YamlConfiguration defaultStepSection = getConfigurationSection("STEPS." + step);
                    step = step.replace("*", "");
                    set("STEPS." + step, defaultStepSection);
                    set("STEPS." + step + "*", null);
                    defaultStep = step;
                }

                if (step.equalsIgnoreCase("1")) {
                    defaultStep = step;
                }
                if (step.equalsIgnoreCase("DEFAULT")) {
                    defaultStep = step;
                }
                steps.add(step);
            }

        }
        catch (Exception e) {
            dB.echoError(e);
        }

        // Make default step the only step if there is only one step
        if (defaultStep == null && steps.size() == 1) {
            defaultStep = steps.get(0);
        }

        if (defaultStep == null) {
            throw new ExceptionInInitializerError("Must specify a default step in '" + getName() + "'!");
        }
    }

    private String defaultStep = null;
    private List<String> steps = new ArrayList<>();

    public List<String> getStepNames() {
        return steps;
    }

    /**
     * <p>Gets the name of the default step for this interact script container. Default step
     * is specified by a '*' character on the end of the step name.</p>
     * <p/>
     * <b>Example:</b>
     * <tt>
     * Example Interact Script: <br/>
     * Type: interact <br/>
     * Steps: <br/>
     * Step Name*:   <--- Default step for this interact script <br/>
     * ...  <br/>
     * <br/>
     * Another Step Name:  <--- Not the default step, must use ZAP <br/>
     * ...  <br/>
     * </tt>
     * <p/>
     * <p>Note: For the sake of compatibility with v0.76, a step named '1' can also
     * be used to specify a default step.</p>
     *
     * @return name of the default step
     */
    public String getDefaultStepName() {
        return defaultStep;
    }

    /**
     * Checks if the step in this script contains an entry for the specified trigger.
     *
     * @param step    the name of the step to check
     * @param trigger the trigger to check for
     * @return true if the trigger is present in the step, false otherwise
     */
    public boolean containsTriggerInStep(String step, Class<? extends AbstractTrigger> trigger) {
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();

        return contains("STEPS." + step.toUpperCase() + "." + triggerName + " TRIGGER");
    }

    /**
     * <p>Gets a list of ScriptEntries from a Trigger's script key. In order to get the correct
     * entries, pass along the id of the exact script for the Trigger, which should be identified
     * by using getPossibleTriggersFor(). If no id is specified by passing null, the
     * base Script key will be used for the Trigger. If no script matches the trigger or
     * trigger/id combination, an empty list is returned.</p>
     * <p/>
     * <b>Example:</b>
     * <tt>
     * Example Interact Script: <br/>
     * Type: interact <br/>
     * Steps: <br/>
     * Current Step:        <--- checked with Player object <br/>
     * Click Trigger:     <--- obtained with the Trigger class <br/>
     * <br/>
     * id:              <--- id of the specific trigger script/script options <br/>
     * Script: <br/>
     * - ...          <--- entries obtained if id matches <br/>
     * - ... <br/>
     * <br/>
     * Script:          <--- script that is referenced if NO id is specified <br/>
     * - ...            <--- entries returned <br/>
     * - ... <br/>
     * </tt>
     * <p/>
     * <p>Note: This is handled internally with the parse() method in AbstractTrigger, so for
     * basic triggers, you probably don't need to even call this.</p>
     *
     * @param trigger the class of the Trigger to use
     * @param player  the Player involved
     * @param npc     the NPC involved
     * @param id      the id of the Trigger Script, optional
     * @return a list of ScriptEntries from the script or an empty list if no script was found
     */
    public List<ScriptEntry> getEntriesFor(Class<? extends AbstractTrigger> trigger,
                                           dPlayer player, dNPC npc, String id) {
        return getEntriesFor(trigger, player, npc, id, false);
    }

    public List<ScriptEntry> getEntriesFor(Class<? extends AbstractTrigger> trigger,
                                           dPlayer player, dNPC npc, String id, boolean quiet) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Check for entries
        if (contains("STEPS." + InteractScriptHelper.getCurrentStep(player, getName()) + "."
                + triggerName + " TRIGGER."
                + (id == null ? "SCRIPT" : id.toUpperCase() + ".SCRIPT"))) {
            // Entries exist, so get them and return the list of ScriptEntries
            return getEntries(new BukkitScriptEntryData(player, npc),
                    "STEPS." + InteractScriptHelper.getCurrentStep(player, getName()) + "."
                            + triggerName + " TRIGGER."
                            + (id == null ? "SCRIPT" : id.toUpperCase() + ".SCRIPT"));
            // No entries, so just return an empty list to avoid NPEs
        }
        else {
            if (!quiet) {
                dB.echoDebug(this, "No entries in script for " +
                        ("STEPS." + InteractScriptHelper.getCurrentStep(player, getName()) + "."
                                + triggerName + " TRIGGER."
                                + (id == null ? "SCRIPT" : id.toUpperCase() + ".SCRIPT")));
            }
            return Collections.emptyList();
        }
    }

    /**
     * Gets the available IDs with its trigger value in the form of a Map. The 'key' is
     * the name of the ID and the value is the value of the 'Trigger' key that is owned
     * by the ID key.
     * <p/>
     * <b>Example:</b>
     * <tt>
     * Example Interact Script: <br/>
     * Type: interact <br/>
     * Steps: <br/>
     * Current Step:
     * Click Trigger:           <--- obtained with the Trigger class <br/>
     * <br/>
     * id:                    <--- id of the specific trigger script/script options <br/>
     * Trigger: iron_sword  <--- value of the id key <br/>
     * Script: <br/>
     * - ...
     * - ... <br/>
     * <br/>
     * Script:                <--- since this is an id-less entry for the click trigger, <br/>
     * - ...                       it will be ignored and not in the Map. <br/>
     * - ... <br/>
     * </tt>
     * <p/>
     * <p>Note: This is handled internally with the parse() method in AbstractTrigger, so for
     * basic triggers, you probably don't need to even call this.</p>
     *
     * @param trigger The trigger involved
     * @param player  The Denizen Player object for the player who triggered it
     * @return A map of options in the trigger's script, excluding a plain 'script'
     */

    public Map<String, String> getIdMapFor(Class<? extends AbstractTrigger> trigger,
                                           dPlayer player) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        // Check for entries
        if (contains("STEPS." + step + "." + triggerName + " TRIGGER")) {
            // Trigger exists in Player's current step, get ids.
            Map<String, String> idMap = new HashMap<>();
            // Iterate through IDs to build the idMap
            try {
                for (StringHolder id : getConfigurationSection("STEPS." + step + "."
                        + triggerName + " TRIGGER").getKeys(false)) {
                    if (!id.str.equalsIgnoreCase("SCRIPT")) {
                        idMap.put(id.str, getString("STEPS." + step + "."
                                + triggerName + " TRIGGER." + id.str + ".TRIGGER", ""));
                    }
                }
            }
            catch (Exception ex) {
                dB.echoError("Warning: improperly defined " + trigger.getName() + " trigger for script '" + getName() + "'!");
            }
            return idMap;
        }
        // No entries, so just return an empty list to avoid NPEs
        else {
            return Collections.emptyMap();
        }
    }

    public String getTriggerOptionFor(Class<? extends AbstractTrigger> trigger,
                                      dPlayer player, String id, String option) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        return getString("STEPS." + step + "." + triggerName + " TRIGGER"
                + (id == null ? "" : "." + id.toUpperCase()) + "." + option.toUpperCase());
    }

    public boolean hasTriggerOptionFor(Class<? extends AbstractTrigger> trigger,
                                       dPlayer player, String id, String option) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance()
                .getTriggerRegistry().get(trigger).getName().toUpperCase();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        return contains("STEPS." + step + "." + triggerName + " TRIGGER"
                + (id == null ? "" : "." + id.toUpperCase()) + "." + option.toUpperCase());
    }
}

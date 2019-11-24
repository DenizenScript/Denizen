package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;

import java.util.*;

public class InteractScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Interact Script Containers
    // @group Script Container System
    // @description
    // Interact script containers are used to handle NPC triggers.
    //
    // Interact scripts must be referenced from an assignment script container to be of any use.
    // See <@link language assignment script containers>.
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
    // Refer to <@link language interact script triggers> for documentation about the triggers available.
    // Any triggers used must be enabled in <@link action assignment> by <@link command trigger>.
    //
    // Note that script commands ran in interact scripts by default have a delay between each command.
    // To override this delay, put a '^' in front of each command name, or set 'speed: 0' on the container.
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

    // <--[language]
    // @name Interact Script Triggers
    // @group NPC Interact Scripts
    // @description
    // Interact script triggers are the most basic components of standard NPC scripting.
    // They're very useful for NPCs that give quests or have other very basic interactions with players.
    // While less powerful that other tools that Denizen provides, they can be very straightforward and clear to use in many simpler cases.
    //
    // Note that triggers have a default cooldown system built in to prevent users from clicking too rapidly.
    // However these are very short cooldowns by default - when you need a longer cooldown, use
    // <@link command cooldown> or <@link command engage>.
    //
    // Triggers go in <@link language interact script containers>.
    //
    // The available default trigger types are <@link language click triggers>,
    // <@link language damage triggers>, <@link language chat triggers>, and <@link language proximity triggers>.
    // -->

    public InteractScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);

        try {
            // Find steps/default step in the script
            Set<StringHolder> keys;
            keys = getConfigurationSection("steps").getKeys(false);

            if (contains("requirements")) {
                Debug.echoError("Interact script '" + getName() + "' is outdated: 'requirements' do not exist in modern Denizen!");
            }

            if (keys.isEmpty()) {
                throw new ExceptionInInitializerError("Could not find any STEPS in " + getName() + "! Is the type on this script correct?");
            }

            for (StringHolder step1 : keys) {
                String step = step1.str;
                if (step.contains("*")) {
                    YamlConfiguration defaultStepSection = getConfigurationSection("steps." + step);
                    step = step.replace("*", "");
                    set("steps." + step, defaultStepSection);
                    set("steps." + step + "*", null);
                    defaultStep = step;
                }

                if (step.equalsIgnoreCase("1")) {
                    defaultStep = step;
                }
                if (step.equalsIgnoreCase("default")) {
                    defaultStep = step;
                }
                steps.add(step);
            }

        }
        catch (Exception e) {
            Debug.echoError(e);
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
        String triggerName = DenizenAPI.getCurrentInstance().getTriggerRegistry().get(trigger).getName();
        return contains("steps." + step + "." + triggerName + " trigger");
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
    public List<ScriptEntry> getEntriesFor(Class<? extends AbstractTrigger> trigger, PlayerTag player, NPCTag npc, String id) {
        return getEntriesFor(trigger, player, npc, id, false);
    }

    public List<ScriptEntry> getEntriesFor(Class<? extends AbstractTrigger> trigger, PlayerTag player, NPCTag npc, String id, boolean quiet) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance().getTriggerRegistry().get(trigger).getName();
        // Check for entries
        String key = "steps." + InteractScriptHelper.getCurrentStep(player, getName()) + "."
                + triggerName + " trigger." + (id == null ? "script" : id + ".script");
        if (contains(key)) {
            // Entries exist, so get them and return the list of ScriptEntries
            return getEntries(new BukkitScriptEntryData(player, npc), key);
            // No entries, so just return an empty list to avoid NPEs
        }
        else {
            if (!quiet) {
                Debug.echoDebug(this, "No entries in script for " + key);
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

    public Map<String, String> getIdMapFor(Class<? extends AbstractTrigger> trigger, PlayerTag player) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance().getTriggerRegistry().get(trigger).getName();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        // Check for entries
        String keyBase = "steps." + step + "." + triggerName + " trigger";
        if (contains(keyBase)) {
            // Trigger exists in Player's current step, get ids.
            Map<String, String> idMap = new HashMap<>();
            // Iterate through IDs to build the idMap
            try {
                for (StringHolder id : getConfigurationSection(keyBase).getKeys(false)) {
                    if (!id.str.equalsIgnoreCase("script")) {
                        idMap.put(id.str, getString(keyBase + "." + id.str + ".trigger", ""));
                    }
                }
            }
            catch (Exception ex) {
                Debug.echoError("Warning: improperly defined " + trigger.getName() + " trigger for script '" + getName() + "'!");
            }
            return idMap;
        }
        // No entries, so just return an empty list to avoid NPEs
        else {
            return Collections.emptyMap();
        }
    }

    public String getTriggerOptionFor(Class<? extends AbstractTrigger> trigger, PlayerTag player, String id, String option) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance().getTriggerRegistry().get(trigger).getName();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        return getString("steps." + step + "." + triggerName + " trigger"
                + (id == null ? "" : "." + id) + "." + option);
    }

    public boolean hasTriggerOptionFor(Class<? extends AbstractTrigger> trigger, PlayerTag player, String id, String option) {
        // Get the trigger name
        String triggerName = DenizenAPI.getCurrentInstance().getTriggerRegistry().get(trigger).getName();
        // Get the step
        String step = InteractScriptHelper.getCurrentStep(player, getName());
        return contains("steps." + step + "." + triggerName + " trigger" + (id == null ? "" : "." + id) + "." + option);
    }
}

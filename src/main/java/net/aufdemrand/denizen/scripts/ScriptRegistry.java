package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.scripts.ScriptHelper;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;

import java.util.*;

public class ScriptRegistry {

    // Currently loaded 'script-containers'
    private static Map<String, Object> scriptContainers = new HashMap<String, Object>();
    private static Map<String, Class<? extends ScriptContainer>> scriptContainerTypes = new HashMap<String, Class<? extends ScriptContainer>>();

    public static void _registerType(String typeName, Class<? extends ScriptContainer> scriptContainerClass) {
        scriptContainerTypes.put(typeName.toUpperCase(), scriptContainerClass);
    }

    public static Set<String> _getScriptNames() {
        return scriptContainers.keySet();
    }

    public static void _registerCoreTypes() {
        _registerType("interact", InteractScriptContainer.class);
        _registerType("book", BookScriptContainer.class);
        _registerType("item", ItemScriptContainer.class);
        _registerType("entity", EntityScriptContainer.class);
        _registerType("task", TaskScriptContainer.class);
        _registerType("assignment", AssignmentScriptContainer.class);
        _registerType("procedure", ProcedureScriptContainer.class);
        _registerType("world", WorldScriptContainer.class);
        _registerType("format", FormatScriptContainer.class);
        _registerType("inventory", InventoryScriptContainer.class);
        _registerType("player listener", PlayerListenerScriptContainer.class);
        _registerType("command", CommandScriptContainer.class);
        _registerType("yaml data", YamlDataScriptContainer.class);
        _registerType("map", MapScriptContainer.class);
    }

    public static boolean containsScript(String id) {
        return scriptContainers.containsKey(id.toUpperCase());
    }

    public static boolean containsScript(String id, Class scriptContainerType) {
        if (!scriptContainers.containsKey(id.toUpperCase())) return false;
        ScriptContainer script = (ScriptContainer) scriptContainers.get(id.toUpperCase());
        String type = null;
        for (Map.Entry<String, Class<? extends ScriptContainer>> entry : scriptContainerTypes.entrySet()) {
            if (entry.getValue() == scriptContainerType)
                type = entry.getKey();
        }
        return type != null && (script.getContainerType().equalsIgnoreCase(type));
    }

    public static void _buildCoreYamlScriptContainers(YamlConfiguration yamlScripts) {
        scriptContainers.clear();
        EventManager.world_scripts.clear();
        EventManager.events.clear();
        ItemScriptHelper.item_scripts.clear();
        ItemScriptHelper.item_scripts_by_hash_id.clear();
        InventoryScriptHelper.inventory_scripts.clear();
        // Get a set of key names in concatenated Denizen Scripts
        Set<String> scripts = yamlScripts.getKeys(false);
        // Iterate through set
        for (String scriptName : scripts) {
        // Make sure the script has a type
            if (yamlScripts.contains(scriptName + ".TYPE")) {
                String type = yamlScripts.getString(scriptName + ".TYPE");
                // Check that types is a registered type
                if (!scriptContainerTypes.containsKey(type.toUpperCase())) {
                    dB.log("<G>Trying to load an invalid script. '<A>" + scriptName + "<Y>(" + type + ")'<G> is an unknown type.");
                    ScriptHelper.setHadError();
                    continue;
                }
                // Instantiate a new scriptContainer of specified type.
                Class typeClass = scriptContainerTypes.get(type.toUpperCase());
                dB.log("Adding script " + scriptName + " as type " + type.toUpperCase());
                try {
                    scriptContainers.put(scriptName, typeClass.getConstructor(YamlConfiguration.class, String.class)
                            .newInstance(ScriptHelper._gs().getConfigurationSection(scriptName), scriptName));
                } catch (Exception e) {
                    dB.echoError(e);
                    ScriptHelper.setHadError();
                }
            }
            else {
                dB.echoError("Found type-less container: '" + scriptName + "'.");
                ScriptHelper.setHadError();
            }
        }
    }

    public static List<net.aufdemrand.denizencore.utilities.YamlConfiguration> outside_scripts = new ArrayList<net.aufdemrand.denizencore.utilities.YamlConfiguration>();

    /**
     * Adds a YAML FileConfiguration to the list of scripts to be loaded. Adding a new
     * FileConfiguration will reload the scripts automatically.
     *
     * @param yaml_script  the FileConfiguration containing the script
     *
     */
    public static void addYamlScriptContainer(net.aufdemrand.denizencore.utilities.YamlConfiguration yaml_script) {
        outside_scripts.add(yaml_script);
    }

    /**
     * Removes a YAML FileConfiguration to the list of scripts to be loaded. Removing a
     * FileConfiguration will reload the scripts automatically.
     *
     * @param yaml_script  the FileConfiguration containing the script
     *
     */
    public static void removeYamlScriptContainer(YamlConfiguration yaml_script) {
        outside_scripts.remove(yaml_script);
        DenizenCore.reloadScripts();
    }

    public static <T extends ScriptContainer> T getScriptContainerAs(String name, Class<T> type) {
        try {
        if (scriptContainers.containsKey(name.toUpperCase()))
            return type.cast(scriptContainers.get(name.toUpperCase()));
        else return null;
        } catch (Exception e) { }

        return null;
    }

    public static <T extends ScriptContainer> T getScriptContainer(String name) {
        if (scriptContainers.containsKey(name.toUpperCase()))
            return (T) scriptContainers.get(name.toUpperCase());

        else return null;
    }
}

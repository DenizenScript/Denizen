package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.scripts.containers.core.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScriptRegistry {

    // Currently loaded 'script-containers'
    private static Map<String, ScriptContainer> scriptContainers = new HashMap<String, ScriptContainer>();
    private static Map<String, Class<? extends ScriptContainer>> scriptContainerTypes = new HashMap<String, Class<? extends ScriptContainer>>();

    public static void registerCoreTypes() {
        registerType("interact", InteractScriptContainer.class);
        registerType("book", BookScriptContainer.class);
        registerType("item", ItemScriptContainer.class);
        registerType("entity", EntityScriptContainer.class);
        registerType("task", TaskScriptContainer.class);
        registerType("activity", ActivityScriptContainer.class);
        registerType("assignment", AssignmentScriptContainer.class);
    }

    public static void registerType(String typeName, Class<? extends ScriptContainer> scriptContainerClass) {
        scriptContainerTypes.put(typeName.toUpperCase(), scriptContainerClass);
    }

    public static void _buildCoreScripts() {
        Set<String> scripts = DenizenAPI.getCurrentInstance().getScripts().getKeys(false);

        for (String scriptName : scripts)
            if (DenizenAPI.getCurrentInstance().getScripts().contains(scriptName + ".TYPE")) {
                String type = DenizenAPI.getCurrentInstance().getScripts().getString(scriptName + ".TYPE");
                if (type == null) continue;
                dB.log("<G>Trying to load an invalid script. '<T>" + scriptName + "<Y>(" + type + ")'<G> is an unknown type.");
                scriptContainers.put(scriptName.toUpperCase(), new ScriptContainer(scriptName));
            }
    }

    public static void _buildScripts(ConfigurationSection configurationSection) {

    }

    public static <T extends ScriptContainer> T getScriptContainer(String scriptContainerName) {
        try {
            return (T) scriptContainerTypes.get(scriptContainers.get(scriptContainerName.toUpperCase()))
                    .getConstructor(scriptContainerTypes.get(scriptContainers.get(scriptContainerName.toUpperCase())))
                    .newInstance(scriptContainerName.toUpperCase());
        } catch (Exception e) { return null; }
    }

}

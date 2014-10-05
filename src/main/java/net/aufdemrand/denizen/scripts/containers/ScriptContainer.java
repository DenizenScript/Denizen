package net.aufdemrand.denizen.scripts.containers;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptEntrySet;
import net.aufdemrand.denizencore.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.scripts.requirements.RequirementsMode;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import net.aufdemrand.denizencore.utilities.debugging.Debuggable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptContainer implements Debuggable {

    // <--[language]
    // @name Script Container
    // @group Script Container System
    // @description
    // Script Containers are the basic structure that Denizen uses inside its YAML-based scripting files found in your
    // plugins/Denizen/scripts/ folder. Regardless of type, all script containers have basic parts that can usually be
    // described as keys, list keys, parent keys, child keys, values, and list values. While specific container types
    // probably have more specific names, just remember that no matter how complicated a script, this basic structure
    // still applies.
    //
    // It's important to keep in mind that all child keys, including all the main keys of the script, must line up with
    // one another, hierarchically. If you are familiar with YAML, great, because all script containers use it at the
    // core. Every value, in one way or another, belongs to some kind of 'key'. To define a key, use a string value plus
    // a colon (:). Keys can have a single value, a list value, or own another key:
    //
    // <code>
    // script name:
    //   key: value
    //   list key:
    //     - list value
    //     - ...
    //   parent key:
    //     child key: value
    // </code>
    //
    // And here's a container, put into a more familiar context:
    //
    // <code>
    // a haiku script:
    //   type: task
    //   script:
    //   - narrate "A simple script,"
    //   - narrate "with a key value relationship."
    //   - narrate "Oh look, a list!"
    // </code>
    //
    // -->


    public ScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        contents = configurationSection;
        this.name = scriptContainerName.toUpperCase();
    }


    // The contents of the script container
    YamlConfiguration contents;

    /**
     * Gets the contents of the container.
     *
     * @return a ConfigurationSection object
     */
    public YamlConfiguration getContents() {
        return contents;
    }


    /**
     * Casts this container to a specify type of script container. Must be a valid
     * container type of the type casting to.
     *
     * @param type the class of the ScriptContainer casting to
     * @param <T>  the ScriptContainer object
     * @return     a ScriptContainer of the type specified
     */
    public <T extends ScriptContainer> T getAsContainerType(Class<T> type) {
        return type.cast(this);
    }


    // <--[language]
    // @name Script Name
    // @group Script Container System
    // @description
    // Typically refers to the name of a script container. When using the object fetcher with dScript objects,
    // (s@script_name), the script_name referred to is the name of the container.
    //
    // <code>
    // script name:         <--- script name
    //   type: script_type
    //   script:            <--- base script
    //     - script entries
    //     - ...
    //   local script:      <--- local script path
    //     - script entries
    //     - ...
    // </code>
    //
    // -->

    // The name of the script container
    private String name;

    /**
     * Gets the name of the script container.
     *
     * @return  the script container name.
     */
    public String getName() {
        return name;
    }

    public String getFileName() {
        return ScriptHelper.getSource(getName());
    }


    /**
     * Gets a dScript object that represents this container.
     *
     * @return  a dScript object linking this script container.
     */
    public dScript getAsScriptArg() {
        return dScript.valueOf(name);
    }


    // <--[language]
    // @name Script Type
    // @group Script Container System
    // @description
    // The type of container that a script is in. For example, 'task script' is a script type that has some sort of
    // utility script or
    //
    // <code>
    // script name:
    //   type: script_type  <--- script type
    //   script:
    //     - script entries
    //     - ...
    // </code>
    //
    // -->

    /**
     * Gets the value of the type: node specified in the script container structure.
     *
     * @return  the type of container
     */
    public String getContainerType() {
        return contents.contains("TYPE")
                ? contents.getString("TYPE").toUpperCase()
                : null;
    }


    /**
     * Checks the ConfigurationSection for the key/path to key specified.
     *
     * @param path  the path of the key
     * @return      true if the key exists
     */
    public boolean contains(String path) {
        return contents.contains(path.toUpperCase());
    }


    public String getString(String path) {
        return contents.getString(path.toUpperCase());
    }


    public String getString(String path, String def) {
        return contents.getString(path.toUpperCase(), def);
    }


    public List<String> getStringList(String path) {
        return contents.getStringList(path.toUpperCase());
    }


    public YamlConfiguration getConfigurationSection(String path) {
        return contents.getConfigurationSection(path.toUpperCase());
    }


    public void set(String path, Object object) {
        contents.set(path.toUpperCase(), object);
    }


    public boolean checkBaseRequirements(dPlayer player, dNPC npc) {
        return checkRequirements(player, npc, "");
    }

    public boolean checkRequirements(dPlayer player, dNPC npc, String path) {
        if (path == null) path = "";
        if (path.length() > 0) path = path + ".";
        // Get requirements
        List<String> requirements = contents.getStringList(path + "REQUIREMENTS.LIST");
        String mode = contents.getString(path + "REQUIREMENTS.MODE", "ALL");
        // No requirements? Meets requirements!
        if (requirements == null || requirements.isEmpty()) return true;
        // Return new RequirementsContext built with info extracted from the ScriptContainer
        RequirementsContext context = new RequirementsContext(new RequirementsMode(mode), requirements, this);
        context.attachPlayer(player);
        context.attachNPC(npc);
        return DenizenAPI.getCurrentInstance().getScriptEngine().getRequirementChecker().check(context);
    }

    public List<ScriptEntry> getBaseEntries(dPlayer player, dNPC npc) {
        return getEntries(player, npc, "script");
    }

    public List<ScriptEntry> getEntries(dPlayer player, dNPC npc, String path) {
        if (path == null) path = "script";
        ScriptEntrySet set = getSetFor(path.toUpperCase());
        if (set == null)
            return new ArrayList<ScriptEntry>();
        set.setPlayerAndNPC(player, npc);
        return set.getEntries();
    }

    ScriptEntrySet getSetFor(String path) {
        ScriptEntrySet got = scriptsMap.get(path);
        if (got != null) {
            return got.Duplicate();
        }
        List<String> stringEntries = contents.getStringList(path);
        if (stringEntries == null || stringEntries.size() == 0) return null;
        List<ScriptEntry> entries = ScriptBuilder.buildScriptEntries(stringEntries, this, null, null);
        got = new ScriptEntrySet(entries);
        scriptsMap.put(path, got);
        return got.Duplicate();
    }

    private Map<String, ScriptEntrySet> scriptsMap = new HashMap<String, ScriptEntrySet>();

    public boolean checkCooldown(dPlayer player) {
        return CooldownCommand.checkCooldown(player, name);
    }

    /////////////
    // DEBUGGABLE
    /////////

    // Cached debug value to avoid repeated complex YAML calls
    private Boolean shouldDebug = null;

    @Override
    public boolean shouldDebug() throws Exception {
        if (shouldDebug == null)
            shouldDebug = (!(contents.contains("DEBUG") && contents.getString("DEBUG").equalsIgnoreCase("false")));
        return shouldDebug;
    }

    @Override
    public boolean shouldFilter(String criteria) throws Exception {
        return name.equalsIgnoreCase(criteria.replace("s@", ""));
    }

    @Override
    public String toString() {
        return "s@" + getName().toLowerCase();
    }
}

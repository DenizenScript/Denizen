package net.aufdemrand.denizen.scripts.containers;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.scripts.requirements.RequirementsMode;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Script;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;

public class ScriptContainer {

    public ScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        contents = configurationSection;
        this.name = scriptContainerName.toUpperCase();
    }

    ConfigurationSection contents;

    public ConfigurationSection getContents() {
        return contents;
    }

    public <T extends ScriptContainer> T getAsContainerType(Class<T> type) {
        return (T) type.cast(this);
    }

    private String name;

    public Script getAsScriptArg() {
        return Script.valueOf(name);
    }

    public String getType() {
        if (contents.contains("TYPE"))
            return contents.getString("TYPE").toUpperCase();
        else return null;
    }

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

    public ConfigurationSection getConfigurationSection(String path) {
        return contents.getConfigurationSection(path.toUpperCase());
    }

    public void set(String path, Object object) {
        contents.set(path.toUpperCase(), object);
    }

    public String getName() {
        return name;
    }

    public boolean checkBaseRequirements(Player player, dNPC npc) {
        return checkRequirements(player, npc, "");
    }

    public boolean checkRequirements(Player player, dNPC npc, String path) {
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

    public List<ScriptEntry> getBaseEntries(Player player, dNPC npc) {
        return getBaseEntries(player, npc, null);
    }

    public List<ScriptEntry> getBaseEntries(Player player, dNPC npc, String path) {
        return getEntries(player, npc, path, null);
    }

    public List<ScriptEntry> getEntries(Player player, dNPC npc, String path, AbstractTrigger trigger) {
        List<ScriptEntry> list = new ArrayList<ScriptEntry>();
        if (path == null) path = "script";
        List<String> stringEntries = contents.getStringList(path.toUpperCase());
        if (stringEntries == null || stringEntries.isEmpty()) return list;
        list = ScriptBuilder.buildScriptEntries(stringEntries, this, player, npc, trigger);
        return list;
    }

    public boolean checkCooldown(Player player) {
        return CooldownCommand.checkCooldown(player.getName(), name);
    }

}

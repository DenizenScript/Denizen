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
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScriptContainer extends MemorySection {

    public ScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        if (getType() == null) throw new IllegalStateException("Could not locate valid ScriptContainer.");
        this.name = scriptContainerName.toUpperCase();
    }

    public <T extends ScriptContainer> T getAsContainerType(Class<T> type) {
        return (T) type.cast(this);
    }

    private String name;

    public Script getAsScriptArg() {
        return Script.valueOf(name);
    }

    public String getType() {
        return getString("TYPE").toUpperCase();
    }

    public boolean checkBaseRequirements(Player player, dNPC npc) {
        return checkRequirements(player, npc, "");
    }

    public boolean checkRequirements(Player player, dNPC npc, String path) {
        if (path == null) path = "";
        if (path.length() > 0) path = path + ".";
        // Get requirements
        List<String> requirements = getStringList(path + "REQUIREMENTS.LIST");
        String mode = getString(path + "REQUIREMENTS.MODE", "ALL");
        // No requirements? Meets requirements!
        if (requirements == null || requirements.isEmpty()) return true;
        // Return new RequirementsContext built with info extracted from the ScriptContainer
        RequirementsContext context = new RequirementsContext(new RequirementsMode(mode), requirements, this);
        context.attachPlayer(player);
        context.attachNPC(npc);
        return DenizenAPI.getCurrentInstance().getScriptEngine().getRequirementChecker().check(context);
    }

    public List<ScriptEntry> getBaseEntries(Player player, dNPC npc) {
        return getEntries(player, npc, null);
    }

    public List<ScriptEntry> getEntries(Player player, dNPC npc, String path) {
        List<ScriptEntry> list = new ArrayList<ScriptEntry>();
        if (path == null) path = "";
        if (path.length() > 0) path = path + ".";
        List<String> stringEntries = getStringList(path + "SCRIPT");
        if (stringEntries == null || stringEntries.size() == 0) return list;
        list = ScriptBuilder.buildScriptEntries(stringEntries, this, player, npc);
        return list;
    }

    public boolean checkCooldown(Player player) {
        return DenizenAPI._commandRegistry().get(CooldownCommand.class).checkCooldown(player.getName(), name);
    }

}

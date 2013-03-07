package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class FormatScriptContainer extends ScriptContainer {
	
    public FormatScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
    
    public String getFormat(Player player, dNPC npc) {
    	String format = getContents().getString("FORMAT", "<text>");
    	format = TagManager.tag(player, npc, format, false);
        return format;
    }
    
    public void setFormat(String format) {
        getContents().set("FORMAT", format);
    }
    
    public String getFormattedText(ScriptEntry entry) {
        String text = (String) entry.getObject("text");
        return getFormat(entry.getPlayer(), entry.getNPC()).replace("<text>", text);
    }
    
}

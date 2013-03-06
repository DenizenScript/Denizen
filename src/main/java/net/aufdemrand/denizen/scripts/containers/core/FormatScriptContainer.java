package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import org.bukkit.configuration.ConfigurationSection;

public class FormatScriptContainer extends ScriptContainer {
    
    public FormatScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
    
    public String getFormat() {
        return getContents().getString("FORMAT", "<text>");
    }
    
    public void setFormat(String format) {
        getContents().set("FORMAT", format);
    }
    
    public String getFormattedText(ScriptEntry entry) {
        String text = (String) entry.getObject("text");
        String tagText = TagManager.tag(entry.getOfflinePlayer(), entry.getNPC(), text, true, entry);
        
        return getFormat().replace("<text>", tagText);
    }
    
}

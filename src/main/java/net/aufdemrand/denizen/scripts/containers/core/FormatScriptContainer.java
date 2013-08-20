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
        return getString("FORMAT", "<text>");
    }

    public void setFormat(String format) {
        set("FORMAT", format);
    }

    public String getFormattedText(ScriptEntry entry) {
        String text = getFormat().replaceAll("<text>", entry.getElement("text").asString());
        return TagManager.tag(entry.getPlayer(), entry.getNPC(), text);
    }
    
}

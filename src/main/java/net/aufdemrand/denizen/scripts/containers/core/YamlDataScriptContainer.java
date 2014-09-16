package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class YamlDataScriptContainer extends ScriptContainer {

    public YamlDataScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
}

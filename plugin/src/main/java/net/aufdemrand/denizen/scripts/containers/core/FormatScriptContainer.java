package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.dScript;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.core.EscapeTags;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

public class FormatScriptContainer extends ScriptContainer {

    public FormatScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public String getFormat() {
        return getString("FORMAT", "<text>");
    }

    public String getFormattedText(ScriptEntry entry) {
        return getFormattedText(entry.getElement("text").asString(),
                ((BukkitScriptEntryData) entry.entryData).getNPC(),
                ((BukkitScriptEntryData) entry.entryData).getPlayer());
    }

    public String getFormattedText(String textToReplace, dNPC npc, dPlayer player) {
        String text = getFormat().replace("<text", "<el@val[" + EscapeTags.escape(textToReplace) + "].unescaped")
                .replace("<name", "<el@val[" + EscapeTags.escape(npc != null ? npc.getName() : (player != null ? player.getName() : "")) + "].unescaped");
        return TagManager.tag(text, new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this)));
    }

    public String getFormatText(dNPC npc, dPlayer player) {
        String text = getFormat().replace("<text>", String.valueOf((char) 0x00)).replace("<name>", String.valueOf((char) 0x04));
        return TagManager.tag(text, new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this)))
                .replace("%", "%%").replace(String.valueOf((char) 0x00), "%2$s").replace(String.valueOf((char) 0x04), "%1$s");
    }
}

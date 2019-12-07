package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

public class FormatScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Format Script Containers
    // @group Script Container System
    // @description
    // Format script containers are very simple script containers used for formatting messages, usually with the 'narrate' command.
    //
    // <code>
    // Format_Script_Name:
    //
    //   type: format
    //
    //   # The only key is the format. The format can use '<text>' as a special tag to contain the message being sent.
    //   # '<name>' is available as a special tag as well for use with the 'on player chats' event to fill the player's name properly.
    //   # | All format scripts MUST have this key!
    //   format: <name> says <text>
    // </code>
    //
    // -->

    public FormatScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public String getFormat() {
        return getString("format", "<text>");
    }

    public String getFormattedText(String text, ScriptEntry entry) {
        return getFormattedText(text, Utilities.getEntryNPC(entry), Utilities.getEntryPlayer(entry));
    }

    public String getFormattedText(String textToReplace, NPCTag npc, PlayerTag player) {
        String text = getFormat().replace("<text", "<element[" + EscapeTagBase.escape(textToReplace) + "].unescaped")
                .replace("<name", "<el@val[" + EscapeTagBase.escape(npc != null ? npc.getName() : (player != null ? player.getName() : "")) + "].unescaped");
        return TagManager.tag(text, new BukkitTagContext(player, npc, new ScriptTag(this)));
    }

    public String getFormatText(NPCTag npc, PlayerTag player) {
        String text = getFormat().replace("<text>", String.valueOf((char) 0x00)).replace("<name>", String.valueOf((char) 0x04));
        return TagManager.tag(text, new BukkitTagContext(player, npc, new ScriptTag(this)))
                .replace("%", "%%").replace(String.valueOf((char) 0x00), "%2$s").replace(String.valueOf((char) 0x04), "%1$s");
    }
}

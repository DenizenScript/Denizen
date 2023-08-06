package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.SimpleDefinitionProvider;
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
    //     type: format
    //
    //     # The only key is the format. The format can use '<[text]>' as a special def to contain the message being sent.
    //     # '<[name]>' is available as a special def as well for use with the 'on player chats' event to fill the player's name properly.
    //     # Note that 'special' means special: these tags behave a little funny in certain circumstances.
    //     # In particular, these can't be used as real tags in some cases, including for example when using a format script as a determine in the 'player chats' event.
    //     # | All format scripts MUST have this key!
    //     format: <[name]> says <[text]>
    // </code>
    //
    // -->

    public FormatScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
    }

    public String getFormat() {
        return getString("format", "<text>");
    }

    public String getFormattedText(String text, ScriptEntry entry) {
        return getFormattedText(text, Utilities.getEntryNPC(entry), Utilities.getEntryPlayer(entry));
    }

    public String getFormattedText(String textToReplace, NPCTag npc, PlayerTag player) {
        String name = npc != null ? npc.getName() : (player != null ? player.getName() : "");
        String text = getFormat();
        if (text.contains("<text") || text.contains("<name")) {
            BukkitImplDeprecations.pseudoTagBases.warn(this);
            text = text.replace("<text", "<element[" + EscapeTagUtil.escape(textToReplace) + "].unescaped").replace("<name", "<element[" + EscapeTagUtil.escape(name) + "].unescaped");
        }
        BukkitTagContext context = new BukkitTagContext(player, npc, new ScriptTag(this));
        context.definitionProvider = new SimpleDefinitionProvider();
        context.definitionProvider.addDefinition("text", new ElementTag(textToReplace));
        context.definitionProvider.addDefinition("name", new ElementTag(name));
        return TagManager.tag(text, context);
    }

    public String getFormatText(NPCTag npc, PlayerTag player) {
        String text = getFormat();
        if (text.contains("<text") || text.contains("<name")) {
            BukkitImplDeprecations.pseudoTagBases.warn(this);
            text = text.replace("<text>", String.valueOf((char) 0x00)).replace("<name>", String.valueOf((char) 0x04));
        }
        text = text.replace("<[text]>", String.valueOf((char) 0x00)).replace("<[name]>", String.valueOf((char) 0x04));
        return TagManager.tag(text, new BukkitTagContext(player, npc, new ScriptTag(this)))
                .replace("%", "%%").replace(String.valueOf((char) 0x00), "%2$s").replace(String.valueOf((char) 0x04), "%1$s");
    }
}

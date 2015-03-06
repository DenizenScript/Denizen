package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;

public class FormatScriptContainer extends ScriptContainer {

    // <--[example]
    // @Title Using Format Scripts
    // @Description
    // Use format scripts to create preset custom formats for narrate or announce commands.
    //
    // @Code
    // # +--------------------
    // # | Using Format Scripts
    // # |
    // # | Format scripts allow you to create custom preset
    // # | formats for the narrate and announce commands.
    // # |
    // # | EG, turn - narrate "hello"
    // # | into: "MyNPC: hello" with coloring too.
    //
    // # +-- The Format Script --+
    // FormatExample:
    //   # Remember to keep the name short, you're going to be typing it a lot!
    //   # One possible name is just 'chat'.
    //
    //   type: format
    //
    //   # The format script is a very small script
    //   # with only one line after the type
    //
    //   # This one line is 'format: <formatting here>'
    //   format: <&b><npc.name><&7><&co> <&2><text>
    //   # <&b> = cyan text
    //   # <npc.name> will become the NPC's name
    //   # <&7> = white text
    //   # <&co> = a :colon: (You can't directly type one in a message, as it can cause errors)
    //   # <&2> = green text
    //   # <text> will become the message
    //
    // # +-- Elsewhere, in some other script --+
    // - narrate format:FormatExample "Hello there!"
    // # Assuming the NPC is named 'Bob', This will show to the player:
    // # Bob: Hello there!
    // # Except with lots of color.
    //
    // # You can also do that with announce instead of narrate
    //
    // # +-- Additional note --+
    // # Remember, you only need one format script for each style of message
    // # Don't duplicate the format script in every file
    // # It's better to make a script file named something like 'UtilFormats.yml'
    // # And put all your favorite format scripts in there
    // # any NPC script will be able to use them as well
    //
    // # ALSO, you can at any time reformat a tag using the tag '<el@element.format[<script>]>'
    // # EG, <npc.name.format[FormatExample]>
    //
    // -->

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
        String text = getFormat().replace("<text>", TagManager.escapeOutput(textToReplace));
        boolean debug = true;
        if (contains("DEBUG"))
            debug = Boolean.valueOf(getString("DEBUG"));
        return TagManager.tag(text, new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this)));
    }

    public String getFormatText(dNPC npc, dPlayer player) {
        String text = getFormat().replace("<text>", String.valueOf((char)0x00));
        boolean debug = true;
        if (contains("DEBUG"))
            debug = Boolean.valueOf(getString("DEBUG"));
        return TagManager.tag(text, new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this)))
                .replace(String.valueOf((char)0x00), "%s");
    }
}

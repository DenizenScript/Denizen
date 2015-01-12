package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.tags.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.Listener;

public class ParseTags implements Listener {

    public ParseTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
        TagManager.registerTagEvents(this);
    }

    @TagManager.TagEvents
    public void parseTags(ReplaceableTagEvent event) {
        // <--[tag]
        // @attribute <parse:<text to parse>>
        // @returns Element
        // @description
        // Returns the text with any tags in it parsed.
        // WARNING: THIS TAG IS DANGEROUS TO USE, DO NOT USE IT UNLESS
        // YOU KNOW WHAT YOU ARE DOING. USE AT YOUR OWN RISK.
        // -->
        if (event.matches("parse")) {
            if (!event.hasValue()) {
                dB.echoError("Escape tag '" + event.raw_tag + "' does not have a value!");
                return;
            }
            ScriptEntry se = event.getAttributes().getScriptEntry();
            String read = TagManager.tag(TagManager.cleanOutputFully(event.getValue()), new BukkitTagContext(
                            (se != null ?((BukkitScriptEntryData)se.entryData).getPlayer(): null),
                            (se != null ?((BukkitScriptEntryData)se.entryData).getNPC(): null), false, se,
                    se != null ? se.shouldDebug(): true, se != null ? se.getScript(): null));
            event.setReplaced(new Element(read).getAttribute(event.getAttributes().fulfill(1)));
        }
    }
}

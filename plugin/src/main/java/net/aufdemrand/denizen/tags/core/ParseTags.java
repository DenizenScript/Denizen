package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.event.Listener;

public class ParseTags implements Listener {

    // TODO: Move me to the core

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
            String read = TagManager.tag(TagManager.cleanOutputFully(event.getValue()), event.getContext());
            event.setReplaced(new Element(read).getAttribute(event.getAttributes().fulfill(1)));
        }
    }
}

package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class ParseTags {

    // TODO: Move me to the core

    public ParseTags(Denizen denizen) {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                parseTags(event);
            }
        }, "parse");
    }

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
            dObject read = TagManager.tagObject(TagManager.cleanOutputFully(event.getValue()), event.getContext());
            event.setReplacedObject(CoreUtilities.autoAttrib(read, event.getAttributes().fulfill(1)));
        }
    }
}

package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class ParseTags {

    // TODO: Move me to the core

    public ParseTags() {
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

package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;

public class ParseTagBase {

    public ParseTagBase() {
        // Intentionally no docs due to deprecation
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                parseTags(event);
            }
        }, "parse");
    }

    public void parseTags(ReplaceableTagEvent event) {
        BukkitImplDeprecations.oldParseTag.warn(event.getScriptEntry());
        if (event.matches("parse")) {
            if (!event.hasValue()) {
                Debug.echoError("Escape tag '" + event.raw_tag + "' does not have a value!");
                return;
            }
            ObjectTag read = TagManager.tagObject(event.getValue(), event.getContext());
            event.setReplacedObject(CoreUtilities.autoAttrib(read, event.getAttributes().fulfill(1)));
        }
    }
}

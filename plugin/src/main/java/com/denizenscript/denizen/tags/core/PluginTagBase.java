package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.PluginTag;
import com.denizenscript.denizencore.tags.TagManager;

public class PluginTagBase {

    public PluginTagBase() {

        // <--[tag]
        // @attribute <plugin[<plugin>]>
        // @returns PluginTag
        // @description
        // Returns a plugin object constructed from the input value.
        // Refer to <@link language PluginTag objects>.
        // -->
        TagManager.registerTagHandler("plugin", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Plugin tag base must have input.");
                return null;
            }
            return PluginTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}

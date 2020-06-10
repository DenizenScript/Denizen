package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.PluginTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class PluginTagBase {

    public PluginTagBase() {

        // <--[tag]
        // @attribute <plugin[<plugin>]>
        // @returns PluginTag
        // @description
        // Returns a plugin object constructed from the input value.
        // Refer to <@link language PluginTag objects>.
        // -->
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                pluginTags(event);
            }
        }, "plugin");
    }

    public void pluginTags(ReplaceableTagEvent event) {

        if (!event.matches("plugin") || event.replaced()) {
            return;
        }

        PluginTag plugin = null;

        if (event.hasNameContext()) {
            plugin = PluginTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (plugin == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(plugin, attribute.fulfill(1)));

    }
}

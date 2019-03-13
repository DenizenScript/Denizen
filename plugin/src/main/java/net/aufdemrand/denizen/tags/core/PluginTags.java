package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dPlugin;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class PluginTags {

    public PluginTags(Denizen denizen) {
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

        dPlugin plugin = null;

        if (event.hasNameContext()) {
            plugin = dPlugin.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (plugin == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(plugin, attribute.fulfill(1)));

    }
}

package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.tags.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.TagManager;
import org.bukkit.event.Listener;


/**
 * Location tag is a starting point for getting attributes for a
 *
 */

public class ListTags implements Listener {

    public ListTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
        TagManager.registerTagEvents(this);
    }

    @TagManager.TagEvents
    public void listTags(ReplaceableTagEvent event) {

        if (!event.matches("list") || event.replaced()) return;

        dList list = null;

        if (event.hasNameContext())
            list = dList.valueOf(event.getNameContext());

        // Check if list is null, return null if it is
        if (list == null) {
            return;
        }

        // Build and fill attributes
        Attribute attribute = event.getAttributes();
        event.setReplaced(list.getAttribute(attribute.fulfill(1)));

    }
}

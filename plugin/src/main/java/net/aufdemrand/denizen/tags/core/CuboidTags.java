package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.event.Listener;


public class CuboidTags implements Listener {

    public CuboidTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
        TagManager.registerTagEvents(this);
    }

    @TagManager.TagEvents
    public void locationTags(ReplaceableTagEvent event) {

        if (!event.matches("cuboid") || event.replaced()) {
            return;
        }

        dCuboid cuboid = null;

        String context = event.getNameContext();
        if (event.hasNameContext()) {
            cuboid = dCuboid.valueOf(context);
        }

        // Check if cuboid is null, return if it is
        if (cuboid == null) {
            return;
        }

        // Build and fill attributes
        Attribute attribute = event.getAttributes();
        event.setReplaced(cuboid.getAttribute(attribute.fulfill(1)));

    }
}

package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class LocationTags implements Listener {

    public LocationTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void locationTags(ReplaceableTagEvent event) {
        if (!event.matches("location, l")) return;

        if (!(event.hasNameContext() && dLocation.matches(event.getNameContext()))) {
            event.setReplaced("null");
            return;
        }

        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        event.setReplaced(dLocation.valueOf(attribute.getContext(1))
                .getAttribute(attribute.fulfill(1)));

    }

}
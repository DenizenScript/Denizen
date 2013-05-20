package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.arguments.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NotableLocationTags implements Listener {

	public NotableLocationTags(Denizen denizen) {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	@EventHandler
	public void constantTags(ReplaceableTagEvent event) {
		if (!event.matches("NOTABLE")) return;

        String tag = event.raw_tag;

        String id = null;
        if (event.hasValue()) {
            id = event.getValue();
            tag = tag.split(":", 2)[1];
        }

        else if (event.hasNameContext()) id = event.getNameContext();

        if (dLocation.isSavedLocation(id)) {
            dB.echoError("Notable tag '" + event.raw_tag + "': id was not found.");
        }

		dLocation location = dLocation.getSavedLocation(id);

        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        attribute.fulfill(1);

        event.setReplaced(location.getAttribute(attribute));

	}
}
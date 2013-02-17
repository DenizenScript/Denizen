package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ForeignCharacterTags  implements Listener {
	
	public ForeignCharacterTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }
	
	@EventHandler
	public void foreignCharacterTags(ReplaceableTagEvent event) {
			
		if (!event.getName().startsWith("&")) return;
		
		if (event.getName().equals("&auml")) {
			event.setReplaced("ä");
			return;
		}
		else if (event.getName().equals("&Auml")) {
			event.setReplaced("Ä");
			return;
		}
		else if (event.getName().equals("&ouml")) {
			event.setReplaced("ö");
			return;
		}
		else if (event.getName().equals("&Ouml")) {
			event.setReplaced("Ö");
			return;
		}
		else if (event.getName().equals("&uuml")) {
			event.setReplaced("ü");
			return;
		}
		else if (event.getName().equals("&Uuml")) {
			event.setReplaced("Ü");
			return;
		}

	}
}

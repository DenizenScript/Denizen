package net.aufdemrand.denizen.tags.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BookmarkTags implements Listener {

    private Denizen denizen;

    public BookmarkTags(Denizen denizen) {
        this.denizen = denizen;
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void bookmarksTags(ReplaceableTagEvent event) {
        if (!event.matches("BOOKMARK")) return;

        // <BOOKMARK:bookmark_name> (assumes attached to supplied NPC)
        // or <BOOKMARK:NPC_NAME:bookmark_name>
        // Note: NPCs Name is case-sensitive

        // Find bookmark
        List<String> bookmarks = new ArrayList<String>();
        if (denizen.getSaves().contains("Denizens." + event.getNPC().getName() + ".Bookmarks.Block"))
            bookmarks.addAll(denizen.getSaves().getStringList("Denizens." + event.getNPC().getName() + ".Bookmarks.Location)"));
        if (denizen.getSaves().contains("Denizens." + event.getNPC().getName() + ".Bookmarks.Block"))
            bookmarks.addAll(denizen.getSaves().getStringList("Denizens." + event.getNPC().getName() + ".Bookmarks.Location)"));

        // Iterate through bookmarks to find a match. Start at end of list.. check Location Bookmarks first.
        if (bookmarks.isEmpty()) return;
        for (int i = bookmarks.size() - 1; i > 0; i--) {
            if (bookmarks.get(i).split(" ")[0].equalsIgnoreCase(event.getValue())) {
                String[] location = bookmarks.get(i).split(" ")[1].split(";");
                event.setReplaceable(location[1] + "," + location[2] + "," + location[3] + "," + location[0]);
                break;
            }
        }
    }


}
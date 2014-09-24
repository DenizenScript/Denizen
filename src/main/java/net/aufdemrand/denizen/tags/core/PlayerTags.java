package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTags implements Listener {

    public PlayerTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    ///////////
    // Player Chat History
    /////////

    public static Map<String, List<String>> playerChatHistory = new ConcurrentHashMap<String, List<String>>(8, 0.9f, 2);

    @EventHandler(priority = EventPriority.MONITOR)
    public void addMessage(final AsyncPlayerChatEvent event) {
        Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                List<String> history = playerChatHistory.get(event.getPlayer().getName());
                // If history hasn't been started for this player, initialize a new ArrayList
                if (history == null) history = new ArrayList<String>();
                // Maximum history size is 10
                // TODO: Make size configurable
                if (history.size() > 10) history.remove(9);
                // Add message to history
                history.add(0, event.getMessage());
                // Store the new history
                playerChatHistory.put(event.getPlayer().getName(), history);
            }
        }, 1);
    }


    //////////
    //  ReplaceableTagEvent handler
    ////////

    @EventHandler
    public void playerTags(ReplaceableTagEvent event) {

        if (!event.matches("player, pl") || event.replaced()) return;

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        // PlayerTags require a... dPlayer!
        dPlayer p = event.getPlayer();

        // Player tag may specify a new player in the <player[context]...> portion of the tag.
        if (attribute.hasContext(1))
            // Check if this is a valid player and update the dPlayer object reference.
            if (dPlayer.matches(attribute.getContext(1)))
                p = dPlayer.valueOf(attribute.getContext(1));
            else {
                if (!event.hasAlternative()) dB.echoError("Could not match '" + attribute.getContext(1) + "' to a valid player!");
                return;
            }

        if (p == null || !p.isValid()) {
            if (!event.hasAlternative()) dB.echoError("Invalid or missing player for tag <" + event.raw_tag + ">!");
            event.setReplaced("null");
            return;
        }

        event.setReplaced(p.getAttribute(attribute.fulfill(1)));
    }
}



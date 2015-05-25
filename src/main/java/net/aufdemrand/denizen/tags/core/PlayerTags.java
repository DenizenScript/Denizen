package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.tags.TagManager;
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
        TagManager.registerTagEvents(this);
    }

    ///////////
    // Player Chat History
    /////////

    public static Map<String, List<String>> playerChatHistory = new ConcurrentHashMap<String, List<String>>(8, 0.9f, 2);

    @EventHandler(priority = EventPriority.MONITOR)
    public void addMessage(final AsyncPlayerChatEvent event) {
        final int maxSize = Settings.chatHistoryMaxMessages();
        if (maxSize > 0) {
            Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
                @Override
                public void run() {
                    List<String> history = playerChatHistory.get(event.getPlayer().getName());
                    // If history hasn't been started for this player, initialize a new ArrayList
                    if (history == null) history = new ArrayList<String>();
                    // Maximum history size is specified by config.yml
                    if (history.size() > maxSize) history.remove(maxSize - 1);
                    // Add message to history
                    history.add(0, event.getMessage());
                    // Store the new history
                    playerChatHistory.put(event.getPlayer().getName(), history);
                }
            }, 1);
        }
    }


    //////////
    //  ReplaceableTagEvent handler
    ////////

    @TagManager.TagEvents
    public void playerTags(ReplaceableTagEvent event) {

        if (!event.matches("player", "pl") || event.replaced()) return;

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = event.getAttributes();

        // PlayerTags require a... dPlayer!
        dPlayer p = ((BukkitTagContext)event.getContext()).player;

        // Player tag may specify a new player in the <player[context]...> portion of the tag.
        if (attribute.hasContext(1))
            p = dPlayer.valueOf(attribute.getContext(1));

        if (p == null || !p.isValid()) {
            if (!event.hasAlternative()) dB.echoError("Invalid or missing player for tag <" + event.raw_tag + ">!");
            return;
        }

        event.setReplaced(p.getAttribute(attribute.fulfill(1)));
    }
}



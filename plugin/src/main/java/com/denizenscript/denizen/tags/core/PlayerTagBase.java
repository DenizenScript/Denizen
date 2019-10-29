package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTagBase implements Listener {

    public PlayerTagBase() {

        // <--[tag]
        // @attribute <player[(<player>)]>
        // @returns PlayerTag
        // @description
        // Returns a player object constructed from the input value.
        // If no input value is specified, returns the linked player.
        // -->
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                playerTags(event);
            }
        }, "player", "pl");
    }

    ///////////
    // Player Chat History
    /////////

    public static Map<UUID, List<String>> playerChatHistory = new ConcurrentHashMap<>(8, 0.9f, 2);

    @EventHandler(priority = EventPriority.MONITOR)
    public void addMessage(final AsyncPlayerChatEvent event) {
        final int maxSize = Settings.chatHistoryMaxMessages();
        if (maxSize > 0) {
            Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
                @Override
                public void run() {
                    List<String> history = playerChatHistory.get(event.getPlayer().getUniqueId());
                    // If history hasn't been started for this player, initialize a new ArrayList
                    if (history == null) {
                        history = new ArrayList<>();
                    }
                    // Maximum history size is specified by config.yml
                    if (history.size() > maxSize) {
                        history.remove(maxSize - 1);
                    }
                    // Add message to history
                    history.add(0, event.getMessage());
                    // Store the new history
                    playerChatHistory.put(event.getPlayer().getUniqueId(), history);
                }
            }, 1);
        }
    }


    //////////
    //  ReplaceableTagEvent handler
    ////////


    public void playerTags(ReplaceableTagEvent event) {

        if (!event.matches("player", "pl") || event.replaced()) {
            return;
        }

        if (event.matches("pl")) {
            Deprecations.playerShorthand.warn(event.getScriptEntry());
        }

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = event.getAttributes();

        // PlayerTags require a... PlayerTag!
        PlayerTag p = ((BukkitTagContext) event.getContext()).player;

        // Player tag may specify a new player in the <player[context]...> portion of the tag.
        if (attribute.hasContext(1)) {
            p = PlayerTag.valueOf(attribute.getContext(1), attribute.context);
        }
        if (p == null || !p.isValid()) {
            if (!event.hasAlternative()) {
                Debug.echoError("Invalid or missing player for tag <" + event.raw_tag + ">!");
            }
            return;
        }

        event.setReplacedObject(CoreUtilities.autoAttrib(p, attribute.fulfill(1)));
    }
}



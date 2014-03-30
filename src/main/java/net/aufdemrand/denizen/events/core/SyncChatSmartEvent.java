package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SyncChatSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Fail if "Use asynchronous event" is true in config file
        if (Settings.WorldScriptChatEventAsynchronous()) return false;

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on player chats", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                return true;
            }
        }
        // No matches at all, so return false.
        return false;
    }


    @Override
    public void _initialize() {
        // Yay! Your event is in use! Register it here.
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        // Record that you loaded in the debug.
        dB.log("Loaded Sync Chat SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }



    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // player chats
    //
    // @Triggers when a player chats.
    // @Context
    // <context.message> returns the player's message as an Element.
    //
    // @Determine
    // "CANCELLED" to stop the player from chatting.
    // Element(String) to change the message.
    //
    // -->
    @EventHandler
    public void playerChat(final PlayerChatEvent event) {


        final Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getMessage()));

        String determination = EventManager.doEvents(Arrays.asList("player chats"),
                null, new dPlayer(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (!determination.equals("none")) {
            event.setMessage(determination);
        }
    }
}

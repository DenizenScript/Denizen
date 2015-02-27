package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SyncChatSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Fail if "Use asynchronous event" is true in config file
        if (Settings.worldScriptChatEventAsynchronous()) return false;

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
        PlayerChatEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // player chats
    //
    // @Warning Using this will forcibly sync the chat thread.
    //
    // @Triggers when a player chats.
    // @Context
    // <context.message> returns the player's message as an Element.
    // <context.format> returns the chat message's format.
    //
    // @Determine
    // "CANCELLED" to stop the player from chatting.
    // Element(String) to change the message.
    // "FORMAT:" to set the format script the message should use.
    //
    // -->
    @EventHandler
    public void playerChat(final PlayerChatEvent event) {

        final Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getMessage()));
        context.put("format", new Element(event.getFormat()));

        dPlayer player = dEntity.getPlayerFrom(event.getPlayer());

        String determination = BukkitWorldScriptHelper.doEvents(Arrays.asList("player chats"), null, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (determination.toUpperCase().startsWith("FORMAT:")) {
            String name = determination.substring(7);
            FormatScriptContainer format = ScriptRegistry.getScriptContainer(name);
            if (format == null) dB.echoError("Could not find format script matching '" + name + '\'');
            else event.setFormat(format.getFormattedText(event.getMessage(), null, player));
        }
        else if (!determination.equals("none")) {
            event.setMessage(determination);
        }

    }
}

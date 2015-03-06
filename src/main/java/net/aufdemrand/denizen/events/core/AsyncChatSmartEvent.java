package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AsyncChatSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Fail if "Use asynchronous event" is false in config file
        if (!Settings.worldScriptChatEventAsynchronous()) return false;

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
        dB.log("Loaded Async Chat SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // TODO: Why are we doing this... we're just forcing sync-chat.
    @EventHandler(priority = EventPriority.LOWEST)
    public void asyncPlayerChat(final AsyncPlayerChatEvent event) {

        final Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getMessage()));
        context.put("format", new Element(event.getFormat()));

        final dPlayer player = dEntity.getPlayerFrom(event.getPlayer());

        Callable<List<String>> call = new Callable<List<String>>() {
            @Override
            public List<String> call() {
                return OldEventManager.doEvents(Arrays.asList("player chats"),
                        new BukkitScriptEntryData(player, null), context);
            }
        };
        List<String> determinations = null;
        try {
            determinations = event.isAsynchronous() ? Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), call).get() : call.call();
        } catch (InterruptedException e) {
            // TODO: Need to find a way to fix this eventually
            // dB.echoError(e);
        } catch (ExecutionException e) {
            dB.echoError(e);
        } catch (Exception e) {
            dB.echoError(e);
        }

        for (String determination: determinations) {
            if (determination == null)
                continue;
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            else if (determination.toUpperCase().startsWith("FORMAT:")) {
                String name = determination.substring(7);
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(name);
                if (format == null) dB.echoError("Could not find format script matching '" + name + '\'');
                else event.setFormat(format.getFormattedText(event.getMessage()
                        .replace("&", "&amp").replace("%", "&pc"), null, player)
                        .replace("&pc", "%").replace("&amp", "&"));
            } else if (!determination.equals("none")) {
                event.setMessage(determination);
            }
        }
    }
}

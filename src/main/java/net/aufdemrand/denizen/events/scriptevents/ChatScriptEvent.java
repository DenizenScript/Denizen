package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player chats
    //
    // @Cancellable true
    //
    // @Warning Using this will forcibly sync the chat thread.
    //
    // @Triggers when a player chats.
    // @Context
    // <context.message> returns the player's message as an Element.
    // <context.format> returns the chat message's format.
    // <context.recipients> returns a list of all players that will receive the chat.
    //
    // @Determine
    // Element(String) to change the message.
    // "FORMAT:" + dScript to set the format script the message should use.
    // "RECIPIENTS:" + dList(dPlayer) to set the list of players that will receive the message.
    //
    // -->

    public ChatScriptEvent() {
        instance = this;
    }

    public static ChatScriptEvent instance;
    public PlayerChatEvent pcEvent;
    public AsyncPlayerChatEvent apcEvent;
    public Element message;
    public Element format;
    public dPlayer player;
    public Set<Player> recipients;

    public SyncChatHandler sch = new SyncChatHandler();
    public AsyncChatHandler asch = new AsyncChatHandler();

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player chats");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "Chat";
    }

    boolean async = false;

    @Override
    public void init() {
        async = Settings.worldScriptChatEventAsynchronous();
        if (async) {
            Bukkit.getServer().getPluginManager().registerEvents(asch, DenizenAPI.getCurrentInstance());
        }
        else {
            Bukkit.getServer().getPluginManager().registerEvents(sch, DenizenAPI.getCurrentInstance());
        }
    }

    @Override
    public void destroy() {
        if (async) {
            AsyncPlayerChatEvent.getHandlerList().unregister(asch);
        }
        else {
            PlayerChatEvent.getHandlerList().unregister(sch);
        }
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("format:")) {
            String name = determination.substring(7);
            FormatScriptContainer formatscr = ScriptRegistry.getScriptContainer(name);
            if (formatscr == null) {
                dB.echoError("Could not find format script matching '" + name + '\'');
            }
            else {
                String formatstr = formatscr.getFormatText(null, player);
                if (net.aufdemrand.denizencore.utilities.debugging.dB.verbose) {
                    dB.log("Setting format to " + formatstr);
                }
                format = new Element(formatstr);
            }
        }
        else if (lower.startsWith("recipients:")) {
            String rec_new = determination.substring(11);
            dList recs = dList.valueOf(rec_new);
            List<dPlayer> players = recs.filter(dPlayer.class);
            recipients.clear();
            for (dPlayer player: players) {
                recipients.add(player.getPlayerEntity());
            }
        }

        else if (!lower.startsWith("cancelled")) {
            message = new Element(determination);
        }
        else {
            return super.applyDetermination(container, determination);
        }
        return true;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("message", message);
        context.put("format", format);
        dList list = new dList();
        for (Player tplayer: recipients) {
            list.add(dPlayer.mirrorBukkitPlayer(tplayer).identify());
        }
        context.put("recipients", list);
        return context;
    }

    class SyncChatHandler implements Listener {
        @EventHandler
        public void onSyncChat(PlayerChatEvent event) {
            message = new Element(event.getMessage());
            format = new Element(event.getFormat());
            cancelled = event.isCancelled();
            recipients = new HashSet<Player>(event.getRecipients());
            pcEvent = event;
            apcEvent = null;
            player = dEntity.getPlayerFrom(event.getPlayer());
            fire();
            event.setCancelled(cancelled);
            event.setMessage(message.asString());
            event.setFormat(format.asString());
            event.getRecipients().clear();
            event.getRecipients().addAll(recipients);
        }
    }

    class AsyncChatHandler implements Listener {
        @EventHandler
        public void onAsyncChat(AsyncPlayerChatEvent event) {
            message = new Element(event.getMessage());
            format = new Element(event.getFormat());
            cancelled = event.isCancelled();
            recipients = new HashSet<Player>(event.getRecipients());
            pcEvent = null;
            apcEvent = event;
            player = dEntity.getPlayerFrom(event.getPlayer());
            fire();
            event.setCancelled(cancelled);
            event.setMessage(message.asString());
            event.setFormat(format.asString());
            event.getRecipients().clear();
            event.getRecipients().addAll(recipients);
        }
    }
}

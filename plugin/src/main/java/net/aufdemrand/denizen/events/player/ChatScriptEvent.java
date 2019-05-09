package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: in area
    // <--[event]
    // @Events
    // player chats
    //
    // @Regex ^on player chats$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Warning Using this will forcibly sync the chat thread.
    //
    // @Triggers when a player chats.
    // @Context
    // <context.message> returns the player's message as an Element.
    // <context.format> returns the chat message's raw format.
    // <context.recipients> returns a list of all players that will receive the chat.
    //
    // @Determine
    // Element to change the message.
    // "FORMAT:" + dScript to set the format script the message should use.
    // "RAW_FORMAT:" + Element to set the format directly (without a format script). (Use with caution, avoid if possible).
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
    public boolean matches(ScriptPath path) {
        return runInCheck(path, player.getLocation());
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
            initListener(asch);
        }
        else {
            initListener(sch);
        }
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("format:")) {
            String name = determination.substring("format:".length());
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
        else if (lower.startsWith("raw_format:")) {
            String form = determination.substring("raw_format:".length());
            format = new Element(form);
        }
        else if (lower.startsWith("recipients:")) {
            String rec_new = determination.substring("recipients:".length());
            dList recs = dList.valueOf(rec_new);
            List<dPlayer> players = recs.filter(dPlayer.class, container);
            recipients.clear();
            for (dPlayer player : players) {
                recipients.add(player.getPlayerEntity());
            }
        }
        else if (!isDefaultDetermination(determination)) {
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
    public dObject getContext(String name) {
        if (name.equals("message")) {
            return message;
        }
        else if (name.equals("format")) {
            return format;
        }
        if (name.equals("recipients")) {
            dList list = new dList();
            for (Player tplayer : recipients) {
                list.add(dPlayer.mirrorBukkitPlayer(tplayer).identify());
            }
            return list;
        }
        return super.getContext(name);
    }

    class SyncChatHandler implements Listener {
        @EventHandler
        public void onSyncChat(PlayerChatEvent event) {
            message = new Element(event.getMessage());
            format = new Element(event.getFormat());
            recipients = new HashSet<>(event.getRecipients());
            pcEvent = event;
            apcEvent = null;
            player = dEntity.getPlayerFrom(event.getPlayer());
            fire(event);
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
            recipients = new HashSet<>(event.getRecipients());
            pcEvent = null;
            apcEvent = event;
            player = dEntity.getPlayerFrom(event.getPlayer());
            fire(event);
            event.setMessage(message.asString());
            event.setFormat(format.asString());
            event.getRecipients().clear();
            event.getRecipients().addAll(recipients);
        }
    }
}

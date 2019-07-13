package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.Settings;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // ElementTag to change the message.
    // "FORMAT:" + dScript to set the format script the message should use.
    // "RAW_FORMAT:" + ElementTag to set the format directly (without a format script). (Use with caution, avoid if possible).
    // "RECIPIENTS:" + ListTag(dPlayer) to set the list of players that will receive the message.
    //
    // -->

    public ChatScriptEvent() {
        instance = this;
    }

    public static ChatScriptEvent instance;

    public PlayerChatEvent pcEvent;
    public AsyncPlayerChatEvent apcEvent;
    public ElementTag message;
    public ElementTag format;
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
                Debug.echoError("Could not find format script matching '" + name + '\'');
            }
            else {
                String formatstr = formatscr.getFormatText(null, player);
                if (com.denizenscript.denizencore.utilities.debugging.Debug.verbose) {
                    Debug.log("Setting format to " + formatstr);
                }
                format = new ElementTag(formatstr);
            }
        }
        else if (lower.startsWith("raw_format:")) {
            String form = determination.substring("raw_format:".length());
            format = new ElementTag(form);
        }
        else if (lower.startsWith("recipients:")) {
            String rec_new = determination.substring("recipients:".length());
            ListTag recs = ListTag.valueOf(rec_new);
            List<dPlayer> players = recs.filter(dPlayer.class, container);
            recipients.clear();
            for (dPlayer player : players) {
                recipients.add(player.getPlayerEntity());
            }
        }
        else if (!isDefaultDetermination(determination)) {
            message = new ElementTag(determination);
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
    public ObjectTag getContext(String name) {
        if (name.equals("message")) {
            return message;
        }
        else if (name.equals("format")) {
            return format;
        }
        if (name.equals("recipients")) {
            ListTag list = new ListTag();
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
            message = new ElementTag(event.getMessage());
            format = new ElementTag(event.getFormat());
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
            message = new ElementTag(event.getMessage());
            format = new ElementTag(event.getFormat());
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

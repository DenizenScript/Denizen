package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.List;
import java.util.Set;

public class ChatScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player chats
    //
    // @Regex ^on player chats$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Warning Using this will forcibly sync the chat thread.
    //
    // @Switch message:<matcher> to only process the event if the chat message matches an advanced matcher.
    //
    // @Triggers when a player chats.
    //
    // @Context
    // <context.message> returns the player's message as an Element.
    // <context.format> returns the chat message's raw format.
    // <context.full_text> returns the full text of the chat message (ie, the written message with the format applied to it).
    // <context.recipients> returns a list of all players that will receive the chat.
    //
    // @Determine
    // ElementTag to change the message.
    // "FORMAT:<ScriptTag>" to set the format script the message should use.
    // "RAW_FORMAT:<ElementTag>" to set the format directly (without a format script). (Use with caution, avoid if possible).
    // "RECIPIENTS:<ListTag(PlayerTag)>" to set the list of players that will receive the message.
    //
    // @Player Always.
    //
    // -->

    public ChatScriptEvent() {
    }


    public PlayerChatEvent pcEvent;
    public AsyncPlayerChatEvent apcEvent;
    public PlayerTag player;

    public SyncChatHandler sch = new SyncChatHandler();
    public AsyncChatHandler asch = new AsyncChatHandler();

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player chats")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "message", getMessage())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public void init() {
        initListener(Settings.worldScriptChatEventAsynchronous() ? asch : sch);
    }

    @Override
    public void initForPriority(EventPriority priority, Listener listener) {
        super.initForPriority(priority, Settings.worldScriptChatEventAsynchronous() ? asch : sch);
    }

    @Override
    public ChatScriptEvent clone() {
        ChatScriptEvent event = (ChatScriptEvent) super.clone();
        event.sch = event.new SyncChatHandler();
        event.asch = event.new AsyncChatHandler();
        return event;
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("format:")) {
                String name = determination.substring("format:".length());
                FormatScriptContainer formatscr = ScriptRegistry.getScriptContainer(name);
                if (formatscr == null) {
                    Debug.echoError("Could not find format script matching '" + name + '\'');
                }
                else {
                    String formatstr = formatscr.getFormatText(null, player);
                    if (CoreConfiguration.debugVerbose) {
                        Debug.log("Setting format to " + formatstr);
                    }
                    if (pcEvent != null) {
                        pcEvent.setFormat(formatstr);
                    }
                    else {
                        apcEvent.setFormat(formatstr);
                    }
                }
                return true;
            }
            else if (lower.startsWith("raw_format:")) {
                String form = determination.substring("raw_format:".length());
                if (pcEvent != null) {
                    pcEvent.setFormat(form);
                }
                else {
                    apcEvent.setFormat(form);
                }
                return true;
            }
            else if (lower.startsWith("recipients:")) {
                String rec_new = determination.substring("recipients:".length());
                ListTag recs = ListTag.valueOf(rec_new, getTagContext(path));
                List<PlayerTag> players = recs.filter(PlayerTag.class, path.container, true);
                Set<Player> recipients;
                if (pcEvent != null) {
                    recipients = pcEvent.getRecipients();
                }
                else {
                    recipients = apcEvent.getRecipients();
                }
                recipients.clear();
                for (PlayerTag player : players) {
                    recipients.add(player.getPlayerEntity());
                }
                return true;
            }
            else {
                if (pcEvent != null) {
                    pcEvent.setMessage(determination);
                }
                else {
                    apcEvent.setMessage(determination);
                }
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    public String getMessage() {
        return pcEvent != null ? pcEvent.getMessage() : apcEvent.getMessage();
    }

    public String getFormat() {
        return pcEvent != null ? pcEvent.getFormat() : apcEvent.getFormat();
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "message":
                return new ElementTag(getMessage(), true);
            case "format":
                return new ElementTag(getFormat(), true);
            case "full_text":
                return new ElementTag(String.format(getFormat(), player.getPlayerEntity().getDisplayName(), getMessage()), true);
        }
        if (name.equals("recipients")) {
            ListTag list = new ListTag();
            for (Player tplayer : pcEvent != null ? pcEvent.getRecipients() : apcEvent.getRecipients()) {
                list.addObject(PlayerTag.mirrorBukkitPlayer(tplayer));
            }
            return list;
        }
        return super.getContext(name);
    }

    class SyncChatHandler implements Listener {
        @EventHandler
        public void onSyncChat(PlayerChatEvent event) {
            pcEvent = event;
            apcEvent = null;
            player = new PlayerTag(event.getPlayer());
            fire(event);
        }
    }

    class AsyncChatHandler implements Listener {
        @EventHandler
        public void onAsyncChat(AsyncPlayerChatEvent event) {
            pcEvent = null;
            apcEvent = event;
            player = new PlayerTag(event.getPlayer());
            fire(event);
        }
    }
}

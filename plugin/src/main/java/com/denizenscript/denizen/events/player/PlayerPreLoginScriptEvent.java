package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.QueueTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlayerPreLoginScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player prelogin
    //
    // @Regex ^on player prelogin$
    //
    // @Group Player
    //
    // @Triggers when a player starts to log in to the server.
    // This is during the EARLY authentication process, and should NOT be confused with <@link event player joins>.
    //
    // @Warning This is a very special-case handler, that delays logins until the events are handled on the main thread.
    // Generally, prefer <@link event on player logs in>.
    //
    // @Context
    // <context.hostname> returns an ElementTag of the player's hostname.
    // <context.name> returns an ElementTag of the player's name.
    // <context.uuid> returns an ElementTag of the player's UUID.
    //
    // @Determine
    // QueueTag to cause the event to wait until the queue is complete.
    // "KICKED" to kick the player from the server.
    // "KICKED <ElementTag>" to kick the player and specify a message to show.
    //
    // @Player When the player has previously joined (and thus the UUID is valid).
    //
    // -->

    public PlayerPreLoginScriptEvent() {
    }

    public AsyncPlayerPreLoginEvent event;
    public PlayerTag player;
    public List<QueueTag> waitForQueues = new ArrayList<>();

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player prelogin");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determinationObj instanceof ElementTag) {
            if (CoreUtilities.toLowerCase(determination).startsWith("kicked")) {
                String message = determination.length() > 7 ? determination.substring(7) : determination;
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
                return true;
            }
        }
        if (QueueTag.matches(determination)) {
            QueueTag newQueue = QueueTag.valueOf(determination, getTagContext(path));
            if (newQueue != null && newQueue.getQueue() != null) {
                waitForQueues.add(newQueue);
            }
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "hostname":
                return new ElementTag(event.getAddress().toString());
            case "name":
                return new ElementTag(event.getName());
            case "uuid":
                return new ElementTag(event.getUniqueId().toString());
        }
        return super.getContext(name);
    }

    public boolean needsToWait() {
        if (CoreConfiguration.debugVerbose) {
            Debug.log("Prelogin: queues that might need waiting: " + waitForQueues.size());
        }
        for (QueueTag queue : waitForQueues) {
            if (!queue.getQueue().isStopped) {
                if (CoreConfiguration.debugVerbose) {
                    Debug.log("Prelogin: need to wait for " + queue.getQueue().id);
                }
                return true;
            }
        }
        if (CoreConfiguration.debugVerbose) {
            Debug.log("Prelogin: no need to wait");
        }
        return false;
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (!Bukkit.isPrimaryThread()) {
            PlayerPreLoginScriptEvent altEvent = (PlayerPreLoginScriptEvent) clone();
            Future future = Bukkit.getScheduler().callSyncMethod(Denizen.getInstance(), () -> {
                altEvent.onPlayerLogin(event);
                return null;
            });
            try {
                future.get(30, TimeUnit.SECONDS);
                while (altEvent.needsToWait()) {
                    Thread.sleep(50);
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
            return;
        }
        waitForQueues = new ArrayList<>();
        OfflinePlayer bukkitPlayer = Bukkit.getOfflinePlayer(event.getUniqueId());
        if (bukkitPlayer != null && bukkitPlayer.getName() != null) {
            player = new PlayerTag(bukkitPlayer);
        }
        else {
            player = null;
        }
        this.event = event;
        fire(event);
    }
}

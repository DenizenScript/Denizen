package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.world.TimeChangeScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.flags.DataPersistenceFlagTracker;
import com.denizenscript.denizencore.flags.MapTagBasedFlagTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public class BukkitWorldScriptHelper implements Listener {

    public BukkitWorldScriptHelper() {
        Denizen.getInstance().getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
    }

    /////////////////////
    //   CUSTOM EVENTS
    /////////////////

    public void serverStartEvent() {
        long ticks = Settings.worldScriptTimeEventFrequency().getTicks();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Denizen.getInstance(),
                this::timeEvent, ticks, ticks);
    }

    private final Map<String, Integer> current_time = new HashMap<>();

    public void timeEvent() {
        for (World world : Bukkit.getWorlds()) {
            int hour = (int) (world.getTime() / 1000);
            hour = hour + 6;
            // Get the hour
            if (hour >= 24) {
                hour = hour - 24;
            }

            WorldTag currentWorld = new WorldTag(world);

            if (!current_time.containsKey(currentWorld.identifySimple())
                    || current_time.get(currentWorld.identifySimple()) != hour) {
                current_time.put(currentWorld.identifySimple(), hour);
                TimeChangeScriptEvent.instance.hour = hour;
                TimeChangeScriptEvent.instance.world = currentWorld;
                TimeChangeScriptEvent.instance.fire();
            }
        }
    }

    /////////////////////
    //   PLAYER EVENTS
    /////////////////

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoins(PlayerJoinEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        if (ScoreboardHelper.viewerMap.containsKey(event.getPlayer().getName())) {
            Scoreboard score = ScoreboardHelper.getScoreboard(ScoreboardHelper.viewerMap.get(event.getPlayer().getName()));
            if (score != null) {
                event.getPlayer().setScoreboard(score);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final String message = ChatColor.DARK_GREEN + "CHAT: " + event.getPlayer().getName() + ": " + event.getMessage();
        Bukkit.getScheduler().runTaskLater(Denizen.getInstance(), () -> {
            // If currently recording debug information, add the chat message to debug output
            if (Debug.record) {
                Debug.log(message);
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLogin(PlayerLoginEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        PlayerTag.notePlayer(event.getPlayer());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
            return;
        }
        NMSHandler.getPacketHelper().removeNoCollideTeam(event.getPlayer(), null);
    }

    @EventHandler
    public void chunkLoadEvent(ChunkLoadEvent event) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
            return;
        }
        if (MapTagBasedFlagTracker.skipAllCleanings) {
            return;
        }
        new DataPersistenceFlagTracker(event.getChunk()).doTotalClean();
    }
}

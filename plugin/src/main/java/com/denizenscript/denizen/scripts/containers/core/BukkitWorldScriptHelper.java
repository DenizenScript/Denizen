package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.world.TimeChangeScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.ScoreboardHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.flags.DataPersistenceFlagTracker;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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
            if (!current_time.containsKey(world.getName())
                    || current_time.get(world.getName()) != hour) {
                current_time.put(world.getName(), hour);
                TimeChangeScriptEvent.instance.hour = hour;
                TimeChangeScriptEvent.instance.world = new WorldTag(world);
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
        String board = ScoreboardHelper.viewerMap.get(event.getPlayer().getUniqueId());
        if (board != null) {
            Scoreboard score = ScoreboardHelper.getScoreboard(board);
            if (score != null) {
                event.getPlayer().setScoreboard(score);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Bukkit.getScheduler().runTaskLater(Denizen.instance, () -> {
            // If currently recording debug information, add the chat message to debug output
            if (CoreConfiguration.shouldRecordDebug) {
                Debug.log(ChatColor.DARK_GREEN + "CHAT: " + event.getPlayer().getName() + ": " + event.getMessage());
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
        NMSHandler.packetHelper.removeNoCollideTeam(event.getPlayer(), null);
    }

    @EventHandler
    public void chunkLoadEvent(ChunkLoadEvent event) {
        if (CoreConfiguration.skipAllFlagCleanings || Settings.skipChunkFlagCleaning) {
            return;
        }
        new DataPersistenceFlagTracker(event.getChunk()).doTotalClean();
    }

    public static void cleanAllWorldChunkFlags() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                new DataPersistenceFlagTracker(chunk).doTotalClean();
            }
        }
    }
}

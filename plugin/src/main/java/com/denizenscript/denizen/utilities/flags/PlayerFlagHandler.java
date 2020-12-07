package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlayerFlagHandler implements Listener {

    public static long cacheTimeoutSeconds = 300;

    public static boolean asyncPreload = false;

    public static class CachedPlayerFlag {

        public long lastAccessed;

        public SavableMapFlagTracker tracker;

        public boolean savingNow = false;

        public boolean loadingNow = false;

        public boolean shouldExpire() {
            if (cacheTimeoutSeconds == -1) {
                return false;
            }
            if (cacheTimeoutSeconds == 0) {
                return true;
            }
            return lastAccessed + (cacheTimeoutSeconds * 1000) < System.currentTimeMillis();
        }
    }

    public static File dataFolder;

    public static HashMap<UUID, CachedPlayerFlag> playerFlagTrackerCache = new HashMap<>();

    public static void cleanCache() {
        if (cacheTimeoutSeconds == -1) {
            return;
        }
        long timeNow = System.currentTimeMillis();
        for (Map.Entry<UUID, CachedPlayerFlag> entry : playerFlagTrackerCache.entrySet()) {
            if (cacheTimeoutSeconds > 0 && entry.getValue().lastAccessed + (cacheTimeoutSeconds * 1000) < timeNow) {
                continue;
            }
            if (Bukkit.getPlayer(entry.getKey()) != null) {
                entry.getValue().lastAccessed = timeNow;
                continue;
            }
            saveThenExpire(entry.getKey(), entry.getValue());
        }
    }

    public static void saveThenExpire(UUID id, CachedPlayerFlag cache) {
        BukkitRunnable expireTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (cache.shouldExpire()) {
                    playerFlagTrackerCache.remove(id);
                }
            }
        };
        if (!cache.tracker.modified) {
            expireTask.runTaskLater(Denizen.getInstance(), 1);
            return;
        }
        if (cache.savingNow || cache.loadingNow) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    CachedPlayerFlag newCache = playerFlagTrackerCache.get(id);
                    if (newCache != null) {
                        saveThenExpire(id, newCache);
                    }
                }
            }.runTaskLater(Denizen.getInstance(), 10);
        }
        cache.tracker.modified = false;
        String text = cache.tracker.toString();
        cache.savingNow = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    saveFlags(id, text);
                }
                catch (Throwable ex) {
                    Debug.echoError(ex);
                }
                cache.savingNow = false;
                expireTask.runTaskLater(Denizen.getInstance(), 1);
            }
        }.runTaskAsynchronously(Denizen.getInstance());
    }

    public static void loadFlags(UUID id, CachedPlayerFlag cache) {
        try {
            File flagFile = new File(dataFolder, id.toString() + ".dat");
            if (flagFile.exists()) {
                FileInputStream fis = new FileInputStream(flagFile);
                String str = ScriptHelper.convertStreamToString(fis);
                fis.close();
                cache.tracker = new SavableMapFlagTracker(str);
            }
            else {
                cache.tracker = new SavableMapFlagTracker();
            }
        }
        catch (Throwable ex) {
            Debug.echoError("Failed to load player data for player ID '" + id + "'");
            Debug.echoError(ex);
            cache.tracker = new SavableMapFlagTracker();
        }
        cache.loadingNow = false;
    }

    public static AbstractFlagTracker getTrackerFor(UUID id) {
        CachedPlayerFlag cache = playerFlagTrackerCache.get(id);
        if (cache == null) {
            cache = new CachedPlayerFlag();
            cache.lastAccessed = System.currentTimeMillis();
            playerFlagTrackerCache.put(id, cache);
            loadFlags(id, cache);
        }
        else {
            while (cache.loadingNow) {
                try {
                    Thread.sleep(1);
                }
                catch (InterruptedException ex) {
                    Debug.echoError(ex);
                }
            }
        }
        return cache.tracker;
    }

    public static Future loadAsync(UUID id) {
        try {
            CachedPlayerFlag cache = playerFlagTrackerCache.get(id);
            if (cache != null) {
                return null;
            }
            CachedPlayerFlag newCache = new CachedPlayerFlag();
            newCache.lastAccessed = System.currentTimeMillis();
            newCache.loadingNow = true;
            playerFlagTrackerCache.put(id, newCache);
            CompletableFuture future = new CompletableFuture();
            new BukkitRunnable() {
                @Override
                public void run() {
                    loadFlags(id, newCache);
                    future.complete(null);
                }
            }.runTaskAsynchronously(Denizen.getInstance());
            return future;
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    public static void saveAllNow() {
        for (Map.Entry<UUID, CachedPlayerFlag> entry : playerFlagTrackerCache.entrySet()) {
            if (entry.getValue().tracker.modified) {
                while (entry.getValue().savingNow || entry.getValue().loadingNow) {
                    try {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException ex) {
                        Debug.echoError(ex);
                    }
                }
                entry.getValue().tracker.modified = false;
                saveFlags(entry.getKey(), entry.getValue().tracker.toString());
            }
        }
    }

    public static void saveFlags(UUID id, String flagData) {
        File serverFlagsFile = new File(dataFolder, id.toString() + ".dat");
        try {
            Charset charset = ScriptHelper.encoding == null ? null : ScriptHelper.encoding.charset();
            FileOutputStream fiout = new FileOutputStream(serverFlagsFile);
            OutputStreamWriter writer;
            if (charset == null) {
                writer = new OutputStreamWriter(fiout);
            }
            else {
                writer = new OutputStreamWriter(fiout, charset);
            }
            writer.write(flagData);
            writer.close();
        }
        catch (Throwable ex) {
            Debug.echoError("Failed to save player data for player ID '" + id + "'");
            Debug.echoError(ex);
        }
    }
    }
}

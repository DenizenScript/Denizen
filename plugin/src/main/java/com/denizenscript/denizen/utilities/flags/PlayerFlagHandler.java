package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
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
            return lastAccessed + (cacheTimeoutSeconds * 1000) < CoreUtilities.monotonicMillis();
        }
    }

    public static File dataFolder;

    public static HashMap<UUID, CachedPlayerFlag> playerFlagTrackerCache = new HashMap<>();

    public static HashMap<UUID, SoftReference<CachedPlayerFlag>> secondaryPlayerFlagTrackerCache = new HashMap<>();

    private static ArrayList<UUID> toClearCache = new ArrayList<>();

    public static void cleanSecondaryCache() {
        toClearCache.clear();
        for (Map.Entry<UUID, SoftReference<CachedPlayerFlag>> entry : secondaryPlayerFlagTrackerCache.entrySet()) {
            // NOTE: This call will make the GC think the value is still needed, thus the 10 minute cleanup timer to allow the GC to know these are unimportant
            if (entry.getValue().get() == null) {
                toClearCache.add(entry.getKey());
            }
        }
        for (UUID id : toClearCache) {
            secondaryPlayerFlagTrackerCache.remove(id);
        }
    }

    private static int secondaryCleanTicker = 0;

    public static void cleanCache() {
        if (cacheTimeoutSeconds == -1) {
            return;
        }
        if (secondaryCleanTicker++ > 10) {
            cleanSecondaryCache();
        }
        long timeNow = CoreUtilities.monotonicMillis();
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
                    secondaryPlayerFlagTrackerCache.put(id, new SoftReference<>(cache));
                }
            }
        };
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
            return;
        }
        if (!cache.tracker.modified) {
            expireTask.runTaskLater(Denizen.getInstance(), 1);
            return;
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
            cache.tracker = SavableMapFlagTracker.loadFlagFile(new File(dataFolder, id.toString()).getPath());
        }
        finally {
            cache.loadingNow = false;
        }
    }

    public static AbstractFlagTracker getTrackerFor(UUID id) {
        CachedPlayerFlag cache = playerFlagTrackerCache.get(id);
        if (cache == null) {
            SoftReference<CachedPlayerFlag> softRef = secondaryPlayerFlagTrackerCache.get(id);
            if (softRef != null) {
                cache = softRef.get();
                if (cache != null) {
                    cache.lastAccessed = CoreUtilities.monotonicMillis();
                    if (CoreConfiguration.debugVerbose) {
                        Debug.echoError("Verbose - flag tracker updated for " + id);
                    }
                    playerFlagTrackerCache.put(id, cache);
                    secondaryPlayerFlagTrackerCache.remove(id);
                    return cache.tracker;
                }
            }
            cache = new CachedPlayerFlag();
            cache.lastAccessed = CoreUtilities.monotonicMillis();
            cache.loadingNow = true;
            if (CoreConfiguration.debugVerbose) {
                Debug.echoError("Verbose - flag tracker updated for " + id);
            }
            playerFlagTrackerCache.put(id, cache);
            loadFlags(id, cache);
        }
        else {
            if (cache.loadingNow) {
                long start = CoreUtilities.monotonicMillis();
                while (cache.loadingNow) {
                    if (CoreUtilities.monotonicMillis() - start > 15 * 1000) {
                        Debug.echoError("Flag loading timeout, errors may follow");
                        playerFlagTrackerCache.remove(id);
                        return null;
                    }
                    try {
                        Thread.sleep(1);
                    }
                    catch (InterruptedException ex) {
                        Debug.echoError(ex);
                        return cache.tracker;
                    }
                }
            }
        }
        return cache.tracker;
    }

    public static Future loadAsync(UUID id) { // Note: this method is called sync, but triggers an async load
        try {
            CachedPlayerFlag cache = playerFlagTrackerCache.get(id);
            if (cache != null) {
                return null;
            }
            SoftReference<CachedPlayerFlag> softRef = secondaryPlayerFlagTrackerCache.get(id);
            if (softRef != null) {
                cache = softRef.get();
                if (cache != null) {
                    cache.lastAccessed = CoreUtilities.monotonicMillis();
                    if (CoreConfiguration.debugVerbose) {
                        Debug.echoError("Verbose - flag tracker updated for " + id);
                    }
                    playerFlagTrackerCache.put(id, cache);
                    secondaryPlayerFlagTrackerCache.remove(id);
                    return null;
                }
            }
            CachedPlayerFlag newCache = new CachedPlayerFlag();
            newCache.lastAccessed = CoreUtilities.monotonicMillis();
            newCache.loadingNow = true;
            if (CoreConfiguration.debugVerbose) {
                Debug.echoError("Verbose - flag tracker updated for " + id);
            }
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

    public static void saveAllNow(boolean canSleep) {
        for (Map.Entry<UUID, CachedPlayerFlag> entry : playerFlagTrackerCache.entrySet()) {
            CachedPlayerFlag flags = entry.getValue();
            if (flags.tracker.modified) {
                if (!canSleep && flags.savingNow || flags.loadingNow) {
                    continue;
                }
                while (flags.savingNow || flags.loadingNow) {
                    try {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException ex) {
                        Debug.echoError(ex);
                    }
                }
                flags.tracker.modified = false;
                saveFlags(entry.getKey(), flags.tracker.toString());
            }
        }
    }

    public static void saveFlags(UUID id, String flagData) {
        CoreUtilities.journallingFileSave(new File(dataFolder, id.toString() + ".dat").getPath(), flagData);
    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        if (!asyncPreload) {
            return;
        }
        if (!Denizen.hasTickedOnce) {
            return;
        }
        UUID id = event.getUniqueId();
        if (!Bukkit.isPrimaryThread()) {
            Future<Future> future = Bukkit.getScheduler().callSyncMethod(Denizen.getInstance(), () -> {
                return loadAsync(id);
            });
            try {
                Future newFuture = future.get(15, TimeUnit.SECONDS);
                if (newFuture != null) {
                    newFuture.get(15, TimeUnit.SECONDS);
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
    }

    public static void reloadAllFlagsNow() {
        playerFlagTrackerCache.clear();
        secondaryPlayerFlagTrackerCache.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            getTrackerFor(player.getUniqueId());
        }
    }
}

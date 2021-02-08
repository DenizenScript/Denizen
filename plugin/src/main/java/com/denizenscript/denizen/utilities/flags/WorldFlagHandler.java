package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashMap;
import java.util.Map;

public class WorldFlagHandler implements Listener {

    public static HashMap<String, SavableMapFlagTracker> worldFlagTrackers = new HashMap<>();

    public WorldFlagHandler() {
        Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
    }

    public void init() {
        for (World world : Bukkit.getWorlds()) {
            loadWorldFlags(world);
        }
    }

    public void saveAll() {
        for (Map.Entry<String, SavableMapFlagTracker> flagTracker : worldFlagTrackers.entrySet()) {
            if (flagTracker.getValue().modified) {
                flagTracker.getValue().saveToFile("./" + flagTracker.getKey() + "/denizen_flags");
                flagTracker.getValue().modified = false;
            }
        }
    }

    public void shutdown() {
        saveAll();
        worldFlagTrackers.clear();
    }

    public static void loadWorldFlags(World world) {
        if (worldFlagTrackers.containsKey(world.getName())) {
            return;
        }
        worldFlagTrackers.put(world.getName(), SavableMapFlagTracker.loadFlagFile("./" + world.getName() + "/denizen_flags"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        loadWorldFlags(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        SavableMapFlagTracker flags = worldFlagTrackers.remove(event.getWorld().getName());
        if (flags != null && flags.modified) {
            flags.modified = false;
            flags.saveToFile("./" + event.getWorld().getName() + "/denizen_flags");
        }
    }
}

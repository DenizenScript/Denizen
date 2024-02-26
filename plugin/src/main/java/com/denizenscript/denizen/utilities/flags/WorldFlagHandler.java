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

    public void saveAll(boolean lockUntilDone) {
        for (Map.Entry<String, SavableMapFlagTracker> flagTracker : worldFlagTrackers.entrySet()) {
            if (flagTracker.getValue().modified) {
                flagTracker.getValue().saveToFile(flagPathFor(flagTracker.getKey()), lockUntilDone);
                flagTracker.getValue().modified = false;
            }
        }
    }

    public void shutdown() {
        saveAll(true);
        worldFlagTrackers.clear();
    }

    public static String flagPathFor(String worldName) {
        return Bukkit.getWorldContainer().getPath() + "/" + worldName + "/denizen_flags";
    }

    public static void loadWorldFlags(World world) {
        if (worldFlagTrackers.containsKey(world.getName())) {
            return;
        }
        worldFlagTrackers.put(world.getName(), SavableMapFlagTracker.loadFlagFile(flagPathFor(world.getName()), true));
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
            flags.saveToFile(flagPathFor(event.getWorld().getName()), true);
        }
    }
}

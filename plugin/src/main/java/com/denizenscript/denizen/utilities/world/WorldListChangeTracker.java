package com.denizenscript.denizen.utilities.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListChangeTracker implements Listener {

    public static int changes;

    @EventHandler
    public void onWorldLoaded(WorldLoadEvent event) {
        changes++;
    }

    @EventHandler
    public void onWorldUnloaded(WorldUnloadEvent event) {
        changes++;
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        changes++;
    }
}

package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.world.RaidData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidSpawnWaveEvent;

public class RaidSpawnsWaveScriptEvent extends BukkitScriptEvent implements Listener {

    public RaidSpawnsWaveScriptEvent() {
        registerCouldMatcher("raid spawns wave");
    }

    public RaidSpawnWaveEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getRaid().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "leader":
                return new EntityTag(event.getPatrolLeader());
            case "new_raiders":
                ListTag raiders = new ListTag();
                for (Raider raider : event.getRaiders()) {
                    raiders.addObject(new EntityTag(raider));
                }
                return raiders;
            case "raid":
                return RaidData.toMap(event.getRaid());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRaidSpawnsWave(RaidSpawnWaveEvent event) {
        this.event = event;
        fire(event);
    }
}

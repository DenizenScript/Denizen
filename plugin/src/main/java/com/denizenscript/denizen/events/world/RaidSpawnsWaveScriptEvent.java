package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidSpawnWaveEvent;

public class RaidSpawnsWaveScriptEvent extends RaidScriptEvent<RaidSpawnWaveEvent> implements Listener {

    public RaidSpawnsWaveScriptEvent() {
        super(true);
        registerCouldMatcher("raid spawns wave");
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
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRaidSpawnsWave(RaidSpawnWaveEvent event) {
        this.event = event;
        fire(event);
    }
}

package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Raid;
import org.bukkit.entity.Raider;
import org.bukkit.event.raid.RaidEvent;

import java.util.UUID;

public class RaidScriptEvent<T extends RaidEvent> extends BukkitScriptEvent {

    public boolean checkRaidLocation;

    public RaidScriptEvent(boolean checkRaidLocation) {
        this.checkRaidLocation = checkRaidLocation;
    }

    public T event;

    public static MapTag getRaidMap(Raid raid) {
        MapTag data = new MapTag();
        data.putObject("location", new LocationTag(raid.getLocation()));
        ListTag heroes = new ListTag();
        for (UUID uuid : raid.getHeroes()) {
            heroes.addObject(new PlayerTag(uuid));
        }
        data.putObject("heroes", heroes);
        ListTag raiders = new ListTag();
        for (Raider raider : raid.getRaiders()) {
            raiders.addObject(new EntityTag(raider));
        }
        data.putObject("raiders", raiders);
        data.putObject("status", new ElementTag(CoreUtilities.toLowerCase(raid.getStatus().name()), true));
        data.putObject("ticks", new ElementTag(raid.getActiveTicks()));
        data.putObject("level", new ElementTag(raid.getBadOmenLevel()));
        data.putObject("total_groups", new ElementTag(raid.getTotalGroups()));
        data.putObject("spawned_groups", new ElementTag(raid.getSpawnedGroups()));
        data.putObject("health", new ElementTag(raid.getTotalHealth()));
        data.putObject("waves", new ElementTag(raid.getTotalWaves()));
        return data;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (checkRaidLocation && !runInCheck(path, event.getRaid().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "raid":
                return getRaidMap(event.getRaid());
        }
        return super.getContext(name);
    }
}

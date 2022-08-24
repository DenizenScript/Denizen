package com.denizenscript.denizen.utilities.world;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Raid;
import org.bukkit.entity.Raider;

import java.util.UUID;

public class RaidData {

    public static MapTag toMap(Raid raid) {
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
}

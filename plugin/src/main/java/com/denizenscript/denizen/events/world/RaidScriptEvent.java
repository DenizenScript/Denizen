package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.Raid;
import org.bukkit.entity.Raider;
import org.bukkit.event.raid.RaidEvent;

import java.util.UUID;

public class RaidScriptEvent<T extends RaidEvent> extends BukkitScriptEvent {

    // <--[language]
    // @name Raid Event Data
    // @group Useful Lists
    // @description
    // Every event related to village raids has a <context.raid> property, a MapTag wrapper around raid data (see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Raid.html>).
    // These events are <@link event player triggers raid>, <@link event raid finishes>, <@link event raid spawns wave>, and <@link event raid stops>.
    //
    // The data format is as follows:
    // location: a LocationTag of the raid's center
    // heroes: a list of PlayerTags that have participated in the raid
    // raiders: a list of raider EntityTags that remain in the current wave
    // status: the current status of the raid. See <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Raid.RaidStatus.html>
    // age: the raid's age (active time) as a DurationTag
    // level: the Bad Omen level that the raid was started with
    // spawned_groups: the number of raider groups spawned
    // total_groups: the number of groups planned to spawn or already spawned
    // health: the combined health of all current raiders
    // waves: the number of waves in the raid
    // -->

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
        data.putObject("status", new ElementTag(raid.getStatus()));
        data.putObject("age", new DurationTag(raid.getActiveTicks()));
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

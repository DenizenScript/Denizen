package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class EntitySpawnerSpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // spawner spawns <entity>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity spawns from a monster spawner.
    //
    // @Switch spawner:<location> to only process the event if the spawner's location matches.
    //
    // @Context
    // <context.entity> returns the EntityTag that spawned.
    // <context.location> returns the LocationTag the entity will spawn at.
    // <context.spawner_location> returns the LocationTag of the monster spawner.
    //
    // -->

    public EntitySpawnerSpawnScriptEvent() {
        registerCouldMatcher("spawner spawns <entity>");
        registerSwitches("spawner");
    }

    private EntityTag entity;
    private LocationTag location;
    private LocationTag spawnerLocation;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, entity)) {
            return false;
        }
        if (!path.tryObjectSwitch("spawner", spawnerLocation)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "location" -> location;
            case "spawner_location" -> spawnerLocation;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        this.entity = new EntityTag(entity);
        location = new LocationTag(event.getLocation());
        spawnerLocation = new LocationTag(event.getSpawner().getLocation());
        EntityTag.rememberEntity(entity);
        fire(event);
        EntityTag.forgetEntity(entity);
    }
}

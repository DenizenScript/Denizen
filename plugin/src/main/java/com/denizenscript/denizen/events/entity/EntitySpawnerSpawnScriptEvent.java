package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class EntitySpawnerSpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // spawner spawns entity
    // spawner spawns <entity>
    //
    // @Regex ^on spawner spawns [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity spawns from a monster spawner.
    //
    // @Context
    // <context.entity> returns the dEntity that spawned.
    // <context.location> returns the dLocation the entity will spawn at.
    // <context.spawner_location> returns the dLocation of the monster spawner.
    //
    // -->

    public EntitySpawnerSpawnScriptEvent() {
        instance = this;
    }

    public static EntitySpawnerSpawnScriptEvent instance;
    private dEntity entity;
    private dLocation location;
    private dLocation spawnerLocation;
    public SpawnerSpawnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("spawner spawns");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(2))) {
            return false;
        }

        return runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "SpawnerSpawn";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("spawner_location")) {
            return spawnerLocation;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        this.entity = new dEntity(entity);
        location = new dLocation(event.getLocation());
        spawnerLocation = new dLocation(event.getSpawner().getLocation());
        this.event = event;
        dEntity.rememberEntity(entity);
        fire(event);
        dEntity.forgetEntity(entity);
    }
}

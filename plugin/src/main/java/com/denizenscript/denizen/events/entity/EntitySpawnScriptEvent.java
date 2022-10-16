package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class EntitySpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> spawns (because <'cause'>)
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Warning This event may fire very rapidly.
    //
    // @Triggers when an entity spawns.
    //
    // @Context
    // <context.entity> returns the EntityTag that spawned.
    // <context.location> returns the location the entity will spawn at.
    // <context.reason> returns the reason the entity spawned, can be ENTITY_SPAWN or any of: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html>
    // <context.spawner_location> returns the location of the mob spawner, when reason is SPAWNER.
    //
    // -->

    public EntitySpawnScriptEvent() {
        registerCouldMatcher("<entity> spawns (because <'cause'>)");
    }

    public EntityTag entity;
    public LocationTag location;
    public ElementTag reason;
    public EntitySpawnEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (path.eventLower.startsWith("item") || path.eventLower.startsWith("spawner") || path.eventLower.startsWith("npc")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!entity.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("because") && !runGenericCheck(path.eventArgLowerAt(3), reason.toString())) {
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
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("spawner_location") && event instanceof SpawnerSpawnEvent) {
            return new LocationTag(((SpawnerSpawnEvent) event).getSpawner().getLocation());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        this.entity = new EntityTag(entity);
        location = new LocationTag(event.getLocation());
        if (event instanceof CreatureSpawnEvent) {
            CreatureSpawnEvent.SpawnReason creatureReason = ((CreatureSpawnEvent) event).getSpawnReason();
            if (creatureReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                return; // Let the SpawnerSpawnEvent happen and handle it instead
            }
            reason = new ElementTag(creatureReason);
        }
        else if (event instanceof SpawnerSpawnEvent) {
            reason = new ElementTag("SPAWNER");
        }
        else {
            reason = new ElementTag("ENTITY_SPAWN");
        }
        this.event = event;
        EntityTag.rememberEntity(entity);
        fire(event);
        EntityTag.forgetEntity(entity);
    }
}

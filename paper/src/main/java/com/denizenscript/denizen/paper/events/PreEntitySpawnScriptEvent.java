package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PreEntitySpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> prespawns (because <'cause'>)
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Warning This event may fire very rapidly, and only fires for NATURAL and SPAWNER reasons.
    //
    // @Triggers before a mob spawns and before the mob is created for spawning. Note that this has a limited number of use cases.
    // The intent of this event is to save server resources for blanket mob banning/limiting scripts. Use the entity spawn event as a backup.
    //
    // @Context
    // <context.entity> returns the EntityTag that will be spawned. Note that this entity will not be spawned yet, so usage will be limited.
    // <context.location> returns the LocationTag the entity will spawn at.
    // <context.reason> returns an ElementTag of the reason for spawning. Currently, this event only fires for NATURAL and SPAWNER reasons.
    // <context.spawner_location> returns the LocationTag of the spawner's location if this mob is spawning from a spawner.
    //
    // -->

    public PreEntitySpawnScriptEvent() {
        registerCouldMatcher("<entity> prespawns (because <'cause'>)");
    }

    public EntityTag entity;
    public LocationTag location;
    public PreCreatureSpawnEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!entity.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("because")
                && !path.eventArgLowerAt(3).equalsIgnoreCase(event.getReason().name())) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public void cancellationChanged() {
        event.setShouldAbortSpawn(cancelled);
        super.cancellationChanged();
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
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
            return new ElementTag(event.getReason());
        }
        else if (name.equals("spawner_location") && event instanceof PreSpawnerSpawnEvent) {
            return new LocationTag(((PreSpawnerSpawnEvent) event).getSpawnerLocation());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        this.entity = new EntityTag(event.getType());
        this.location = new LocationTag(event.getSpawnLocation());
        this.event = event;
        fire(event);
    }
}

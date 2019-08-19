package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntitySpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity spawns
    // entity spawns (because <cause>)
    // <entity> spawns
    // <entity> spawns (because <cause>)
    //
    // @Regex ^on [^\s]+ spawns( because [^\s]+)?$
    // @Switch in <area>
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
    // <context.reason> returns the reason the entity spawned.
    // Reasons: <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html>
    //
    // -->

    public EntitySpawnScriptEvent() {
        instance = this;
    }

    public static EntitySpawnScriptEvent instance;
    public EntityTag entity;
    public LocationTag location;
    public ElementTag reason;
    public CreatureSpawnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "spawns") && !lower.startsWith("item") && !lower.startsWith("spawner");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (path.eventArgLowerAt(2).equals("because")
                && !path.eventArgLowerAt(3).equalsIgnoreCase(reason.toString())) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntitySpawn";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? EntityTag.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? EntityTag.getNPCFrom(event.getEntity()) : null);
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
        return super.getContext(name);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        this.entity = new EntityTag(entity);
        location = new LocationTag(event.getLocation());
        reason = new ElementTag(event.getSpawnReason().name());
        this.event = event;
        EntityTag.rememberEntity(entity);
        fire(event);
        EntityTag.forgetEntity(entity);
    }

}

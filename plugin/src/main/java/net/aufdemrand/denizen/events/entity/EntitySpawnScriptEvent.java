package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
    // <context.entity> returns the dEntity that spawned.
    // <context.location> returns the location the entity will spawn at.
    // <context.cuboids> DEPRECATED.
    // <context.reason> returns the reason the entity spawned.
    // Reasons: <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html>
    //
    // -->

    public EntitySpawnScriptEvent() {
        instance = this;
    }

    public static EntitySpawnScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public dList cuboids;
    public Element reason;
    public CreatureSpawnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "spawns") && !lower.startsWith("item") && !lower.startsWith("spawner");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;

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
        else if (name.equals("cuboids")) { // NOTE: Deprecated in favor of context.location.cuboids
            if (cuboids == null) {
                cuboids = new dList();
                for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                    cuboids.add(cuboid.identifySimple());
                }
            }
            return cuboids;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        this.entity = new dEntity(entity);
        location = new dLocation(event.getLocation());
        cuboids = null;
        reason = new Element(event.getSpawnReason().name());
        this.event = event;
        dEntity.rememberEntity(entity);
        fire(event);
        dEntity.forgetEntity(entity);
    }

}

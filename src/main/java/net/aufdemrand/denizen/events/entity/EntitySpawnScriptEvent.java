package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashMap;

public class EntitySpawnScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity spawns
    // entity spawns (in <area>) (because <cause>)
    // <entity> spawns
    // <entity> spawns (in <area>) (because <cause>)
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
    // <context.cuboids> returns a list of cuboids that the entity spawned inside. DEPRECATED.
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
        return CoreUtilities.xthArgEquals(1, lower, "spawns") && !lower.startsWith("item");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!entity.matchesEntity(CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        if (CoreUtilities.xthArgEquals(4, lower, "because")) {
            return CoreUtilities.getXthArg(5, lower).equals(reason.toString());
        }
        return true;
    }

    @Override
    public String getName() {
        return "EntitySpawn";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        CreatureSpawnEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()): null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("location", location);
        context.put("cuboids", cuboids);
        context.put("reason", reason);
        return context;
    }

    @EventHandler
    public void onEntityInteract(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        this.entity = new dEntity(entity);
        location = new dLocation(event.getLocation());
        cuboids = new dList();
        for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        reason = new Element(event.getSpawnReason().name());
        cancelled = event.isCancelled();
        this.event = event;
        dEntity.rememberEntity(entity);
        fire();
        dEntity.forgetEntity(entity);
        event.setCancelled(cancelled);
    }

}

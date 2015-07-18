package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.HashMap;

public class EntityExplosionPrimesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity explosion primes (in <area>)
    // <entity> explosion primes (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when an entity decides to explode.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.radius> returns an Element of the explosion's radius.
    // <context.fire> returns an Element with a value of "true" if the explosion will create fire and "false" otherwise.
    // -->

    public EntityExplosionPrimesScriptEvent() {
        instance = this;
    }

    public static EntityExplosionPrimesScriptEvent instance;
    public dEntity entity;
    public Float radius;
    public Boolean fire;
    public ExplosionPrimeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).contains("explosion primes");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return entity.matchesEntity(CoreUtilities.getXthArg(0, lower)) && runInCheck(scriptContainer, s, lower, entity.getLocation());

    }

    @Override
    public String getName() {
        return "EntityExplosionPrimes";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        ExplosionPrimeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesDouble(determination)) {
            radius = aH.getFloatFrom(determination);
            return true;
        }
        if (aH.Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Boolean)) {
            fire = aH.getBooleanFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("radius", new Element(radius));
        context.put("fire", new Element(fire));
        return context;
    }

    @EventHandler
    public void onEntityExplosionPrimes(ExplosionPrimeEvent event) {
        entity = new dEntity(event.getEntity());
        radius = event.getRadius();
        fire = event.getFire();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setFire(fire);
        event.setRadius(radius);
    }
}

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
    // @Regex ^on [^\s]+ explosion primes( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("radius")) {
            return new Element(radius);
        }
        else if (name.equals("fire")) {
            return new Element(fire);
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
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

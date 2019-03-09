package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class EntityExplosionPrimesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity explosion primes
    // <entity> explosion primes
    //
    // @Regex ^on [^\s]+ explosion primes$
    // @Switch in <area>
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
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;

        if (!tryEntity(entity, CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;

    }

    @Override
    public String getName() {
        return "EntityExplosionPrimes";
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

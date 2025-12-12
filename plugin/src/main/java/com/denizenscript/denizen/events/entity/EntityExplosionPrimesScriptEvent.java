package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class EntityExplosionPrimesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> explosion primes
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity decides to explode.
    //
    // @Context
    // <context.entity> returns an EntityTag of the explosive.
    // <context.radius> returns the explosion's radius.
    // <context.fire> returns whether the explosion will create fire.
    //
    // @Determine
    // ElementTag(Decimal) to change the explosion radius.
    // "FIRE:<ElementTag(Boolean)>" to set whether the explosion will produce fire.
    // -->

    public EntityExplosionPrimesScriptEvent() {
        registerCouldMatcher("<entity> explosion primes");
        this.<EntityExplosionPrimesScriptEvent, ElementTag>registerOptionalDetermination(null, ElementTag.class, (evt, context, value) -> {
            if (value.isBoolean()) {
                BukkitImplDeprecations.explosionPrimeDetermination.warn();
                evt.event.setFire(value.asBoolean());
                return true;
            }
            if (value.isFloat()) {
                evt.event.setRadius(value.asFloat());
                return true;
            }
            return false;
        });
        this.<EntityExplosionPrimesScriptEvent, ElementTag>registerOptionalDetermination("fire", ElementTag.class, (evt, context, value) -> {
            if (value.isBoolean()) {
                evt.event.setFire(value.asBoolean());
                return true;
            }
            return false;
        });
    }

    public EntityTag entity;
    public ExplosionPrimeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity.getDenizenObject();
            case "radius" -> new ElementTag(event.getRadius());
            case "fire" -> new ElementTag(event.getFire());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onEntityExplosionPrimes(ExplosionPrimeEvent event) {
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}

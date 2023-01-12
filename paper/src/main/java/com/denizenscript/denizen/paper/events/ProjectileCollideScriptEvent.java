package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ProjectileCollideScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <projectile> collides with <entity>
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a projectile entity collides with an entity (before any damage calculations are done).
    //
    // @Context
    // <context.projectile> returns the projectile that is colliding.
    // <context.entity> returns the entity that was collided with.
    //
    // @Player When the entity collided with is a player.
    // @NPC When the entity collided with is a NPC.
    //
    // @deprecated Use <@link event projectile hits> with the 'entity' switch on versions above 1.19.
    //
    // -->

    public ProjectileCollideScriptEvent() {
        registerCouldMatcher("<projectile> collides with <entity>");
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            BukkitImplDeprecations.projectileCollideEvent.warn(path.container);
        }
        return true;
    }

    public ProjectileCollideEvent event;
    public EntityTag projectile;
    public EntityTag collidedWith;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        if (!path.tryArgObject(0, projectile)) {
            return false;
        }
        if (!path.tryArgObject(3, collidedWith)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(collidedWith);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> collidedWith.getDenizenObject();
            case "projectile" -> projectile;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void projectileCollideEvent(ProjectileCollideEvent event) {
        this.event = event;
        collidedWith = new EntityTag(event.getCollidedWith());
        projectile = new EntityTag(event.getEntity());
        fire(event);
    }
}

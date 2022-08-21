package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
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
    // -->

    public ProjectileCollideScriptEvent() {
        instance = this;
        registerCouldMatcher("<projectile> collides with <entity>");
    }

    public static ProjectileCollideScriptEvent instance;
    public ProjectileCollideEvent event;
    public EntityTag projectile;
    public EntityTag collidedWith;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        if (!projectile.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!collidedWith.tryAdvancedMatcher(path.eventArgLowerAt(3))) {
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
        if (name.equals("entity")) {
            return collidedWith.getDenizenObject();
        }
        else if (name.equals("projectile")) {
            return projectile;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void projectileCollideEvent(ProjectileCollideEvent event) {
        this.event = event;
        collidedWith = new EntityTag(event.getCollidedWith());
        projectile = new EntityTag(event.getEntity());
        fire(event);
    }
}

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
    // projectile collides with entity
    // <projectile> collides with <entity>
    //
    // @Regex ^on [^\s]+ collides with [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Plugin Paper
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
    }

    public static ProjectileCollideScriptEvent instance;
    public ProjectileCollideEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("collides") && path.eventArgLowerAt(2).equals("with");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ProjectileCollides";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getCollidedWith());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return new EntityTag(event.getCollidedWith());
        }
        else if (name.equals("projectile")) {
            return new EntityTag(event.getEntity());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void projectileCollideEvent(ProjectileCollideEvent event) {
        this.event = event;
        fire(event);
    }
}

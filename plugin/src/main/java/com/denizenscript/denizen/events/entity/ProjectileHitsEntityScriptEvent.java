package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitsEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile hits entity
    // projectile hits <entity>
    // <projectile> hits entity
    // <projectile> hits <entity>
    //
    // @Switch shooter:<entity>
    //
    // @Regex ^on [^\s]+ hits [^\s]+$
    //
    // @Synonyms fish hook latches onto entity, arrow hits entity, snowball hits entity
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a projectile hits an entity.
    //
    // @Context
    // <context.projectile> returns the EntityTag of the projectile.
    // <context.shooter> returns the EntityTag of the shooter, if there is one.
    // <context.hit_entity> returns the EntityTag of the entity that was hit.
    //
    // @Player when the entity that was hit is a player.
    //
    // @NPC when the entity that was hit is an NPC.
    //
    // -->
    public ProjectileHitsEntityScriptEvent() {
        instance = this;
    }

    public static ProjectileHitsEntityScriptEvent instance;
    public EntityTag projectile;
    public EntityTag shooter;
    public EntityTag hitEntity;
    public LocationTag location;
    public ProjectileHitEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (!cmd.equals("hits")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!projectile.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!hitEntity.tryAdvancedMatcher(path.eventArgLowerAt(2))) {
            return false;
        }
        if (path.switches.containsKey("shooter") && (shooter == null || !shooter.tryAdvancedMatcher(path.switches.get("shooter")))) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ProjectileHitsEntity";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(hitEntity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("projectile")) {
            return projectile.getDenizenObject();
        }
        else if (name.equals("hit_entity")) {
            return hitEntity.getDenizenObject();
        }
        else if (name.equals("shooter") && shooter != null) {
            return shooter.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onProjectileHits(ProjectileHitEvent event) {
        Entity entity = event.getHitEntity();
        if (entity == null) {
            return;
        }
        projectile = new EntityTag(event.getEntity());
        shooter = projectile.getShooter();
        hitEntity = new EntityTag(entity);
        location = hitEntity.getLocation();
        this.event = event;
        fire(event);
    }
}

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile hits (<block>)
    // projectile hits (<entity>)
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when talec makes a new event, right now!
    //
    // @Context
    // <context.day> returns Tuesday.
    //
    // -->

    public ProjectileHitScriptEvent() {
        registerCouldMatcher("<projectile> hits (<material>)");
        registerCouldMatcher("<projectile> hits (<entity>)");
        registerCouldMatcher("<entity> shoots <material> (with <projectile>)");
        registerSwitches("entity", "block", "shooter", "with");
    }

    public ProjectileHitEvent event;
    public LocationTag hitBlock = null;
    public EntityTag hitEntity = null;
    public EntityTag projectile = null;
    public EntityTag shooter = null;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        String arg1 = path.eventArgLowerAt(1);
        if (arg1.equals("shoots")) {
            BukkitImplDeprecations.entityShootsMaterialEvent.warn(path.container);
        }
        else if (path.switches.containsKey("with")) {
            addPossibleCouldMatchFailReason("unrecognized switch name", "with");
            return false;
        }
        if (arg1.equals("hits") && path.eventArgs.length > 2) {
            BukkitImplDeprecations.projectileHitsEventMatchers.warn(path.container);
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("entity", hitEntity)) {
            return false;
        }
        if (!path.tryObjectSwitch("block", hitBlock)) {
            return false;
        }
        if (!path.tryObjectSwitch("shooter", shooter)) {
            return false;
        }
        if (path.eventArgLowerAt(1).equals("hits")) {
            if (!path.tryArgObject(0, projectile)) {
                return false;
            }
            if (path.eventArgs.length > 2 && !path.tryArgObject(2, hitEntity) && !path.tryArgObject(2, hitBlock)) {
                return false;
            }
        }
        else {
            // Matching specific to deprecated 'entity shoots material' event
            if (event.getHitBlock() == null || !path.tryArgObject(2, new MaterialTag(event.getHitBlock()))) {
                return false;
            }
            if (!path.tryArgObject(0, shooter)) {
                return false;
            }
            if (!path.tryObjectSwitch("with", projectile)) {
                return false;
            }
            if (path.eventArgLowerAt(3).equals("with") && !path.tryArgObject(4, projectile)) {
                return false;
            }
        }
        if (!runInCheck(path, hitEntity != null ? hitEntity.getLocation() : hitBlock)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "projectile" -> projectile.getDenizenObject();
            case "hit_entity" -> hitEntity != null ? hitEntity.getDenizenObject() : null;
            case "hit_block" -> hitBlock;
            case "location" -> {
                BukkitImplDeprecations.projectileHitsBlockLocationContext.warn();
                yield hitBlock;
            }
            case "shooter" -> shooter != null ? shooter.getDenizenObject() : null;
            case "hit_face" -> event.getHitBlockFace() != null ? new LocationTag(event.getHitBlockFace().getDirection()) : null;
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(hitEntity != null ? hitEntity : shooter);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        this.event = event;
        hitBlock = event.getHitBlock() != null ? new LocationTag(event.getHitBlock().getLocation()) : null;
        hitEntity = event.getHitEntity() != null ? new EntityTag(event.getHitEntity()) : null;
        projectile = new EntityTag(event.getEntity());
        shooter = projectile.getShooter();
        fire(event);
    }
}

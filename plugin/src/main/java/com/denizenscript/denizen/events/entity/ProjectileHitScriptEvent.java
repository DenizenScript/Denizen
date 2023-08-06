package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <projectile> hits
    //
    // @Synonyms entity shoots
    //
    // @Switch entity:<entity> to only process the event if an entity got hit, and it matches the specified EntityTag matcher.
    // @Switch block:<block> to only process the event if a block got hit, and it matches the specified LocationTag matcher.
    // @Switch shooter:<entity> to only process the event if the projectile was shot by an entity, and it matches the specified EntityTag matcher.
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers When a projectile hits a block or an entity.
    //
    // @Context
    // <context.projectile> returns an EntityTag of the projectile.
    // <context.hit_entity> returns an EntityTag of the entity that was hit, if any.
    // <context.hit_block> returns a LocationTag of the block that was hit, if any.
    // <context.hit_face> returns a LocationTag vector of the hit normal (like '0,1,0' if the projectile hit the top of a block).
    // <context.shooter> returns an EntityTag of the entity that shot the projectile, if any.
    //
    // @Player when the entity that was hit is a player, or when the shooter is a player if no entity was hit.
    //
    // @NPC when the entity that was hit is a npc, or when the shooter is a npc if no entity was hit.
    //
    // -->

    // <--[event]
    // @Events
    // <projectile> hits <'block/entity'>
    // <entity> shoots <material> (with <projectile>)
    // @Group Entity
    // @Triggers N/A - deprecated in favor of <@link event projectile hits>
    // @Deprecated use new 'projectile hits' unified event
    // -->

    public ProjectileHitScriptEvent() {
        registerCouldMatcher("<projectile> hits (<block>)");
        registerCouldMatcher("<projectile> hits <entity>");
        registerCouldMatcher("<entity> shoots <block> (with <projectile>)");
        registerSwitches("entity", "block", "shooter", "with");
    }

    public ProjectileHitEvent event;
    public LocationTag hitBlock;
    public EntityTag hitEntity;
    public EntityTag projectile;
    public EntityTag shooter;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (path.eventArgLowerAt(1).equals("shoots")) {
            BukkitImplDeprecations.entityShootsMaterialEvent.warn(path.container);
        }
        else {
            if (path.switches.containsKey("with")) { // 'with' is only valid for the deprecated 'entity shoots material' event
                addPossibleCouldMatchFailReason("unrecognized switch name", "with");
                return false;
            }
            if (path.eventArgs.length > 2) {
                BukkitImplDeprecations.projectileHitsEventMatchers.warn(path.container);
            }
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
            case "hit_face" -> event.getHitBlockFace() != null ? new LocationTag(event.getHitBlockFace().getDirection()) : null;
            case "shooter" -> shooter != null ? shooter.getDenizenObject() : null;
            case "location" -> {
                BukkitImplDeprecations.projectileHitsBlockLocationContext.warn();
                yield hitBlock;
            }
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
        projectile = new EntityTag(event.getEntity());
        // Additional checks for some rare edge-cases
        if (projectile.getLocation() == null) {
            return;
        }
        if (Double.isNaN(projectile.getLocation().getDirection().normalize().getX())) {
            return;
        }
        hitBlock = event.getHitBlock() != null ? new LocationTag(event.getHitBlock().getLocation()) : null;
        hitEntity = event.getHitEntity() != null ? new EntityTag(event.getHitEntity()) : null;
        shooter = projectile.getShooter();
        fire(event);
    }
}

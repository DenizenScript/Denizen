package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile launched
    // <entity> launched
    //
    // @Group Entity
    //
    // @Switch by:<entity> to only process the event if the projectile shooter matches the specified entity matcher.
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a projectile is launched.
    //
    // @Context
    // <context.projectile> returns an EntityTag of the projectile.
    // <context.shooter> returns an EntityTag of the entity that shot the projectile, if any.
    //
    // -->

    public ProjectileLaunchedScriptEvent() {
        registerCouldMatcher("<entity> launched");
        registerSwitches("by");
    }

    public EntityTag projectile;
    private LocationTag location;
    public ProjectileLaunchEvent event;
    public EntityTag shooter;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(0, projectile)) {
            return false;
        }
        if (!path.tryObjectSwitch("by", shooter)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> {
                BukkitImplDeprecations.projectileLaunchedEntityContext.warn();
                yield projectile;
            }
            case "projectile" -> projectile;
            case "shooter" -> shooter.getDenizenObject();
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onProjectileLaunched(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();
        EntityTag.rememberEntity(entity);
        this.event = event;
        projectile = new EntityTag(entity);
        location = projectile.getLocation();
        shooter = projectile.getShooter();
        fire(event);
        EntityTag.forgetEntity(entity);
    }
}

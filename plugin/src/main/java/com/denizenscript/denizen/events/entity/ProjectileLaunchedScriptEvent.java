package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile launched
    // <entity> launched
    //
    // @Regex ^on [^\s]+ launched$
    //
    // @Group Entity
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
        registerSwitches("by");
    }

    public EntityTag projectile;
    private LocationTag location;
    public ProjectileLaunchEvent event;
    public EntityTag shooter;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("launched")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("by", shooter)) {
            return false;
        }
        String projTest = path.eventArgLowerAt(0);
        if (!projTest.equals("projectile") && !projectile.tryAdvancedMatcher(projTest, path.context)) {
            return false;
        }
        if (!runInCheck(path, location)) {
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
            case "shooter" -> shooter;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onProjectileLaunched(ProjectileLaunchEvent event) {
        this.event = event;
        projectile = new EntityTag(event.getEntity());
        location = projectile.getLocation();
        shooter = projectile.getShooter();
        fire(event);
    }
}

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
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
    // <context.entity> returns the projectile.
    //
    // -->

    public ProjectileLaunchedScriptEvent() {
    }

    public EntityTag projectile;
    private LocationTag location;
    public ProjectileLaunchEvent event;

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
        String projTest = path.eventArgLowerAt(0);
        if (!projTest.equals("projectile") && !projectile.tryAdvancedMatcher(projTest)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return projectile.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onProjectileLaunched(ProjectileLaunchEvent event) {
        Entity projectile = event.getEntity();
        EntityTag.rememberEntity(projectile);
        this.projectile = new EntityTag(projectile);
        location = new LocationTag(event.getEntity().getLocation());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(projectile);
    }
}

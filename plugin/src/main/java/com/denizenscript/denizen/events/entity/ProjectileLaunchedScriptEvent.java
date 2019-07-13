package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // @Switch in <area>
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
        instance = this;
    }

    public static ProjectileLaunchedScriptEvent instance;
    public dEntity projectile;
    private dLocation location;
    public ProjectileLaunchEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("launched");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String projTest = path.eventArgLowerAt(0);

        if (!projTest.equals("projectile") && !tryEntity(projectile, projTest)) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "ProjectileLaunched";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
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
        dEntity.rememberEntity(projectile);
        this.projectile = new dEntity(projectile);
        location = new dLocation(event.getEntity().getLocation());
        this.event = event;
        fire(event);
        dEntity.forgetEntity(projectile);
    }
}

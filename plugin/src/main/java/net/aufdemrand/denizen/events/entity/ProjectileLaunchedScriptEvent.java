package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile launched (in <area>)
    // <entity> launched (in <area>)
    //
    // @Regex ^on [^\s]+ launched( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
        String s = path.event;
        String lower = path.eventLower;
        String projTest = CoreUtilities.getXthArg(0, lower);

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
    public void destroy() {
        ProjectileLaunchEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
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
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        dEntity.forgetEntity(projectile);
        event.setCancelled(cancelled);
    }
}

package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

public class LingeringPotionSplashScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // lingering potion splash (in <area>)
    // lingering <item> splashes (in <area>)
    //
    // @Regex ^on lingering [^\s]+ splashes( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a lingering splash potion breaks open
    //
    // @Context
    // <context.potion> returns a dItem of the potion that broke open.
    // <context.location> returns the dLocation the splash potion broke open at.
    // <context.entity> returns a dEntity of the splash potion.
    // <context.radius> returns the radius of the effect cloud.
    // <context.duration> returns the lingering duration of the effect cloud.
    //
    // -->

    public LingeringPotionSplashScriptEvent() {
        instance = this;
    }

    public static LingeringPotionSplashScriptEvent instance;
    public LingeringPotionSplashEvent event;
    public dLocation location;
    public Element duration;
    public dEntity entity;
    public Element radius;
    public dItem item;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(2, lower);
        if (!CoreUtilities.getXthArg(0, lower).equals("lingering")) {
            return false;
        }
        if (!cmd.equals("splash") && !cmd.equals("splashes")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String iTest = CoreUtilities.getXthArg(1, s);
        if (!tryItem(item, iTest)) {
            return false;
        }
        if (runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "LingeringPotionSplash";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        LingeringPotionSplashEvent.getHandlerList().unregister(this);
    }


    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("radius")) {
            return radius;
        }
        else if (name.equals("duration")) {
            return duration;
        }
        else if (name.equals("potion")) {
            return item;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        AreaEffectCloud cloud = event.getAreaEffectCloud();
        item = new dItem(event.getEntity().getItem());
        duration = new Element(cloud.getDuration());
        entity = new dEntity(event.getEntity());
        location = entity.getLocation();
        radius = new Element(cloud.getRadius());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

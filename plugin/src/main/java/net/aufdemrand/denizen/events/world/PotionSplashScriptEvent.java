package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionSplashScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // potion splash (in <area>)
    // <item> splashes (in <area>)
    //
    // @Regex ^on [^\s]+ splashes( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a splash potion breaks open
    //
    // @Context
    // <context.potion> returns a dItem of the potion that broke open.
    // <context.entities> returns a dList of effected entities.
    // <context.location> returns the dLocation the splash potion broke open at.
    // <context.entity> returns a dEntity of the splash potion.
    //
    // -->

    public PotionSplashScriptEvent() {
        instance = this;
    }

    public static PotionSplashScriptEvent instance;
    public dItem potion;
    public dList entities;
    public dLocation location;
    public dEntity entity;
    public PotionSplashEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("splash") || cmd.equals("splashes");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String iTest = CoreUtilities.getXthArg(0, lower);
        return tryItem(potion, iTest) && runInCheck(scriptContainer, s, lower, location);
    }

    @Override
    public String getName() {
        return "PotionSplash";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PotionSplashEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("entities")) {
            return entities;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("potion")) {
            return potion;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        entity = new dEntity(event.getEntity());
        potion = new dItem(event.getPotion().getItem());
        location = new dLocation(entity.getLocation());
        entities = new dList();
        for (Entity e : event.getAffectedEntities()) {
            entities.add(new dEntity(e).identify());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

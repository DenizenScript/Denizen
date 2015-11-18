package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;

// TODO: Allow me when we update
    /*
public class FireworkBurstsScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: <-- [ event ]
    // @Events
    // firework bursts (in <area>)
    //
    // @Regex ^on firework bursts( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Warning not yet implemented
    //
    // @Triggers when a firework bursts (explodes).
    //
    // @Context
    // <context.entity> returns the firework that exploded.
    // <context.location> returns the dLocation the firework exploded at.
    //
    // -->

    public FireworkBurstsScriptEvent() {
        instance = this;
    }

    public static FireworkBurstsScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public FireworkExplodeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("firework bursts");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return runInCheck(scriptContainer, s, lower, entity.getLocation());
    }

    @Override
    public String getName() {
        return "FireworkBursts";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        FireworkExplodeEvent.getHandlerList().unregister(this);
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
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireworkBursts(FireworkExplodeEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(entity.getLocation());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
    */

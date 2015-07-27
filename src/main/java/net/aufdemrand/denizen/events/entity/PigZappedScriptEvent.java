package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PigZapEvent;

import java.util.HashMap;

public class PigZappedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // pig zapped (in <area>)
    //
    // @Regex ^on pig zapped( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a pig is zapped by lightning and turned into a pig zombie.
    //
    // @Context
    // <context.pig> returns the dEntity of the pig.
    // <context.pig_zombie> returns the dEntity of the pig zombie.
    // <context.lightning> returns the dEntity of the lightning.
    //
    // -->

    public PigZappedScriptEvent() {
        instance = this;
    }

    public static PigZappedScriptEvent instance;
    public dEntity pig;
    public dEntity pig_zombie;
    private dEntity lightning;
    public PigZapEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return (lower.equals("pig zapped"));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return runInCheck(scriptContainer, s, CoreUtilities.toLowerCase(s), pig.getLocation());
    }

    @Override
    public String getName() {
        return "PigZapped";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PigZapEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("pig", pig);
        context.put("pig_zombie", pig_zombie);
        context.put("lightning", lightning);
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPigZapped(PigZapEvent event) {
        pig = new dEntity(event.getEntity());
        pig_zombie = new dEntity(event.getPigZombie());
        lightning = new dEntity(event.getLightning());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

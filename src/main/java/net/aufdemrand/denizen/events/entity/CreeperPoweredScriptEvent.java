package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreeperPowerEvent;

import java.util.HashMap;

public class CreeperPoweredScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // creeper powered (because <cause>) (in <area>)
    //
    // @Regex ^on creeper powered( because [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a creeper is struck by lightning and turned into a powered creeper.
    //
    // @Context
    // <context.entity> returns the dEntity of the creeper.
    // <context.lightning> returns the dEntity of the lightning.
    // <context.cause> returns an Element of the cause for the creeper being powered.
    //
    // -->

    public CreeperPoweredScriptEvent() {
        instance = this;
    }

    public static CreeperPoweredScriptEvent instance;
    public dEntity lightning;
    public dEntity entity;
    public Element cause;
    public CreeperPowerEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("creeper powered");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String reason = CoreUtilities.getXthArg(3, lower);

        if (CoreUtilities.getXthArg(2, lower).equals("because")
                && !reason.equals(CoreUtilities.toLowerCase(cause.toString()))) {
            return false;
        }
        return runInCheck(scriptContainer, s, lower, event.getEntity().getLocation());
    }

    @Override
    public String getName() {
        return "CreeperPowered";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        CreeperPowerEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        if (lightning != null) {
            context.put("lightning", lightning);
        }
        context.put("entity", entity);
        context.put("cause", cause);
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeperPowered(CreeperPowerEvent event) {
        lightning = new dEntity(event.getLightning());
        entity = new dEntity(event.getEntity());
        cause = new Element(event.getCause().name());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

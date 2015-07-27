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
import org.bukkit.event.entity.EntityUnleashEvent;

import java.util.HashMap;

public class EntityUnleashedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity unleashed (because <reason>) (in <area>)
    // <entity> unleashed (because <reason>) (in <area>)
    //
    // @Regex ^on [^\s]+ unleashed( because [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity is unleashed.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.reason> returns an Element of the reason for the unleashing.
    // Reasons include DISTANCE, HOLDER_GONE, PLAYER_UNLEASH, and UNKNOWN
    //
    // -->

    public EntityUnleashedScriptEvent() {
        instance = this;
    }

    public static EntityUnleashedScriptEvent instance;
    public dEntity entity;
    public Element reason;
    public EntityUnleashEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "unleashed");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!entity.matchesEntity(CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        if (lower.contains("because")) {
            if (!CoreUtilities.getXthArg(3, lower).equals(reason.toString().toLowerCase())) {
                return false;
            }
        }

        return runInCheck(scriptContainer, s, lower, entity.getLocation());
    }

    @Override
    public String getName() {
        return "EntityUnleashed";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityUnleashEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("reason", reason);
        return context;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityUnleashed(EntityUnleashEvent event) {
        entity = new dEntity(event.getEntity());
        reason = new Element(event.getReason().toString());
        this.event = event;
        fire();
    }

}

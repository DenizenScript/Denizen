package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreaksScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // hanging breaks (because <cause>) (in <area>)
    // <hanging> breaks (because <cause>) (in <area>)
    //
    // @Regex ^on [^\s]+ breaks( because [^\s]+)( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting, item_frame, or leash_hitch) is broken.
    //
    // @Context
    // <context.cause> returns the cause of the entity breaking.
    // <context.entity> returns the dEntity that broke the hanging entity, if any.
    // <context.hanging> returns the dEntity of the hanging.
    // <context.cuboids> DEPRECATED.
    // <context.location> DEPRECATED.
    // Causes list: <@link url http://bit.ly/1BeqxPX>
    //
    // -->

    public HangingBreaksScriptEvent() {
        instance = this;
    }

    public static HangingBreaksScriptEvent instance;
    public Element cause;
    public dEntity entity;
    public dEntity hanging;
    public dList cuboids;
    public dLocation location;
    public HangingBreakEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.getXthArg(1, lower).equals("breaks")
                && !CoreUtilities.getXthArg(2, lower).equals("hanging")
                && !CoreUtilities.getXthArg(0, lower).equals("player");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String hangCheck = CoreUtilities.getXthArg(0, lower);

        if (!tryEntity(hanging, hangCheck)) {
            return false;
        }

        if (CoreUtilities.xthArgEquals(2, lower, "because") && !CoreUtilities.xthArgEquals(3, lower, CoreUtilities.toLowerCase(cause.asString()))) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "HangingBreaks";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        HangingBreakEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("cause")) {
            return cause;
        }
        else if (name.equals("entity")) { // NOTE: Deprecated
            return entity;
        }
        else if (name.equals("hanging")) {
            return hanging;
        }
        else if (name.equals("cuboids")) { // NOTE: Deprecated
            return cuboids;
        }
        else if (name.equals("location")) { // NOTE: Deprecated
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onHangingBreaks(HangingBreakEvent event) {
        hanging = new dEntity(event.getEntity());
        cause = new Element(event.getCause().name());
        location = new dLocation(hanging.getLocation());
        if (event instanceof HangingBreakByEntityEvent) {
            entity = new dEntity(((HangingBreakByEntityEvent) event).getRemover());
        }
        cuboids = new dList();
        for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

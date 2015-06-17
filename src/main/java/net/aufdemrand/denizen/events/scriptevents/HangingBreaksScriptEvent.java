package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
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

import java.util.HashMap;

public class HangingBreaksScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // hanging breaks (in <notable cuboid>)
    // hanging breaks because <cause> (in <notable cuboid>)
    // <hanging> breaks (in <notable cuboid>)
    // <hanging> breaks because <cause> (in <notable cuboid>)
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting or itemframe) is broken.
    //
    // @Context
    // <context.cause> returns the cause of the entity breaking.
    // <context.entity> returns the dEntity that broke the hanging entity, if any.
    // <context.hanging> returns the dEntity of the hanging.
    // <context.cuboids> returns a dList of the cuboids the hanging is in. DEPRECATED.
    // <context.location> returns a dLocation of the hanging.
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
        String mat = CoreUtilities.getXthArg(0, lower);
        return CoreUtilities.getXthArg(1, lower).equals("breaks")
                && (mat.equals("hanging") || mat.equals("painting") || mat.equals("item_frame"));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String hangCheck = CoreUtilities.getXthArg(0, lower);
        if (!hangCheck.equals("hanging")
                && hanging.matchesEntity(hangCheck)){
            return false;
        }
        String notable = null;
        if (CoreUtilities.xthArgEquals(3, lower, "in")) {
            notable = CoreUtilities.getXthArg(4, lower);
        }
        else if (CoreUtilities.xthArgEquals(5, lower, "in")) {
            notable = CoreUtilities.getXthArg(6, lower);
        }
        if (notable != null) {
            if (dCuboid.matches(notable)) {
                dCuboid cuboid = dCuboid.valueOf(notable);
                if (!cuboid.isInsideCuboid(location)) {
                    return false;
                }
            }
            else if (dEllipsoid.matches(notable)) {
                dEllipsoid ellipsoid = dEllipsoid.valueOf(notable);
                if (!ellipsoid.contains(location)) {
                    return false;
                }
            }
            else {
                dB.echoError("Invalid event 'IN ...' check [" + getName() + "]: '" + s + "' for " + scriptContainer.getName());
                return false;
            }
        }

        if (CoreUtilities.xthArgEquals(2, lower, "because")){
            if (!CoreUtilities.getXthArg(3, lower).equals(CoreUtilities.toLowerCase(cause.asString()))) {
                return false;
            }
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
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()): null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("cause", cause);
        context.put("entity", entity);
        context.put("hanging", hanging);
        context.put("cuboids", cuboids);
        context.put("location", location);
        return context;
    }

    @EventHandler
    public void onHangingBreaks(HangingBreakEvent event) {
        hanging = new dEntity(event.getEntity());
        cause =  new Element(event.getCause().name());
        location = new dLocation(hanging.getLocation());
        if (event instanceof HangingBreakByEntityEvent) {
            entity = new dEntity(((HangingBreakByEntityEvent) event).getRemover());
        }
        cuboids = new dList();
        for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

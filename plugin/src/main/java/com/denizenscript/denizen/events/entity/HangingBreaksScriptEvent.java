package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dCuboid;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreaksScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // hanging breaks (because <cause>)
    // <hanging> breaks (because <cause>)
    //
    // @Regex ^on [^\s]+ breaks( because [^\s]+)$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting, item_frame, or leash_hitch) is broken.
    //
    // @Context
    // <context.cause> returns the cause of the entity breaking. Causes: ENTITY, EXPLOSION, OBSTRUCTION, PHYSICS, and DEFAULT.
    // <context.entity> returns the dEntity that broke the hanging entity, if any.
    // <context.hanging> returns the dEntity of the hanging.
    // -->

    public HangingBreaksScriptEvent() {
        instance = this;
    }

    public static HangingBreaksScriptEvent instance;
    public Element cause;
    public dEntity entity;
    public dEntity hanging;
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
    public boolean matches(ScriptPath path) {
        String hangCheck = path.eventArgLowerAt(0);

        if (!tryEntity(hanging, hangCheck)) {
            return false;
        }

        if (path.eventArgLowerAt(2).equals("because") && !path.eventArgLowerAt(3).equals(CoreUtilities.toLowerCase(cause.asString()))) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "HangingBreaks";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity != null && entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity != null && entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("cause")) {
            return cause;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("hanging")) {
            return hanging;
        }
        else if (name.equals("cuboids")) {
            dB.echoError("context.cuboids tag is deprecated in " + getName() + " script event");
            dList cuboids = new dList();
            for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                cuboids.add(cuboid.identifySimple());
            }
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
        else {
            entity = null;
        }
        this.event = event;
        fire(event);
    }
}

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreaksScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <hanging> breaks (because <'cause'>)
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting, item_frame, or leash_hitch) is broken.
    //
    // @Context
    // <context.cause> returns the cause of the entity breaking. Causes: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/hanging/HangingBreakEvent.RemoveCause.html>.
    // <context.entity> returns the EntityTag that broke the hanging entity, if any.
    // <context.hanging> returns the EntityTag of the hanging.
    // -->

    public HangingBreaksScriptEvent() {
        registerCouldMatcher("<hanging> breaks (because <'cause'>)");
    }

    public ElementTag cause;
    public EntityTag entity;
    public EntityTag hanging;
    public HangingBreakEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String hangCheck = path.eventArgLowerAt(0);
        if (!hanging.tryAdvancedMatcher(hangCheck)) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("because") && !path.eventArgLowerAt(3).equals(cause.asLowerString())) {
            return false;
        }
        if (!runInCheck(path, hanging.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "cause":
                return cause;
            case "entity":
                return entity;
            case "hanging":
                return hanging;
            case "location":
                BukkitImplDeprecations.hangingBreaksEventContext.warn();
                return hanging.getLocation();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onHangingBreaks(HangingBreakEvent event) {
        hanging = new EntityTag(event.getEntity());
        cause = new ElementTag(event.getCause());
        if (event instanceof HangingBreakByEntityEvent) {
            entity = new EntityTag(((HangingBreakByEntityEvent) event).getRemover());
        }
        else {
            entity = null;
        }
        this.event = event;
        fire(event);
    }
}

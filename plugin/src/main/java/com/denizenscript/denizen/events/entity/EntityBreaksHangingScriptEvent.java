package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class EntityBreaksHangingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity breaks hanging (because <cause>)
    // entity breaks <hanging> (because <cause>)
    // <entity> breaks hanging (because <cause>)
    // <entity> breaks <hanging> (because <cause>)
    //
    // @Regex ^on [^\s]+ breaks [^\s]+( because [^\s]+)?$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting, item_frame, or leash_hitch) is broken.
    //
    // @Context
    // <context.cause> returns the cause of the entity breaking. Causes list: <@link url http://bit.ly/1BeqxPX>
    // <context.breaker> returns the EntityTag that broke the hanging entity, if any.
    // <context.hanging> returns the EntityTag of the hanging.
    //
    // @Player when the breaker is a player.
    //
    // @NPC when the breaker is an npc.
    //
    // -->

    public EntityBreaksHangingScriptEvent() {
        instance = this;
    }

    public static EntityBreaksHangingScriptEvent instance;
    public ElementTag cause;
    public EntityTag breaker;
    public EntityTag hanging;
    public LocationTag location;
    public HangingBreakByEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("breaks");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String entName = path.eventArgLowerAt(0);
        String hang = path.eventArgLowerAt(2);

        if (!tryEntity(breaker, entName)) {
            return false;
        }

        if (!hang.equals("hanging") && !tryEntity(hanging, hang)) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        if (path.eventArgLowerAt(3).equals("because") && !path.eventArgLowerAt(4).equals(CoreUtilities.toLowerCase(cause.asString()))) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityBreaksHanging";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: What if the hanging is an NPC?
        return new BukkitScriptEntryData(breaker.isPlayer() ? breaker.getDenizenPlayer() : null,
                breaker.isCitizensNPC() ? breaker.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("cause")) {
            return cause;
        }
        else if (name.equals("entity")) {
            Deprecations.entityBreaksHangingEventContext.warn();
            return breaker;
        }
        else if (name.equals("breaker")) {
            return breaker;
        }
        else if (name.equals("hanging")) {
            return hanging;
        }
        else if (name.equals("location")) {
            return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onHangingBreaks(HangingBreakByEntityEvent event) {
        hanging = new EntityTag(event.getEntity());
        cause = new ElementTag(event.getCause().name());
        location = new LocationTag(hanging.getLocation());
        breaker = new EntityTag(event.getRemover());
        this.event = event;
        fire(event);
    }
}

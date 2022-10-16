package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import java.util.Arrays;
import java.util.HashSet;

public class EntityBreaksHangingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> breaks <hanging> (because <'cause'>)
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
    // <context.cause> returns the cause of the entity breaking. Causes list: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/hanging/HangingBreakEvent.RemoveCause.html>
    // <context.breaker> returns the EntityTag that broke the hanging entity, if any.
    // <context.hanging> returns the EntityTag of the hanging.
    //
    // @Player when the breaker is a player.
    //
    // @NPC when the breaker is an npc.
    //
    // -->

    public EntityBreaksHangingScriptEvent() {
        registerCouldMatcher("<entity> breaks <hanging> (because <'cause'>)");
    }

    public ElementTag cause;
    public EntityTag breaker;
    public EntityTag hanging;
    public LocationTag location;
    public HangingBreakByEntityEvent event;

    public static HashSet<String> notRelevantBreakables = new HashSet<>(Arrays.asList("item", "held", "block", "because"));

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (notRelevantBreakables.contains(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String entName = path.eventArgLowerAt(0);
        String hang = path.eventArgLowerAt(2);
        if (!breaker.tryAdvancedMatcher(entName)) {
            return false;
        }
        if (!hang.equals("hanging") && !hanging.tryAdvancedMatcher(hang)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        if (path.eventArgLowerAt(3).equals("because") && !path.eventArgLowerAt(4).equals(cause.asLowerString())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(breaker);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "cause":
                return cause;
            case "entity":
                BukkitImplDeprecations.entityBreaksHangingEventContext.warn();
                return breaker;
            case "breaker":
                return breaker;
            case "hanging":
                return hanging;
            case "location":
                return location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onHangingBreaks(HangingBreakByEntityEvent event) {
        hanging = new EntityTag(event.getEntity());
        cause = new ElementTag(event.getCause());
        location = new LocationTag(hanging.getLocation());
        breaker = new EntityTag(event.getRemover());
        this.event = event;
        fire(event);
    }
}

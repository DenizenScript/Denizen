package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;

public class EntityExhaustsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> exhausts
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity exhausts.
    //
    // @Switch reason:<reason> to only process the event if the reason matches a specific reason.
    //
    // @Context
    // <context.entity> returns the entity.
    // <context.exhaustion> returns the amount of exhaustion added to the entity.
    // <context.reason> returns the reason of exhaustion. See <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityExhaustionEvent.ExhaustionReason.html> for a list of valid reasons.
    //
    // @Determine
    // ElementTag(Decimal) to change the amount of exhaustion.
    //
    // @Player when the exhausting entity is a player.
    //
    // @NPC when the exhausting entity is a npc.
    //
    // @Warning This event may fire very rapidly.
    //
    // -->

    public EntityExhaustsScriptEvent() {
        registerCouldMatcher("<entity> exhausts");
        registerSwitches("reason");
    }

    public EntityExhaustionEvent event;

    public ElementTag reason;
    public EntityTag entity;

    @Override
    public boolean matches(ScriptPath path) {
        String entityName = path.eventArgLowerAt(0);
        if (!entity.tryAdvancedMatcher(entityName)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "reason", reason.asString())) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity": return entity;
            case "exhaustion": return new ElementTag(event.getExhaustion());
            case "reason": return reason;
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            ElementTag value = new ElementTag(lower);
            if (value.isFloat()) {
                event.setExhaustion(value.asFloat());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @EventHandler
    public void onEntityExhausts(EntityExhaustionEvent event) {
        reason = new ElementTag(event.getExhaustionReason().name(), true);
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}

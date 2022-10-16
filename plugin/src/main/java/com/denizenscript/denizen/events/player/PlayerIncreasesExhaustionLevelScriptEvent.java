package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;

public class PlayerIncreasesExhaustionLevelScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player exhaustion level increases
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player does an activity that increases their exhaustion level, which increases the rate of hunger.
    //
    // @Switch reason:<reason> to only process the event if the reason matches a specific reason.
    //
    // @Context
    // <context.exhaustion> returns the amount of exhaustion added to the player.
    // <context.reason> returns the reason of exhaustion. See <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityExhaustionEvent.ExhaustionReason.html> for a list of valid reasons.
    //
    // @Determine
    // ElementTag(Decimal) to change the amount of exhaustion that will be added to the player.
    //
    // @Player Always.
    //
    // @Warning This event may fire very rapidly.
    //
    // -->

    public PlayerIncreasesExhaustionLevelScriptEvent() {
        registerCouldMatcher("player exhaustion level increases");
        registerSwitches("reason");
    }

    public EntityExhaustionEvent event;

    public ElementTag reason;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "reason", reason.asString())) {
            return false;
        }
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "exhaustion": return new ElementTag(event.getExhaustion());
            case "reason": return reason;
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            ElementTag value = determinationObj.asElement();
            if (value.isFloat()) {
                event.setExhaustion(value.asFloat());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getEntity());
    }

    @EventHandler
    public void onPlayerIncreasesExhaustionLevel(EntityExhaustionEvent event) {
        if (EntityTag.isNPC(event.getEntity())) {
            return;
        }
        reason = new ElementTag(event.getExhaustionReason());
        this.event = event;
        fire(event);
    }
}

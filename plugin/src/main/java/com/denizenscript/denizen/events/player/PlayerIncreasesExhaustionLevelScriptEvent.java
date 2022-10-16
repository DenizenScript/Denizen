package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Player;
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
    // @Triggers when a player experience exhaustion.
    //
    // @Switch reason:<reason> to only process the event if the reason matches a specific reason.
    //
    // @Context
    // <context.exhaustion> returns the amount of exhaustion added to the player.
    // <context.reason> returns the reason of exhaustion. See <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityExhaustionEvent.ExhaustionReason.html> for a list of valid reasons.
    //
    // @Determine
    // ElementTag(Decimal) to change the amount of exhaustion.
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
    public PlayerTag player;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "reason", reason.asString())) {
            return false;
        }
        if (!runInCheck(path, player.getLocation())) {
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
            ElementTag value = new ElementTag(determinationObj.toString());
            if (value.isFloat()) {
                event.setExhaustion(value.asFloat());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @EventHandler
    public void onPlayerIncreasesExhaustionLevel(EntityExhaustionEvent event) {
        if (EntityTag.isNPC(event.getEntity())) {
            return;
        }
        reason = new ElementTag(event.getExhaustionReason().name(), true);
        player = new PlayerTag((Player) event.getEntity());
        this.event = event;
        fire(event);
    }
}

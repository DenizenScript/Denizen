package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class PlayerChangesXPScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes xp
    //
    // @Regex ^on player changes xp$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player's experience amount changes.
    //
    // @Context
    // <context.amount> returns the amount of changed experience.
    //
    // @Determine
    // ElementTag(Number) to set the amount of changed experience.
    //
    // @Player Always.
    //
    // -->

    public PlayerChangesXPScriptEvent() {
    }

    public PlayerExpChangeEvent event;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player changes xp");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag element && element.isInt()) {
            event.setAmount(element.asInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("amount")) {
            return new ElementTag(event.getAmount());
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            event.setAmount(0);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onPlayerChangesXP(PlayerExpChangeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        player = PlayerTag.mirrorBukkitPlayer(event.getPlayer());
        this.event = event;
        fire(event);
    }
}

package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class PlayerChangesXPScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: in area
    // <--[event]
    // @Events
    // player changes xp
    //
    // @Regex ^on player changes xp$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player's experience amount changes.
    //
    // @Context
    // <context.amount> returns the amount of changed experience.
    //
    // @Determine
    // Element(Number) to set the amount of changed experience.
    //
    // -->

    public PlayerChangesXPScriptEvent() {
        instance = this;
    }

    public static PlayerChangesXPScriptEvent instance;
    public PlayerExpChangeEvent event;
    public int amount;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player changes xp");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerChangesXP";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (aH.matchesInteger(determination)) {
            amount = aH.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("amount")) {
            return new Element(amount);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesXP(PlayerExpChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        amount = event.getAmount();
        player = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        this.event = event;
        cancelled = false;
        fire(event);
        if (cancelled) {
            amount = 0;
        }
        event.setAmount(amount);
    }
}

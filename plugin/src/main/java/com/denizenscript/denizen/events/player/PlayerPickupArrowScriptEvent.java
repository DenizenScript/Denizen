package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class PlayerPickupArrowScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player picks up launched arrow
    //
    // @Regex ^on player picks up launched arrow$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player picks up a launched arrow projectile entity that is embedded into the ground. Will not necessarily fire for creative players.
    //
    // @Context
    // <context.arrow> returns the arrow entity.
    // <context.item> returns the item of the arrow.
    //
    // @Player Always.
    //
    // -->

    public PlayerPickupArrowScriptEvent() {
    }


    public PlayerPickupArrowEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player picks up launched arrow")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("arrow")) {
            return new EntityTag(event.getArrow());
        }
        else if (name.equals("item")) {
            return new ItemTag(event.getItem().getItemStack());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerConsumes(PlayerPickupArrowEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}

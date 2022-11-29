package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class PlayerClosesInvScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player closes inventory
    // player closes <inventory>
    //
    // @Regex ^on player closes [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Triggers when a player closes an inventory. (EG, chests, not the player's main inventory.)
    //
    // @Context
    // <context.inventory> returns the InventoryTag.
    //
    // @Player Always.
    //
    // -->

    public PlayerClosesInvScriptEvent() {
    }


    public InventoryTag inventory;
    private PlayerTag player;
    public InventoryCloseEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player closes ")) {
            return false;
        }
        if (!couldMatchInventory(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, inventory)) {
            return false;
        }
        LocationTag loc = inventory.getLocation();
        if (loc == null) {
            loc = player.getLocation();
        }
        if (!runInCheck(path, loc)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("inventory")) {
            return inventory;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerClosesInv(InventoryCloseEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        player = new PlayerTag((Player) event.getPlayer());
        this.event = event;
        fire(event);
    }
}

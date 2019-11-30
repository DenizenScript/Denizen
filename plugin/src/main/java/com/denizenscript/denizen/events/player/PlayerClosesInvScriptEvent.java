package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
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

    // TODO: in area
    // <--[event]
    // @Events
    // player closes inventory
    // player closes <inventory>
    //
    // @Regex ^on player closes [^\s]+$
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
        instance = this;
    }

    public static PlayerClosesInvScriptEvent instance;

    public InventoryTag inventory;
    private PlayerTag player;
    public InventoryCloseEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player closes ");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryInventory(inventory, path.eventArgLowerAt(2))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerClosesInv";
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

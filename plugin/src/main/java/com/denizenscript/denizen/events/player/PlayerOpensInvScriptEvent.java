package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class PlayerOpensInvScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: in area
    // <--[event]
    // @Events
    // player opens inventory
    // player opens <inventory>
    //
    // @Regex ^on player opens [^\s]+$
    //
    // @Triggers when a player opens an inventory. (EG, chests, not the player's main inventory.)
    //
    // @Context
    // <context.inventory> returns the InventoryTag.
    //
    // @Player Always.
    //
    // -->

    public PlayerOpensInvScriptEvent() {
        instance = this;
    }

    public static PlayerOpensInvScriptEvent instance;

    public InventoryTag inventory;
    public InventoryOpenEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player opens");
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
        return "PlayerOpensInv";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(event != null ? EntityTag.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("inventory")) {
            return inventory;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerOpensInv(InventoryOpenEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        this.event = event;
        fire(event);
    }
}

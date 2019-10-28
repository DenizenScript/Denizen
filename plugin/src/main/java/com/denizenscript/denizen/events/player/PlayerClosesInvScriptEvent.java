package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // -->

    public PlayerClosesInvScriptEvent() {
        instance = this;
    }

    public static PlayerClosesInvScriptEvent instance;

    public InventoryTag inventory;
    private EntityTag entity;
    public InventoryCloseEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player closes ");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String entName = path.eventArgLowerAt(0);
        if (entName.equals("player") && !entity.isPlayer()) {
            return false;
        }
        String inv = path.eventArgLowerAt(2);
        String nname = NotableManager.isSaved(inventory) ?
                CoreUtilities.toLowerCase(NotableManager.getSavedId(inventory)) :
                "\0";
        if (!inv.equals("inventory")
                && !inv.equals(CoreUtilities.toLowerCase(inventory.getInventoryType().name()))
                && !inv.equals(CoreUtilities.toLowerCase(inventory.getIdHolder()))
                && !(inv.equals("notable") && !nname.equals("\0"))
                && !inv.equals(nname)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerClosesInv";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player?
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null, null);
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
        entity = new EntityTag(event.getPlayer());
        this.event = event;
        fire(event);
    }
}

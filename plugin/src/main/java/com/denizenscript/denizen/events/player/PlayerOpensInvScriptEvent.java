package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // -->

    public PlayerOpensInvScriptEvent() {
        instance = this;
    }

    public static PlayerOpensInvScriptEvent instance;

    public InventoryTag inventory;
    public InventoryOpenEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player opens");
    }

    @Override
    public boolean matches(ScriptPath path) {
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
        return "PlayerOpensInv";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
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

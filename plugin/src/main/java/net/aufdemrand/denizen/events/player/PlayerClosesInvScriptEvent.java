package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
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
    //
    // @Triggers when a player closes an inventory. (EG, chests, not the player's main inventory.)
    //
    // @Context
    // <context.inventory> returns the dInventory.
    //
    // -->

    public PlayerClosesInvScriptEvent() {
        instance = this;
    }

    public static PlayerClosesInvScriptEvent instance;

    public dInventory inventory;
    private dEntity entity;
    public InventoryCloseEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player closes ");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String entName = CoreUtilities.getXthArg(0, lower);
        if (entName.equals("player") && !entity.isPlayer()) {
            return false;
        }
        String inv = CoreUtilities.getXthArg(2, lower);
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
    public void destroy() {
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player?
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("inventory")) {
            return inventory;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerClosesInv(InventoryCloseEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        entity = new dEntity(event.getPlayer());
        this.event = event;
        fire();
    }
}

package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class PlayerOpensInvScriptEvent extends ScriptEvent implements Listener {

    // TODO: in area
    // <--[event]
    // @Events
    // player opens inventory
    // player opens <inventory type>
    //
    // @Regex ^on player opens [^\s]+$
    //
    // @Triggers when a player opens an inventory. (EG, chests, not the player's main inventory.)
    //
    // @Context
    // <context.inventory> returns the dInventory.
    //
    // -->

    public PlayerOpensInvScriptEvent() {
        instance = this;
    }

    public static PlayerOpensInvScriptEvent instance;

    public dInventory inventory;
    public InventoryOpenEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player opens");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String inv = CoreUtilities.getXthArg(2, lower);
        if (!inv.equals("inventory")
                && !inv.equals(CoreUtilities.toLowerCase(inventory.getInventoryType().name()))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerOpensInv";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        InventoryOpenEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(event != null ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("inventory")) {
            return inventory;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOpensInv(InventoryOpenEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}

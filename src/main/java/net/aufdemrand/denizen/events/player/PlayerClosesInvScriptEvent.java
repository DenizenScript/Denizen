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
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;

public class PlayerClosesInvScriptEvent extends ScriptEvent implements Listener {

    // TODO: in area
    // <--[event]
    // @Events
    // player closes inventory
    // player closes <inventory type>
    // npc closes inventory
    // npc closes <inventory type>
    //
    // @Regex ^on (player|npc) closes [^\s]+$
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
        String entName = CoreUtilities.getXthArg(0, lower);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("closes")
                && (entName.equals("player") || entName.equals("npc"));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String entName = CoreUtilities.getXthArg(0, lower);
        if (entName.equals("player") && !entity.isPlayer()) {
            return false;
        }
        if (entName.equals("npc") && !entity.isCitizensNPC()) {
            return false;
        }

        String inv = CoreUtilities.getXthArg(2, lower);
        if (!inv.equals("inventory")
                && !inv.equals(CoreUtilities.toLowerCase(inventory.getInventoryType().name()))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerClosesInv";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
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
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null,
                entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("inventory", inventory);
        return context;
    }

    @EventHandler
    public void onPlayerClosesInv(InventoryCloseEvent event) {
        inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        entity = new dEntity(event.getPlayer());
        this.event = event;
        fire();
    }
}

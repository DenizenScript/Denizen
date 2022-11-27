package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

public class PlayerPreparesGrindstoneCraftScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player prepares grindstone craft item
    // player prepares grindstone craft <item>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Triggers when a player prepares to grind an item.
    //
    // @Context
    // <context.inventory> returns the InventoryTag of the grindstone inventory.
    // <context.result> returns the ItemTag to be crafted.
    //
    // @Determine
    // "RESULT:" + ItemTag to change the item that is crafted.
    //
    // @Player Always.
    //
    // @Warning The player doing the grinding is estimated and may be inaccurate.
    //
    // -->


    public PlayerPreparesGrindstoneCraftScriptEvent() {
        registerCouldMatcher("player prepares grindstone craft <item>");
    }

    public PrepareResultEvent event;
    public ItemTag item;
    public InventoryTag inventory;
    public PlayerTag player;

    @Override
    public boolean matches(ScriptPath path) {
        if (!item.tryAdvancedMatcher(path.eventArgLowerAt(3))) {
            return false;
        }
        if (!runInCheck(path, inventory.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("result:")) {
                ItemTag result = ItemTag.valueOf(lower.substring("result:".length()), path.container);
                event.setResult(result.getItemStack());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "inventory": return inventory;
            case "result": return new ItemTag(event.getResult());
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @EventHandler
    public void onPlayerPreparesGrindStoneCraft(PrepareResultEvent event) {
        inventory = InventoryTag.mirrorBukkitInventory(event.getInventory());
        if ((inventory.getInventoryType() != InventoryType.GRINDSTONE)) {
            return;
        }
        if (event.getInventory().getViewers().isEmpty()) {
            return;
        }
        HumanEntity humanEntity = inventory.getInventory().getViewers().get(0);
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        player = EntityTag.getPlayerFrom(humanEntity);
        item = new ItemTag(event.getResult());
        this.event = event;
        fire(event);
    }
}

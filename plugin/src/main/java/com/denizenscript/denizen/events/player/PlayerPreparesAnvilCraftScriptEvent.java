package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class PlayerPreparesAnvilCraftScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player prepares anvil craft item
    // player prepares anvil craft <item>
    //
    // @Regex ^on player prepares anvil craft [^\s]+$
    //
    // @Group Player
    //
    // @Triggers when a player prepares an anvil to craft an item.
    //
    // @Warning The player doing the crafting is estimated and may be inaccurate.
    //
    // @Context
    // <context.inventory> returns the InventoryTag of the anvil inventory.
    // <context.item> returns the ItemTag to be crafted.
    // <context.repair_cost> returns an ElementTag(Number) of the repair cost.
    // <context.new_name> returns an ElementTag of the new name.
    //
    // @Determine
    // ElementTag(Number) to set the repair cost.
    // ItemTag to change the item that is crafted.
    //
    // @Player Always.
    //
    // -->

    public PlayerPreparesAnvilCraftScriptEvent() {
    }

    public PrepareAnvilEvent event;
    public ItemTag result;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player prepares anvil craft")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(4))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(4, result)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag element && element.isInt()) {
            event.getInventory().setRepairCost(element.asInt());
            return true;
        }
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            result = ItemTag.valueOf(determination, path.container);
            event.setResult(result.getItemStack());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item":
                return result;
            case "repair_cost":
                return new ElementTag(event.getInventory().getRepairCost());
            case "new_name":
                return new ElementTag(event.getInventory().getRenameText());
            case "inventory":
                return InventoryTag.mirrorBukkitInventory(event.getInventory());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCraftItem(PrepareAnvilEvent event) {
        if (event.getInventory().getViewers().isEmpty()) {
            return;
        }
        HumanEntity humanEntity = event.getInventory().getViewers().get(0);
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        this.event = event;
        result = new ItemTag(event.getResult());
        this.player = EntityTag.getPlayerFrom(humanEntity);
        fire(event);
    }
}

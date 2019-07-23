package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;

public class PlayerPreparesAnvilCraftScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player prepares anvil craft item
    // player prepares anvil craft <item>
    //
    // @Regex ^on player prepares anvil craft [^\s]+$
    //
    // @Triggers when a player prepares an anvil to craft an item.
    //
    // @Warning The player doing the crafting is estimated and may be inaccurate.
    //
    // @Context
    // <context.inventory> returns the InventoryTag of the anvil inventory.
    // <context.item> returns the ItemTag to be crafted.
    // <context.repair_cost> returns an Element(Number) of the repair cost.
    // <context.new_name> returns an ElementTag of the new name.
    //
    // @Determine
    // Element(Number) to set the repair cost.
    // ItemTag to change the item that is crafted.
    //
    // -->

    public PlayerPreparesAnvilCraftScriptEvent() {
        instance = this;
    }

    public static PlayerPreparesAnvilCraftScriptEvent instance;
    public boolean resultChanged;
    public ItemTag result;
    public AnvilInventory inventory;
    public PlayerTag player;
    public ElementTag repairCost;
    public ElementTag newName;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player prepares anvil craft");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String eItem = path.eventArgLowerAt(4);

        if (!tryItem(result, eItem)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerPreparesAnvilCraft";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            repairCost = (ElementTag) determinationObj;
            return true;
        }
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            result = ItemTag.valueOf(determination, path.container);
            resultChanged = true;
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
        if (name.equals("item")) {
            return result;
        }
        else if (name.equals("repair_cost")) {
            return repairCost;
        }
        else if (name.equals("new_name")) {
            return newName;
        }
        else if (name.equals("inventory")) {
            return InventoryTag.mirrorBukkitInventory(inventory);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCraftItem(PrepareAnvilEvent event) {
        if (event.getInventory().getViewers().size() == 0) {
            return;
        }
        HumanEntity humanEntity = event.getInventory().getViewers().get(0);
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        inventory = event.getInventory();
        repairCost = new ElementTag(inventory.getRepairCost());
        newName = new ElementTag(inventory.getRenameText());
        result = new ItemTag(event.getResult());
        this.player = EntityTag.getPlayerFrom(humanEntity);
        this.resultChanged = false;
        this.cancelled = false;
        fire(event);
        inventory.setRepairCost(repairCost.asInt());
        if (resultChanged) {
            event.setResult(result.getItemStack());
        }
    }
}

package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;

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
// <context.inventory> returns the dInventory of the anvil inventory.
// <context.item> returns the dItem to be crafted.
// <context.repair_cost> returns an Element(Number) of the repair cost.
// <context.new_name> returns an Element of the new name.
//
// @Determine
// Element(Number) to set the repair cost.
// dItem to change the item that is crafted.
//
// -->

public class PlayerPreparesAnvilCraftScriptEvent extends BukkitScriptEvent implements Listener {

    public PlayerPreparesAnvilCraftScriptEvent() {
        instance = this;
    }

    public static PlayerPreparesAnvilCraftScriptEvent instance;
    public boolean resultChanged;
    public dItem result;
    public AnvilInventory inventory;
    public dPlayer player;
    public Element repairCost;
    public Element newName;

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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            repairCost = new Element(determination);
            return true;
        }
        else if (dItem.matches(determination)) {
            result = dItem.valueOf(determination, container);
            resultChanged = true;
            return true;
        }
        else {
            return super.applyDetermination(container, determination);
        }
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
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
            return dInventory.mirrorBukkitInventory(inventory);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCraftItem(PrepareAnvilEvent event) {
        if (event.getInventory().getViewers().size() == 0) {
            return;
        }
        HumanEntity humanEntity = event.getInventory().getViewers().get(0);
        if (dEntity.isNPC(humanEntity)) {
            return;
        }
        inventory = event.getInventory();
        repairCost = new Element(inventory.getRepairCost());
        newName = new Element(inventory.getRenameText());
        result = new dItem(event.getResult());
        this.player = dEntity.getPlayerFrom(humanEntity);
        this.resultChanged = false;
        this.cancelled = false;
        fire(event);
        inventory.setRepairCost(repairCost.asInt());
        if (resultChanged) {
            event.setResult(result.getItemStack());
        }
    }
}

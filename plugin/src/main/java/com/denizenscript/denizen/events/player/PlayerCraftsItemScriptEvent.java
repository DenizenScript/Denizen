package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dInventory;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class PlayerCraftsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player crafts item
    // player crafts <item>
    //
    // @Regex ^on player crafts [^\s]+$
    //
    // @Cancellable true
    //
    // @Triggers when a player fully crafts an item.
    // @Context
    // <context.inventory> returns the dInventory of the crafting inventory.
    // <context.item> returns the dItem to be crafted.
    // <context.recipe> returns a dList of dItems in the recipe.
    //
    // @Determine
    // dItem to change the item that is crafted.
    //
    // -->

    public PlayerCraftsItemScriptEvent() {
        instance = this;
    }

    public static PlayerCraftsItemScriptEvent instance;
    public boolean resultChanged;
    public dItem result;
    public dList recipe;
    public CraftingInventory inventory;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.getXthArg(0, lower).equals("player") && CoreUtilities.getXthArg(1, lower).equals("crafts");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String eItem = path.eventArgLowerAt(2);

        if (!tryItem(result, eItem)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerCraftsItem";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dItem.matches(determination)) {
            result = dItem.valueOf(determination, container);
            resultChanged = true;
            return true;
        }

        return super.applyDetermination(container, determination);
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
        else if (name.equals("inventory")) {
            return dInventory.mirrorBukkitInventory(inventory);
        }
        else if (name.equals("recipe")) {
            return recipe;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (dEntity.isNPC(humanEntity)) {
            return;
        }
        Recipe eRecipe = event.getRecipe();
        if (eRecipe == null || eRecipe.getResult() == null) {
            return;
        }
        inventory = event.getInventory();
        result = new dItem(eRecipe.getResult());
        recipe = new dList();
        for (ItemStack itemStack : inventory.getMatrix()) {
            if (itemStack != null) {
                recipe.add(new dItem(itemStack).identify());
            }
            else {
                recipe.add(new dItem(Material.AIR).identify());
            }
        }
        this.player = dEntity.getPlayerFrom(humanEntity);
        this.resultChanged = false;
        this.cancelled = false;
        fire(event);
        if (cancelled) { // This event has a weird cancellation handler
            event.setCancelled(true);
        }
        else if (resultChanged) {
            event.setCurrentItem(result.getItemStack());
        }
    }
}

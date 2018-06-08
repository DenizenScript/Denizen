package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

// <--[event]
// @Events
// item recipe formed
// <item> recipe formed
// <material> recipe formed
//
// @Regex ^on [^\s]+ recipe formed$
//
// @Cancellable true
//
// @Triggers when an item's recipe is correctly formed.
// @Context
// <context.inventory> returns the dInventory of the crafting inventory.
// <context.item> returns the dItem to be formed in the result slot.
// <context.recipe> returns a dList of dItems in the recipe.
//
// @Determine
// dItem to change the item that is formed in the result slot.
//
// -->

public class ItemRecipeFormedScriptEvent extends BukkitScriptEvent implements Listener {

    public ItemRecipeFormedScriptEvent() {
        instance = this;
    }

    public static ItemRecipeFormedScriptEvent instance;

    public boolean resultChanged;
    public dItem result;
    public dList recipe;
    public CraftingInventory inventory;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return (CoreUtilities.getXthArg(1, lower).equals("recipe") && CoreUtilities.getXthArg(2, lower).equals("formed"))
                || CoreUtilities.getXthArg(1, lower).equals("crafted");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String eItem = CoreUtilities.getXthArg(0, lower);

        if (!tryItem(result, eItem)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "ItemRecipeFormed";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PrepareItemCraftEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dItem.matches(determination)) {
            result = dItem.valueOf(determination);
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
        else if (name.equals("inventory")) {
            return dInventory.mirrorBukkitInventory(inventory);
        }
        else if (name.equals("recipe")) {
            return recipe;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRecipeFormed(PrepareItemCraftEvent event) {
        HumanEntity humanEntity = event.getView().getPlayer();
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
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                recipe.add(new dItem(itemStack).identify());
            }
            else {
                recipe.add(new dItem(Material.AIR).identify());
            }
        }
        player = dEntity.getPlayerFrom(humanEntity);
        resultChanged = false;
        cancelled = false;
        fire();
        if (cancelled) {
            inventory.setResult(null);
        }
        else if (resultChanged) {
            inventory.setResult(result.getItemStack());
        }
    }
}

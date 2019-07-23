package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
// <context.inventory> returns the InventoryTag of the crafting inventory.
// <context.item> returns the ItemTag to be formed in the result slot.
// <context.recipe> returns a ListTag of ItemTags in the recipe.
//
// @Determine
// ItemTag to change the item that is formed in the result slot.
//
// -->

public class ItemRecipeFormedScriptEvent extends BukkitScriptEvent implements Listener {

    public ItemRecipeFormedScriptEvent() {
        instance = this;
    }

    public static ItemRecipeFormedScriptEvent instance;

    public boolean resultChanged;
    public ItemTag result;
    public ListTag recipe;
    public CraftingInventory inventory;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return (CoreUtilities.getXthArg(1, lower).equals("recipe") && CoreUtilities.getXthArg(2, lower).equals("formed"))
                || CoreUtilities.getXthArg(1, lower).equals("crafted");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String eItem = path.eventArgLowerAt(0);

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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            result = ItemTag.valueOf(determination, path.container);
            resultChanged = true;
            return true;
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
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
        else if (name.equals("inventory")) {
            return InventoryTag.mirrorBukkitInventory(inventory);
        }
        else if (name.equals("recipe")) {
            return recipe;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onRecipeFormed(PrepareItemCraftEvent event) {
        HumanEntity humanEntity = event.getView().getPlayer();
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        Recipe eRecipe = event.getRecipe();
        if (eRecipe == null || eRecipe.getResult() == null) {
            return;
        }
        inventory = event.getInventory();
        result = new ItemTag(eRecipe.getResult());
        recipe = new ListTag();
        for (ItemStack itemStack : inventory.getMatrix()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                recipe.add(new ItemTag(itemStack).identify());
            }
            else {
                recipe.add(new ItemTag(Material.AIR).identify());
            }
        }
        player = EntityTag.getPlayerFrom(humanEntity);
        resultChanged = false;
        cancelled = false;
        fire(event);
        if (cancelled) { // Hacked-in cancellation helper
            inventory.setResult(null);
        }
        else if (resultChanged) {
            inventory.setResult(result.getItemStack());
        }
    }
}

package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    // <context.inventory> returns the InventoryTag of the crafting inventory.
    // <context.item> returns the ItemTag to be crafted.
    // <context.recipe> returns a ListTag of ItemTags in the recipe.
    //
    // @Determine
    // ItemTag to change the item that is crafted.
    //
    // @Player Always.
    //
    // -->

    public PlayerCraftsItemScriptEvent() {
        instance = this;
    }

    public static PlayerCraftsItemScriptEvent instance;
    public boolean resultChanged;
    public ItemTag result;
    public ListTag recipe;
    public CraftingInventory inventory;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(0).equals("player") && path.eventArgLowerAt(1).equals("crafts");
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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
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
        else if (name.equals("inventory")) {
            return InventoryTag.mirrorBukkitInventory(inventory);
        }
        else if (name.equals("recipe")) {
            return recipe;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
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
            if (itemStack != null) {
                recipe.add(new ItemTag(itemStack).identify());
            }
            else {
                recipe.add(new ItemTag(Material.AIR).identify());
            }
        }
        this.player = EntityTag.getPlayerFrom(humanEntity);
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

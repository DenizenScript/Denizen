package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.inventory.RecipeHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
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
    // <context.amount> returns the amount of the item that will be crafted (usually 1, except when shift clicked. Can be above 64).
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
    public CraftItemEvent event;
    public ItemTag result;
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

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerCraftsItem";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            event.setCurrentItem(ItemTag.valueOf(determination, path.container).getItemStack());
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
            return InventoryTag.mirrorBukkitInventory(event.getInventory());
        }
        else if (name.equals("amount")) {
            int amount = event.getRecipe().getResult().getAmount();
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                amount *= RecipeHelper.getMaximumOutputQuantity(event.getRecipe(), event.getInventory());
            }
            return new ElementTag(amount);
        }
        else if (name.equals("recipe")) {
            ListTag recipe = new ListTag();
            for (ItemStack itemStack : event.getInventory().getMatrix()) {
                if (itemStack != null) {
                    recipe.addObject(new ItemTag(itemStack));
                }
                else {
                    recipe.addObject(new ItemTag(Material.AIR));
                }
            }
            return recipe;
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled) { // This event has a weird cancellation handler
            event.setCancelled(true);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR && !event.getCursor().isSimilar(event.getCurrentItem())) {
            // This event fires even when nothing crafts due to a cursor item, so disregard those cases.
            return;
        }
        HumanEntity humanEntity = event.getWhoClicked();
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        Recipe eRecipe = event.getRecipe();
        this.event = event;
        result = new ItemTag(eRecipe.getResult());
        this.player = EntityTag.getPlayerFrom(humanEntity);
        this.cancelled = false;
        fire(event);
    }
}

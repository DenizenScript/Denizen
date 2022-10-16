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
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerCraftsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player crafts item
    // player crafts <item>
    //
    // @Regex ^on player crafts [^\s]+$
    //
    // @Group Player
    //
    // @Cancellable true
    //
    // @Triggers when a player fully crafts an item.
    // @Context
    // <context.inventory> returns the InventoryTag of the crafting inventory.
    // <context.item> returns the ItemTag to be crafted.
    // <context.amount> returns the amount of the item that will be crafted (usually 1, except when shift clicked. Can be above 64).
    // <context.recipe> returns a ListTag of ItemTags in the recipe.
    // <context.recipe_id> returns the ID of the recipe that is being crafted.
    // <context.click_type> returns an ElementTag with the name of the click type. Click type list: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/ClickType.html>
    //
    // @Determine
    // ItemTag to change the item that is crafted.
    //
    // @Player Always.
    //
    // -->

    public PlayerCraftsItemScriptEvent() {
    }

    public CraftItemEvent event;
    public ItemTag result;
    public PlayerTag player;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgsLowEqualStartingAt(0, "player", "crafts")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!result.tryAdvancedMatcher(path.eventArgLowerAt(2))) {
            return false;
        }
        return super.matches(path);
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
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                amount *= RecipeHelper.getMaximumOutputQuantity(event.getRecipe(), event.getInventory());
            }
            return new ElementTag(amount);
        }
        else if (name.equals("click_type")) {
            return new ElementTag(event.getClick());
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
        else if (name.equals("recipe_id") && event.getRecipe() instanceof Keyed) {
            return new ElementTag(((Keyed) event.getRecipe()).getKey().toString());
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
        // This event fires even when nothing crafts due to a cursor item, so disregard those cases.
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            if (!event.getCursor().isSimilar(event.getCurrentItem())) {
                return;
            }
            if (event.getCursor().getAmount() + event.getRecipe().getResult().getAmount() > event.getCursor().getType().getMaxStackSize()) {
                return;
            }
        }
        HumanEntity humanEntity = event.getWhoClicked();
        if (EntityTag.isNPC(humanEntity)) {
            return;
        }
        this.event = event;
        result = new ItemTag(event.getInventory().getResult());
        if (result.getBukkitMaterial() == Material.AIR) {
            result = new ItemTag(event.getRecipe().getResult());
        }
        this.player = EntityTag.getPlayerFrom(humanEntity);
        this.cancelled = false;
        fire(event);
    }
}

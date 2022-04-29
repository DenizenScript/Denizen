package com.denizenscript.denizen.events.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class ItemRecipeFormedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <item> recipe formed
    //
    // @Group Item
    //
    // @Cancellable true
    //
    // @Triggers when an item's recipe is correctly formed.
    // @Context
    // <context.inventory> returns the InventoryTag of the crafting inventory.
    // <context.item> returns the ItemTag to be formed in the result slot.
    // <context.recipe> returns a ListTag of ItemTags in the recipe.
    // <context.recipe_id> returns the ID of the recipe that was formed.
    //
    // @Determine
    // ItemTag to change the item that is formed in the result slot.
    //
    // @Player Always.
    //
    // -->

    public ItemRecipeFormedScriptEvent() {
        instance = this;
        registerCouldMatcher("<item> recipe formed");
    }

    public static ItemRecipeFormedScriptEvent instance;

    public PrepareItemCraftEvent event;
    public ItemTag result;

    @Override
    public boolean matches(ScriptPath path) {
        if (!result.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ItemRecipeFormed";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj.canBeType(ItemTag.class)) {
            ItemTag result = determinationObj.asType(ItemTag.class, getTagContext(path));
            event.getInventory().setResult(result.getItemStack());
            return true;
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(EntityTag.getPlayerFrom(event.getView().getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item": return result;
            case "inventory": return InventoryTag.mirrorBukkitInventory(event.getInventory());
            case "recipe": {
                ListTag recipe = new ListTag();
                for (ItemStack itemStack : event.getInventory().getMatrix()) {
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        recipe.addObject(new ItemTag(itemStack));
                    }
                    else {
                        recipe.addObject(new ItemTag(Material.AIR));
                    }
                }
                return recipe;
            }
            case "recipe_id":
                if (event.getRecipe() instanceof Keyed) {
                    return new ElementTag(((Keyed) event.getRecipe()).getKey().toString());
                }
                break;
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled) { // Hacked-in cancellation helper
            event.getInventory().setResult(null);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onRecipeFormed(PrepareItemCraftEvent event) {
        this.event = event;
        if (event.getRecipe() == null) {
            return;
        }
        result = new ItemTag(event.getInventory().getResult());
        if (result.getBukkitMaterial() == Material.AIR) {
            result = new ItemTag(event.getRecipe().getResult());
        }
        cancelled = false;
        fire(event);
    }
}

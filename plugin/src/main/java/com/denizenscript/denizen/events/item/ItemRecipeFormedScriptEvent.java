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

import java.util.Arrays;

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
    // <context.is_repair> returns an ElementTag(Boolean) of whether the event was triggered by a tool repair operation rather than a crafting recipe.
    //
    // @Determine
    // ItemTag to change the item that is formed in the result slot.
    //
    // @Player Always.
    //
    // -->

    public ItemRecipeFormedScriptEvent() {
        registerCouldMatcher("<item> recipe formed");
        this.<ItemRecipeFormedScriptEvent, ObjectTag>registerOptionalDetermination(null, ObjectTag.class, (evt, context, determination) -> {
            if (determination.canBeType(ItemTag.class)) {
                ItemTag result = determination.asType(ItemTag.class, context);
                evt.event.getInventory().setResult(result.getItemStack());
                return true;
            }
            return false;
        });
    }


    public PrepareItemCraftEvent event;
    public ItemTag result;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, result)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(EntityTag.getPlayerFrom(event.getView().getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "item" -> result;
            case "inventory" -> InventoryTag.mirrorBukkitInventory(event.getInventory());
            case "recipe" -> new ListTag(Arrays.asList(event.getInventory().getMatrix()), ItemTag::new);
            case "recipe_id" -> event.getRecipe() instanceof Keyed keyed ? new ElementTag(keyed.getKey().toString(), true): null;
            case "is_repair" -> new ElementTag(event.isRepair());
            default -> super.getContext(name);
        };
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

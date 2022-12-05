package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerSelectsStonecutterRecipeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player selects stonecutter recipe
    //
    // @Group Player
    //
    // @Cancellable true
    //
    // @Location true
    //
    // @Triggers when a player selects a recipe in a stonecutter.
    //
    // @Switch recipe_id:<recipe_id> to only process the event if the recipe matches the recipe ID.
    //
    // @Context
    // <context.inventory> returns the InventoryTag of the stonecutter inventory.
    // <context.input> returns an ItemTag of the item in the input slot.
    // <context.result> returns an ItemTag of the item in the result slot.
    // <context.recipe_id> returns the ID of the recipe that was selected.
    //
    // @Player Always.
    //
    // -->

    public PlayerSelectsStonecutterRecipeScriptEvent() {
        registerCouldMatcher("player selects stonecutter recipe");
        registerSwitches("recipe_id");
    }

    public PlayerStonecutterRecipeSelectEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getStonecutterInventory().getLocation())) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "recipe_id", event.getStonecuttingRecipe().getKey().toString())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "inventory": return InventoryTag.mirrorBukkitInventory(event.getStonecutterInventory());
            case "input": return new ItemTag(event.getStonecutterInventory().getInputItem());
            case "result": return new ItemTag(event.getStonecuttingRecipe().getResult());
            case "recipe_id": return new ElementTag(String.valueOf(event.getStonecuttingRecipe().getKey()));
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSelectsStonecutterRecipe(PlayerStonecutterRecipeSelectEvent event) {
        this.event = event;
        fire(event);
    }
}

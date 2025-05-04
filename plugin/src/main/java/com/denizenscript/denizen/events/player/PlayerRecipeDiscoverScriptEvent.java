package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;

public class PlayerRecipeDiscoverScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player discovers recipe
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player discovers a new item in the recipe book.
    //
    // @Context
    // <context.recipe_id> returns the ID of the recipe discovered.
    //
    // @Player Always.
    //
    // -->

    public PlayerRecipeDiscoverScriptEvent() {
        registerCouldMatcher("player discovers recipe");
    }

    public PlayerRecipeDiscoverEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "recipe_id" -> new ElementTag(event.getRecipe().toString(), true);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerDiscoversRecipe(PlayerRecipeDiscoverEvent event) {
        this.event = event;
        fire(event);
    }
}

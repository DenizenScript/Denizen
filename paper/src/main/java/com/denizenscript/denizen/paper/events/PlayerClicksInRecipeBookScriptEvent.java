package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerClicksInRecipeBookScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player uses recipe book
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player interacts with their recipe book.
    //
    // @Context
    // <context.recipe> returns the key of the recipe that was clicked.
    // <context.is_all> returns 'true' if the player is trying to make the maximum amount of items from the recipe, otherwise 'false'.
    //
    // @Player Always.
    //
    // -->

    public PlayerClicksInRecipeBookScriptEvent() {
        registerCouldMatcher("player uses recipe book");
    }

    public PlayerRecipeBookClickEvent event;

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
        switch (name) {
            case "recipe":
                return new ElementTag(event.getRecipe().toString());
            case "is_all":
                return new ElementTag(event.isMakeAll());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerUsesRecipeBook(PlayerRecipeBookClickEvent event) {
        this.event = event;
        fire(event);
    }
}

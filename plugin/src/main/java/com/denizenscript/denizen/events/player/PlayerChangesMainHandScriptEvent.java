package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.inventory.MainHand;

public class PlayerChangesMainHandScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes main hand
    //
    // @Group Player
    //
    // @Triggers when a player changes their main hand.
    //
    // @Context
    // <context.old_hand> returns the player's old main hand, either LEFT or RIGHT.
    // <context.new_hand> returns the player's new main hand.
    //
    // @Player Always.
    //
    // -->

    public PlayerChangesMainHandScriptEvent() {
        registerCouldMatcher("player changes main hand");
    }

    public PlayerChangedMainHandEvent event;

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            // workaround for spigot bug: getMainHand returns the old value, despite being documented as returning the new value
            case "old_hand": return new ElementTag(event.getMainHand().toString());
            case "new_hand": return new ElementTag(event.getMainHand() == MainHand.LEFT ? "RIGHT" : "LEFT");
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesMainHand(PlayerChangedMainHandEvent event) {
        this.event = event;
        fire(event);
    }
}

package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.inventory.MainHand;

import java.util.Arrays;

public class PlayerChangesMainHandScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes main hand
    //
    // @Regex ^on player changes main hand$
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
        instance = this;
    }

    public static PlayerChangesMainHandScriptEvent instance;
    public PlayerChangedMainHandEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player changes main hand")) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerChangesMainHand";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "old_hand":
                return new ElementTag(event.getMainHand().toString());
            // workaround for spigot bug
            case "new_hand":
                return new ElementTag(event.getMainHand() == MainHand.LEFT ? "RIGHT" : "LEFT");
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesMainHand(PlayerChangedMainHandEvent event) {
        this.event = event;
        fire(event);
    }

}

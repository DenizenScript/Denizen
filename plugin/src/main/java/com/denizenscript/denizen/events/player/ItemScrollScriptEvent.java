package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class ItemScrollScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player scrolls their hotbar
    // player holds item
    //
    // @Regex ^on player (scrolls their hotbar|holds item)$
    //
    // @Switch in <area>
    // @Switch item <item>
    //
    // @Cancellable true
    //
    // @Triggers when a player scrolls through their hotbar.
    //
    // @Context
    // <context.new_slot> returns the number of the new inventory slot.
    // <context.previous_slot> returns the number of the old inventory slot.
    //
    // -->

    public ItemScrollScriptEvent() {
        instance = this;
    }

    public static ItemScrollScriptEvent instance;

    public PlayerItemHeldEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player holds item")
                || path.eventLower.startsWith("player scrolls their hotbar");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        if (path.switches.containsKey("item") && !tryItem(new ItemTag(event.getPlayer().getInventory().getItem(event.getNewSlot())), path.switches.get("item"))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerScrollsItem";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("new_slot")) {
            return new ElementTag(event.getNewSlot() + 1);
        }
        else if (name.equals("previous_slot")) {
            return new ElementTag(event.getPreviousSlot() + 1);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerScrollsHotbar(PlayerItemHeldEvent event) {
        this.event = event;
        fire(event);
    }
}

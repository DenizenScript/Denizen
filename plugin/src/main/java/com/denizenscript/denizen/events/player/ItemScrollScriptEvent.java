package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class ItemScrollScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: in area
    // TODO: item:x switch
    // <--[event]
    // @Events
    // player scrolls their hotbar
    // player holds item
    //
    // @Regex ^on player (scrolls their hotbar|holds item)$
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

    public ElementTag new_slot;
    public ElementTag previous_slot;
    public PlayerItemHeldEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player holds item")
                || lower.startsWith("player scrolls their hotbar");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerScrollsItem";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("new_slot")) {
            return new_slot;
        }
        else if (name.equals("previous_slot")) {
            return previous_slot;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerScrollsHotbar(PlayerItemHeldEvent event) {
        new_slot = new ElementTag(event.getNewSlot() + 1);
        previous_slot = new ElementTag(event.getPreviousSlot() + 1);
        this.event = event;
        fire(event);
    }
}

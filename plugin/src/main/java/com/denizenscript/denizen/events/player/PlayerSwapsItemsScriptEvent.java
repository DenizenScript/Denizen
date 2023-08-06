package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapsItemsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player swaps items
    //
    // @Regex ^on player swaps items$
    //
    // @Group Player
    //
    // @Switch main:<item> to only process the event if the item being put into the main hand matches the input item.
    // @Switch offhand:<item> to only process the event if the item being put into the off-hand matches the input item.
    //
    // @Cancellable true
    //
    // @Triggers when a player swaps the items in their main and off hands.
    //
    // @Context
    // <context.main> returns the ItemTag switched to the main hand.
    // <context.offhand> returns the ItemTag switched to the off hand.
    //
    // @Determine
    // "MAIN:<ItemTag>" to set the item in the main hand.
    // "OFFHAND:<ItemTag>" to set the item in the off hand.
    //
    // @Player Always.
    //
    // -->

    public PlayerSwapsItemsScriptEvent() {
    }

    public PlayerTag player;
    public PlayerSwapHandItemsEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player swaps items");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("main", new ItemTag(event.getMainHandItem()))) {
            return false;
        }
        if (!path.tryObjectSwitch("offhand", new ItemTag(event.getOffHandItem()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("main:")) {
                event.setMainHandItem(ItemTag.valueOf(determination.substring("main:".length()), path.container).getItemStack());
                return true;
            }
            else if (lower.startsWith("offhand:")) {
                event.setOffHandItem(ItemTag.valueOf(determination.substring("offhand:".length()), path.container).getItemStack());
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("main")) {
            return new ItemTag(event.getMainHandItem());
        }
        else if (name.equals("offhand")) {
            return new ItemTag(event.getOffHandItem());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerSwapsItems(PlayerSwapHandItemsEvent event) {
        player = PlayerTag.mirrorBukkitPlayer(event.getPlayer());
        this.event = event;
        fire(event);
    }
}

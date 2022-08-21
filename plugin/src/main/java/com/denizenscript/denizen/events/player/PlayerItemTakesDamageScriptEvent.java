package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class PlayerItemTakesDamageScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player item takes damage
    // player <item> takes damage
    //
    // @Regex ^on player [^\s]+ takes damage$
    //
    // @Synonyms item durability changes
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when the player damages an item.
    //
    // @Context
    // <context.damage> returns the amount of damage the item has taken.
    // <context.item> returns the item that has taken damage.
    // <context.slot> returns the slot of the item that has taken damage. This value is a bit of a hack and is not reliable.
    //
    // @Determine
    // ElementTag(Number) to set the amount of damage the item will take.
    //
    // @Player Always.
    //
    // -->

    PlayerItemDamageEvent event;
    ItemTag item;
    LocationTag location;

    public PlayerItemTakesDamageScriptEvent() {
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(0).equals("player")) {
            return false;
        }
        if (!path.eventArgsLowEqualStartingAt(2, "takes", "damage")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(1))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!item.tryAdvancedMatcher(path.eventArgLowerAt(1))) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            event.setDamage(((ElementTag) determinationObj).asInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public BukkitScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item":
                return item;
            case "damage":
                return new ElementTag(event.getDamage());
            case "slot":
                return new ElementTag(SlotHelper.slotForItem(event.getPlayer().getInventory(), item.getItemStack()) + 1);
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            final Player p = event.getPlayer();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), p::updateInventory, 1);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onPlayerItemTakesDamage(PlayerItemDamageEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getItem());
        location = new LocationTag(event.getPlayer().getLocation());
        this.event = event;
        fire(event);
    }
}

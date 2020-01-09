package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
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
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when the player damages an item.
    //
    // @Context
    // <context.damage> returns the amount of damage the item has taken.
    // <context.item> returns the item that has taken damage.
    // <context.slot> returns the slot of the item that has taken damage.
    //
    // @Determine
    // ElementTag(Number) to set the amount of damage the item will take.
    //
    // @Player Always.
    //
    // -->

    public static PlayerItemTakesDamageScriptEvent instance;
    PlayerItemDamageEvent event;
    ItemTag item;
    LocationTag location;

    public PlayerItemTakesDamageScriptEvent() {
        instance = this;
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        return (path.eventLower.startsWith("players") || path.eventLower.startsWith("player")) &&
                path.eventArgLowerAt(2).equals("takes") &&
                path.eventArgLowerAt(3).equals("damage");
    }

    @Override
    public boolean matches(ScriptPath path) {

        String iItem = path.eventArgLowerAt(1);
        if (!tryItem(item, iItem)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerItemTakesDamage";
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
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("damage")) {
            return new ElementTag(event.getDamage());
        }
        else if (name.equals("slot")) {
            return new ElementTag(SlotHelper.slotForItem(event.getPlayer().getInventory(), item.getItemStack()));
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerItemTakesDamage(PlayerItemDamageEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getItem());
        location = new LocationTag(event.getPlayer().getLocation());
        boolean wasCancelled = event.isCancelled();
        this.event = event;
        fire(event);
        final Player p = event.getPlayer();
        if (cancelled && !wasCancelled) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                @Override
                public void run() {
                    p.updateInventory();
                }
            }, 1);
        }
    }
}

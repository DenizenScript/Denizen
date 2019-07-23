package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

// <--[event]
// @Events
// player item takes damage
// player <item> takes damage
//
// @Regex ^on player [^\s]+ takes damage$
// @Switch in <area>
//
// @Cancellable true
//
// @Triggers when the player damages an item.
//
// @Context
// <context.damage> returns the amount of damage the item has taken.
// <context.item> returns the item that has taken damage.
//
// @Determine
// Element(Number) to set the amount of damage the item will take.
//
// -->

public class PlayerItemTakesDamageScriptEvent extends BukkitScriptEvent implements Listener {

    PlayerItemTakesDamageScriptEvent instance;
    PlayerItemDamageEvent event;
    ElementTag damage;
    ItemTag item;
    LocationTag location;


    public PlayerItemTakesDamageScriptEvent() {
        instance = this;
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return (lower.startsWith("players") || lower.startsWith("player")) &&
                CoreUtilities.getXthArg(2, lower).equals("takes") &&
                CoreUtilities.getXthArg(3, lower).equals("damage");
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
        return true;
    }

    @Override
    public String getName() {
        return "PlayerItemTakesDamage";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ArgumentHelper.matchesInteger(determination)) {
            damage = new ElementTag(determination);
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
            return damage;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerItemTakesDamage(PlayerItemDamageEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getItem());
        damage = new ElementTag(event.getDamage());
        location = new LocationTag(event.getPlayer().getLocation());
        boolean wasCancelled = event.isCancelled();
        this.event = event;
        fire(event);
        event.setDamage(damage.asInt());
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

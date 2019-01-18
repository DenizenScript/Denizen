package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

// <--[event]
// @Events
// player item takes damage (in <area>)
// player <item> takes damage (in <area>)
//
// @Regex ^on player [^\s]+ takes damage( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    Element damage;
    dItem item;
    dLocation location;


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
    public boolean matches(ScriptContainer scriptContainer, ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        String iItem = CoreUtilities.getXthArg(1, lower);
        if (!tryItem(item, iItem)) {
            return false;
        }
        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerItemTakesDamage";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerItemDamageEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            damage = new Element(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public BukkitScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
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
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        item = new dItem(event.getItem());
        damage = new Element(event.getDamage());
        location = new dLocation(event.getPlayer().getLocation());
        cancelled = event.isCancelled();
        boolean wasCancelled = cancelled;
        this.event = event;
        fire();
        event.setCancelled(cancelled);
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

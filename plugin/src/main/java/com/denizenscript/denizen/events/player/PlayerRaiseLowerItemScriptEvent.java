package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerRaiseLowerItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player raises|lowers|toggles <item>
    //
    // @Synonyms player raises shield, player raises spyglass
    //
    // @Group Player
    //
    // @Location true
    // @Switch reason:<reason> to only process the event if the reason matches the input.
    //
    // @Triggers when a player starts or stops holding up an item, such as a shield, spyglass, or crossbow.
    //
    // @Warning For 'lowers', the item may be tracked incorrectly. Prefer 'player lowers item' (the generic item form) for a 'lowers' event (similar for 'toggles').
    // Also be aware this event may misfire in some cases.
    // This event and its data are more accurate on Paper servers.
    //
    // @Context
    // <context.state> returns an ElementTag(Boolean) of whether the player raised or lowered the item.
    // <context.held_for> returns a DurationTag of how long the player held the item up for (only on Paper).
    // <context.hand> returns an ElementTag of the hand that the player is raising or lowering (only on Paper).
    // <context.item> returns an ItemTag of the item that the player is raising or lowering (only on Paper).
    // <context.reason> returns the reason for a state change. Can be: raise, lower, swap, hold, drop, quit, death.
    //
    // @Player Always.
    //
    // -->

    public static final EnumSet<Material> raisableItems = EnumSet.of(Material.SHIELD, Material.CROSSBOW, Material.BOW, Material.TRIDENT, Material.SPYGLASS);

    public PlayerRaiseLowerItemScriptEvent() {
        registerCouldMatcher("player raises|lowers|toggles <item>");
        registerSwitches("reason");
        instance = this;
    }

    public static PlayerRaiseLowerItemScriptEvent instance;
    public PlayerTag player;
    public boolean state;
    public ItemTag item;
    public ElementTag reason;

    @Override
    public boolean matches(ScriptPath path) {
        String action = path.eventArgLowerAt(1);
        if (action.equals("raises") && !state) {
            return false;
        }
        if (action.equals("lowers") && state) {
            return false;
        }
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("reason", reason)) {
            return false;
        }
        if (!path.tryArgObject(2, item)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "state" -> new ElementTag(state);
            case "reason" -> reason;
            default -> super.getContext(name);
        };
    }

    public static Set<UUID> raisedItems = new HashSet<>();

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        super.init();
    }

    public void run(Player pl, String reason) {
        player = new PlayerTag(pl);
        this.reason = new ElementTag(reason);
        if (raisableItems.contains(player.getHeldItem().getBukkitMaterial()) || !raisableItems.contains(player.getOffhandItem().getBukkitMaterial())) {
            item = player.getHeldItem();
        }
        else {
            item = player.getOffhandItem();
        }
        fire();
    }

    public static void signalDidRaise(Player player) {
        if (!raisedItems.add(player.getUniqueId())) {
            return;
        }
        instance.state = true;
        instance.run(player, "raise");
    }

    public static void signalDidLower(Player player, String reason) {
        if (!raisedItems.remove(player.getUniqueId())) {
            return;
        }
        instance.state = false;
        instance.run(player, reason);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        signalDidLower(event.getPlayer(), "quit");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        signalDidLower(event.getEntity(), "death");
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        signalDidLower(event.getPlayer(), "swap");
    }

    public static class PlayerRaiseLowerItemScriptEventSpigotImpl extends PlayerRaiseLowerItemScriptEvent {

        @EventHandler
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            signalDidLower(event.getPlayer(), "drop");
        }

        @EventHandler
        public void onPlayerChangeHeldItem(PlayerItemHeldEvent event) {
            signalDidLower(event.getPlayer(), "hold");
        }
    }
}

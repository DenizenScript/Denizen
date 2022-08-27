package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.UUID;

public class PlayerHoldsItemEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player raises|lowers|toggles <item>
    //
    // @Synonyms player raises shield, player raises spyglass
    //
    // @Group Player
    //
    // @Location true
    //
    // @Triggers when a player starts or stops holding up an item, such as a shield, spyglass, or crossbow.
    //
    // @Warning For 'lowers', the item may be tracked incorrectly. Prefer 'player lowers item' (the generic item form) for a 'lowers' event (similar for 'toggles').
    // Also be aware this event may misfire in some cases.
    //
    // @Context
    // <context.state> returns an ElementTag(Boolean) with a value of "true" if the player is now holding up a raisable item and "false" otherwise.
    //
    // @Player Always.
    //
    // -->

    public PlayerHoldsItemEvent() {
        registerCouldMatcher("player raises|lowers|toggles <item>");
        instance = this;
    }

    public static PlayerHoldsItemEvent instance;
    public PlayerTag player;
    public boolean state;
    public ItemTag raised;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(0).equals("player")) {
            return false;
        }
        String middleWord = path.eventArgAt(1);
        if (!(middleWord.equals("raises") || middleWord.equals("lowers") || middleWord.equals("toggles"))) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (cmd.equals("raises") && !state) {
            return false;
        }
        if (cmd.equals("lowers") && state) {
            return false;
        }
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }
        if (!raised.tryAdvancedMatcher(path.eventArgLowerAt(2))) {
            return false;
        }
        return super.matches(path);
    }

    public boolean enabled = false;

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("state")) {
            return new ElementTag(state);
        }
        return super.getContext(name);
    }

    public static HashSet<UUID> raisedShields = new HashSet<>();

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    public void run(Player pl) {
        cancelled = false;
        player = new PlayerTag(pl);
        if (DenizenPacketHandler.raisableItems.contains(player.getHeldItem().getBukkitMaterial())
            || !DenizenPacketHandler.raisableItems.contains(player.getOffhandItem().getBukkitMaterial())) {
            raised = player.getHeldItem();
        }
        else {
            raised = player.getOffhandItem();
        }
        fire();
    }

    public static void signalDidRaise(Player player) {
        if (raisedShields.contains(player.getUniqueId())) {
            return;
        }
        raisedShields.add(player.getUniqueId());
        instance.state = true;
        instance.run(player);
    }

    public static void signalDidLower(Player player) {
        if (!raisedShields.remove(player.getUniqueId())) {
            return;
        }
        instance.state = false;
        instance.run(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        signalDidLower(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        signalDidLower(event.getEntity());
    }

    @EventHandler
    public void onPlayerChangeItem(PlayerItemHeldEvent event) {
        signalDidLower(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        signalDidLower(event.getPlayer());
    }
}

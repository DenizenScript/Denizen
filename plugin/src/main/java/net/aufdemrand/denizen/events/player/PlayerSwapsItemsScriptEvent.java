package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
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
    // @Cancellable true
    //
    // @Triggers when a player swaps the items in their main and off hands.
    //
    // @Context
    // <context.main> returns the dItem switched to the main hand.
    // <context.offhand> returns the dItem switched to the off hand.
    //
    // @Determine
    // "MAIN:" + dItem to set the item in the main hand.
    // "OFFHAND:" + dItem to set the item in the off hand.
    //
    // -->

    public PlayerSwapsItemsScriptEvent() {
        instance = this;
    }

    public static PlayerSwapsItemsScriptEvent instance;
    public dPlayer player;
    public dItem mainhand;
    public dItem offhand;
    public PlayerSwapHandItemsEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player swaps items");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerSwapsItems";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerSwapHandItemsEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("main:")) {
            event.setMainHandItem(dItem.valueOf(determination.substring("main:".length())).getItemStack());
            return true;
        }
        else if (lower.startsWith("offhand:")) {
            event.setOffHandItem(dItem.valueOf(determination.substring("offhand:".length())).getItemStack());
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("main")) {
            return mainhand;
        }
        else if (name.equals("offhand")) {
            return offhand;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerSwapsItems(PlayerSwapHandItemsEvent event) {
        player = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        mainhand = new dItem(event.getMainHandItem());
        offhand = new dItem(event.getOffHandItem());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

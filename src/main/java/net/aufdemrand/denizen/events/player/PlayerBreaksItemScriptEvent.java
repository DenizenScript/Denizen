package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class PlayerBreaksItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player breaks item (in <area>)
    // player breaks <item> (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when a player breaks the item they are holding.
    //
    // @Context
    // <context.item> returns the item that broke.
    //
    // -->

    public PlayerBreaksItemScriptEvent() {
        instance = this;
    }

    public static PlayerBreaksItemScriptEvent instance;
    public dItem item;
    public PlayerItemBreakEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player breaks");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String iCheck = CoreUtilities.getXthArg(2, lower);
        if (!tryItem(item, iCheck)) {
            return false;
        }
        return runInCheck(scriptContainer, s, lower, event.getPlayer().getLocation());
    }

    @Override
    public String getName() {
        return "PlayerItemBreak";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerItemBreakEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (CoreUtilities.toLowerCase(determination).startsWith("cancelled")) {
            cancelled = true;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("item", item);
        return context;
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        item = new dItem((ItemStack) event.getBrokenItem());
        this.event = event;
        fire();
        if (cancelled) {
            final Player player = event.getPlayer();
            final ItemStack itemstack = event.getBrokenItem();
            itemstack.setAmount(itemstack.getAmount() + 1);
            new BukkitRunnable() {
                @Override
                public void run() {
                    itemstack.setDurability(itemstack.getType().getMaxDurability());
                    player.updateInventory();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
        }
    }
}

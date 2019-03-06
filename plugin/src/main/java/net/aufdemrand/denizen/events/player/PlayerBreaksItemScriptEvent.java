package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
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

public class PlayerBreaksItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player breaks item (in <area>)
    // player breaks <item> (in <area>)
    //
    // @Regex ^on player breaks [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String iCheck = CoreUtilities.getXthArg(2, lower);
        if (!tryItem(item, iCheck)) {
            return false;
        }
        return runInCheck(path, event.getPlayer().getLocation());
    }

    @Override
    public String getName() {
        return "PlayerItemBreak";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        item = new dItem(event.getBrokenItem());
        this.event = event;
        cancelled = false;
        fire();
        if (cancelled) { // Hacked-in cancellation helper
            final Player player = event.getPlayer();
            final ItemStack itemstack = event.getBrokenItem();
            itemstack.setAmount(itemstack.getAmount() + 1);
            new BukkitRunnable() {
                public void run() {
                    itemstack.setDurability(itemstack.getType().getMaxDurability());
                    player.updateInventory();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
        }
    }
}

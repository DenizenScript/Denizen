package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerBreaksItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player breaks held item
    // player breaks held <item>
    //
    // @Regex ^on player breaks held [^\s]+$
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player breaks the item they are holding.
    //
    // @Context
    // <context.item> returns the item that broke.
    // <context.slot> returns the slot of the item that broke.
    //
    // @Player Always.
    //
    // -->

    public PlayerBreaksItemScriptEvent() {
        instance = this;
    }

    public static PlayerBreaksItemScriptEvent instance;
    public ItemTag item;
    public PlayerItemBreakEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("block")) {
            return false;
        }
        // TODO: *require* "held"
        return path.eventLower.startsWith("player breaks");
    }

    @Override
    public boolean matches(ScriptPath path) {
        boolean isModern = path.eventArgLowerAt(2).equals("held");
        String iCheck = path.eventArgLowerAt(isModern ? 3 : 2);
        if (!tryItem(item, iCheck)) {
            return false;
        }
        if (!isModern && item.getMaterial().getMaterial().isBlock()) { // Prevent "breaks block" collision with old style event.
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        if (!isModern) {
            Deprecations.oldStylePlayerBreaksItemEvent.message = oldWarningMessage + " (for event: " + path.toString() + ").";
            Deprecations.oldStylePlayerBreaksItemEvent.warn();
        }
        return true;
    }

    public static String oldWarningMessage = Deprecations.oldStylePlayerBreaksItemEvent.message;


    @Override
    public String getName() {
        return "PlayerItemBreak";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("slot")) {
            return new ElementTag(SlotHelper.slotForItem(event.getPlayer().getInventory(), item.getItemStack()));
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getBrokenItem());
        this.event = event;
        cancelled = false;
        fire(event);
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

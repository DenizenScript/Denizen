package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerRaiseLowerItemScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerRaiseLowerItemScriptEventPaperImpl extends PlayerRaiseLowerItemScriptEvent {

    public DurationTag heldFor;
    public ElementTag hand;

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "held_for" -> heldFor;
            case "hand" -> hand;
            case "item" -> item;
            default -> super.getContext(name);
        };
    }

    @Override
    public void run(Player pl) {
        cancelled = false;
        player = new PlayerTag(pl);
        item = new ItemTag(pl.getActiveItem());
        heldFor = state ? null : new DurationTag((long) pl.getHandRaisedTime());
        hand = new ElementTag(pl.getHandRaised());
        fire();
    }

    @EventHandler
    public void onStopUsingItem(PlayerStopUsingItemEvent event) {
        signalDidLower(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // You can only drop items from your main hand, so if the player's main hand isn't raised, ignore
        if (event.getPlayer().isHandRaised() && event.getPlayer().getHandRaised() == EquipmentSlot.HAND && raisedItems.remove(event.getPlayer().getUniqueId())) {
            cancelled = false;
            state = false;
            Player pl = event.getPlayer();
            player = new PlayerTag(pl);
            // Work around Player#getActiveItem being air in the drop item event
            ItemStack loweredItem = event.getItemDrop().getItemStack();
            item = new ItemTag(loweredItem);
            heldFor = new DurationTag((long) loweredItem.getMaxItemUseDuration() - pl.getItemUseRemainingTime());
            hand = new ElementTag(pl.getHandRaised());
            fire();
        }
    }

    @EventHandler
    public void onPlayerChangeHeldItem(PlayerItemHeldEvent event) {
        if (event.getPlayer().isHandRaised() && event.getPlayer().getHandRaised() == EquipmentSlot.HAND) {
            signalDidLower(event.getPlayer());
        }
    }
}

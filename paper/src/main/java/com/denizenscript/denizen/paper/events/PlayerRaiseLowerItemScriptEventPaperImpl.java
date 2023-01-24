package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.player.PlayerRaiseLowerItemScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import org.bukkit.Material;
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
    public void run(Player pl, String reason) {
        this.reason = new ElementTag(reason);
        player = new PlayerTag(pl);
        item = new ItemTag(pl.getActiveItem());
        hand = new ElementTag(pl.getHandRaised());
        if (item.getBukkitMaterial() == Material.AIR) {
            item = new ItemTag(pl.getEquipment().getItemInMainHand());
            hand = new ElementTag("HAND");
        }
        if (item.getBukkitMaterial() == Material.AIR) {
            item = new ItemTag(pl.getEquipment().getItemInOffHand());
            hand = new ElementTag("OFF_HAND");
        }
        if (item.getBukkitMaterial() == Material.AIR) {
            return;
        }
        heldFor = state ? null : new DurationTag((long) pl.getHandRaisedTime());
        fire();
    }

    @EventHandler
    public void onStopUsingItem(PlayerStopUsingItemEvent event) {
        signalDidLower(event.getPlayer(), "lower");
    }

    public boolean isHandRaised(Player player, EquipmentSlot slot) {
        if (player.isHandRaised()) {
            return slot == player.getHandRaised();
        }
        return raisedItems.contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // You can only drop items from your main hand, so if the player's main hand isn't raised, ignore
        if (isHandRaised(event.getPlayer(), EquipmentSlot.HAND) && raisedItems.remove(event.getPlayer().getUniqueId())) {
            cancelled = false;
            state = false;
            Player pl = event.getPlayer();
            player = new PlayerTag(pl);
            // Work around Player#getActiveItem being air in the drop item event
            ItemStack loweredItem = event.getItemDrop().getItemStack();
            item = new ItemTag(loweredItem);
            heldFor = new DurationTag((long) loweredItem.getMaxItemUseDuration() - pl.getItemUseRemainingTime());
            hand = new ElementTag(pl.getHandRaised());
            reason = new ElementTag("drop");
            fire();
        }
    }

    @EventHandler
    public void onPlayerChangeHeldItem(PlayerItemHeldEvent event) {
        if (isHandRaised(event.getPlayer(), EquipmentSlot.HAND)) {
            signalDidLower(event.getPlayer(), "hold");
        }
    }
}

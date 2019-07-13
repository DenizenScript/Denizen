package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class PlayerEquipsArmorScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player (un)equips armor
    // player (un)equips <item>
    // player (un)equips [helmet/chestplate/leggings/boots]
    //
    // @Regex ^on player (un)?equips [^\s]+$
    //
    // @Warning This event is unstable and not guaranteed to function correctly at all.
    //
    // @Triggers when a player (un)equips armor.
    //
    // @Context
    // <context.armor> returns the dItem that was (un)equipped.
    // <context.reason> returns the reason that the armor was (un)equipped. Can be "INVENTORY", "INTERACT", "DISPENSER", or "BREAK".
    // -->

    public PlayerEquipsArmorScriptEvent() {
        instance = this;
    }

    public static PlayerEquipsArmorScriptEvent instance;
    public ElementTag equipType;
    public ElementTag armorType;
    public ElementTag reason;
    public dItem armor;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player equips ") || lower.startsWith("player unequips ");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String type = path.eventArgLowerAt(1);

        if (!type.equals(CoreUtilities.toLowerCase(equipType.asString()))) {
            return false;
        }

        String eItem = path.eventArgLowerAt(2);

        if (!eItem.equals("armor") && !eItem.equals(CoreUtilities.toLowerCase(armorType.asString()))
                && !tryItem(armor, eItem)) {
            return false;
        }

        return true;
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getName() {
        return "PlayerEquipsArmor";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("armor")) {
            return armor;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        return super.getContext(name);
    }

    private void handleChangedArmor(final Player player, final String reason) {
        EntityEquipment oldEquipment = player.getEquipment();
        final ItemStack oldHelmet = oldEquipment.getHelmet();
        final ItemStack oldChestplate = oldEquipment.getChestplate();
        final ItemStack oldLeggings = oldEquipment.getLeggings();
        final ItemStack oldBoots = oldEquipment.getBoots();
        Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                EntityEquipment newEquipment = player.getEquipment();
                handleEvent(player, "helmet", oldHelmet, newEquipment.getHelmet(), reason);
                handleEvent(player, "chestplate", oldChestplate, newEquipment.getChestplate(), reason);
                handleEvent(player, "leggings", oldLeggings, newEquipment.getLeggings(), reason);
                handleEvent(player, "boots", oldBoots, newEquipment.getBoots(), reason);
            }
        }, 1);
    }

    private void handleEvent(Player player, String type, ItemStack oldItem, ItemStack newItem, String reason) {
        if (!isSameItem(oldItem, newItem)) {
            if (isAir(oldItem)) {
                // Equips
                fireEquipsEvent(player, type, newItem, reason);
            }
            else if (isAir(newItem)) {
                // Unequips
                fireUnequipsEvent(player, type, oldItem, reason);
            }
            else {
                // Both
                fireUnequipsEvent(player, type, oldItem, reason);
                fireEquipsEvent(player, type, newItem, reason);
            }
        }
    }

    private void fireEquipsEvent(Player bukkitPlayer, String type, ItemStack newItem, String reasonString) {
        equipType = new ElementTag("equips");
        armorType = new ElementTag(type);
        reason = new ElementTag(reasonString);
        armor = new dItem(newItem);
        player = dPlayer.mirrorBukkitPlayer(bukkitPlayer);
        cancelled = false;
        fire();
    }

    private void fireUnequipsEvent(Player bukkitPlayer, String type, ItemStack oldItem, String reasonString) {
        equipType = new ElementTag("unequips");
        armorType = new ElementTag(type);
        reason = new ElementTag(reasonString);
        armor = new dItem(oldItem);
        player = dPlayer.mirrorBukkitPlayer(bukkitPlayer);
        cancelled = false;
        fire();
    }

    private boolean isSameItem(ItemStack oldItem, ItemStack newItem) {
        if (isAir(oldItem) || isAir(newItem)) {
            return isAir(oldItem) && isAir(newItem);
        }
        oldItem = oldItem.clone();
        oldItem.setDurability((short) 0);
        newItem = newItem.clone();
        newItem.setDurability((short) 0);
        return oldItem.equals(newItem);
    }

    private boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    @EventHandler
    public void onBlockDispense(final BlockDispenseEvent event) {
        final Location location = event.getBlock().getLocation();
        for (Player player : location.getWorld().getPlayers()) {
            if (!dEntity.isNPC(player) && Utilities.checkLocation(player, location, 2.5)) {
                handleChangedArmor(player, "DISPENSER");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            handleChangedArmor((Player) humanEntity, "INVENTORY");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (humanEntity instanceof Player) {
            handleChangedArmor((Player) humanEntity, "INVENTORY");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        handleChangedArmor(event.getPlayer(), "INTERACT");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            handleChangedArmor((Player) entity, "BREAK");
        }
    }
}

package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
    public Element equipType;
    public Element armorType;
    public Element reason;
    public dItem armor;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player equips ") || lower.startsWith("player unequips ");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String type = CoreUtilities.getXthArg(1, lower);

        if (!type.equals(CoreUtilities.toLowerCase(equipType.asString()))) {
            return false;
        }

        String eItem = CoreUtilities.getXthArg(2, lower);

        if (!eItem.equals("armor") && !eItem.equals(CoreUtilities.toLowerCase(armorType.asString()))
                && !tryItem(armor, eItem)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerEquipsArmor";
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
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
        equipType = new Element("equips");
        armorType = new Element(type);
        reason = new Element(reasonString);
        armor = new dItem(newItem);
        player = dPlayer.mirrorBukkitPlayer(bukkitPlayer);
        fire();
    }

    private void fireUnequipsEvent(Player bukkitPlayer, String type, ItemStack oldItem, String reasonString) {
        equipType = new Element("unequips");
        armorType = new Element(type);
        reason = new Element(reasonString);
        armor = new dItem(oldItem);
        player = dPlayer.mirrorBukkitPlayer(bukkitPlayer);
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

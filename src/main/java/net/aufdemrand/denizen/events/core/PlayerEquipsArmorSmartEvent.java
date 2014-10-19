package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerEquipsArmorSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on player (un)?equips ((m@|i@)?\\w+)", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                String string = m.group(2);
                if (string.equalsIgnoreCase("armor")
                        || string.equalsIgnoreCase("helmet")
                        || string.equalsIgnoreCase("chestplate")
                        || string.equalsIgnoreCase("leggings")
                        || string.equalsIgnoreCase("boots"))
                    return true;
                else if (dMaterial.matches(string)) {
                    dMaterial material = dMaterial.valueOf(string);
                    if (material != null)
                        return isArmor(material.getMaterial());
                }
                else if (dItem.matches(string)) {
                    dItem item = dItem.valueOf(string);
                    if (item != null)
                        return isArmor(item.getItemStack());
                }
            }
        }
        // No matches at all, just fail.
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Player Equips Armor SmartEvent.");
    }


    @Override
    public void breakDown() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        BlockDispenseEvent.getHandlerList().unregister(this);
    }


    //////////////
    //  MECHANICS
    ///////////

    @EventHandler
    public void blockDispense(BlockDispenseEvent event) {
        final ItemStack item = event.getItem();
        final Location location = event.getBlock().getLocation();

        if (isArmor(item)) {
            for (final Player player : location.getWorld().getPlayers()) {
                if (Utilities.checkLocation(player, location, 2.5)) {
                    final ItemStack[] armor_contents = player.getInventory().getArmorContents();
                    final Vector velocity = event.getVelocity();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ItemStack[] new_armor = player.getInventory().getArmorContents();
                            for (int i = 0; i < new_armor.length; i++) {
                                ItemStack before = armor_contents[i];
                                ItemStack now = new_armor[i];
                                if (now != null && now.getType() == item.getType()
                                        && now.getDurability() == item.getDurability()
                                        && (before == null || before.getType() == Material.AIR)) {
                                    if (playerEquipsArmorEvent(player, item)) {
                                        player.getInventory().setContents(armor_contents);
                                        location.getWorld().dropItemNaturally(location, item).setVelocity(velocity);
                                    }
                                }
                            }
                        }
                    }.runTaskLater(DenizenAPI.getCurrentInstance(), 0); // Yes, 0 is correct.
                }
            }
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!didPlayerClickOwnInventory((Player) event.getWhoClicked(), inventory))
            return;
        ItemStack item = event.getCurrentItem();
        Player player = (Player) inventory.getHolder();
        ItemStack cursor = event.getCursor();
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (item != null && item.getType() != Material.AIR
                    && (cursor == null || cursor.getType() == Material.AIR || isArmor(cursor))) {
                if (playerUnequipsArmorEvent(player, item)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (cursor != null && cursor.getType() != Material.AIR && isArmor(cursor)) {
                if (playerEquipsArmorEvent(player, cursor)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        else if (event.getClick().isShiftClick() && item != null && isArmor(item)) {
            ItemStack currentItem = player.getInventory().getArmorContents()[getArmorTypeNumber(item)];
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                if (playerEquipsArmorEvent(player, item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void inventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (!didPlayerClickOwnInventory((Player) event.getWhoClicked(), inventory))
            return;
        ItemStack item = event.getOldCursor();
        Player player = (Player) inventory.getHolder();
        if (!isArmor(item)) return;
        int[] armor_slots = new int[]{5,6,7,8};
        Set<Integer> slots = event.getRawSlots();
        for (int slot : armor_slots) {
            if (slots.contains(slot) && (slot-5 == getArmorTypeNumber(item))) {
                ItemStack before = inventory.getItem(slot);
                if (before == null || before.getType() == Material.AIR) {
                    if (playerEquipsArmorEvent(player, item)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.hasItem()) {
            ItemStack item = event.getItem();
            Action action = event.getAction();
            if ((action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
                    || !isArmor(item) || isInteractive(event.getClickedBlock().getType()))
                return;
            ItemStack currentItem = event.getPlayer().getInventory().getArmorContents()[getArmorTypeNumber(item)];
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                if (playerEquipsArmorEvent(event.getPlayer(), item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean isArmor(ItemStack itemStack) {
        int id = itemStack.getTypeId();
        return id >= 298 && id <= 317;
    }

    private boolean isArmor(Material material) {
        int id = material.getId();
        return id >= 298 && id <= 317;
    }

    private boolean didPlayerClickOwnInventory(Player player, Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        InventoryType type = inventory.getType();
        return holder != null
                && holder.equals(player)
                && type != InventoryType.ENDER_CHEST
                && type != InventoryType.WORKBENCH;
    }

    private int getArmorTypeNumber(ItemStack itemStack) {
        return (itemStack.getTypeId()-298)%4;
    }
    
    private String getArmorType(ItemStack itemStack) {
        if (!isArmor(itemStack))
            return "helmet";
        switch (getArmorTypeNumber(itemStack)) {
            case 0:
                return "helmet";
            case 1:
                return "chestplate";
            case 2:
                return "leggings";
            case 3:
                return "boots";
        }
        return null;
    }

    private boolean isInteractive(Material material) {
        if (material == null || !material.isBlock()) {
            return false;
        }
        switch (material) {
            case DISPENSER:
            case NOTE_BLOCK:
            case BED_BLOCK:
            case CHEST:
            case WORKBENCH:
            case FURNACE:
            case BURNING_FURNACE:
            case WOODEN_DOOR:
            case LEVER:
            case REDSTONE_ORE:
            case STONE_BUTTON:
            case JUKEBOX:
            case CAKE_BLOCK:
            case DIODE_BLOCK_ON:
            case DIODE_BLOCK_OFF:
            case TRAP_DOOR:
            case FENCE_GATE:
            case ENCHANTMENT_TABLE:
            case BREWING_STAND:
            case DRAGON_EGG:
            case ENDER_CHEST:
            case COMMAND:
            case BEACON:
            case WOOD_BUTTON:
            case ANVIL:
            case TRAPPED_CHEST:
            case REDSTONE_COMPARATOR_ON:
            case REDSTONE_COMPARATOR_OFF:
            case HOPPER:
            case DROPPER:
                return true;
            default:
                return false;
        }
    }

    // <--[event]
    // @Events
    // player equips armor
    // player equips <item>
    // player equips [helmet/chestplate/leggings/boots]
    //
    // @Regex on player equips (m@|i@)?\w+
    //
    // @Triggers when a player equips armor.
    // @Context
    // <context.armor> returns the dItem that was equipped.
    //
    // @Determine
    // "CANCELLED" to stop the armor from being equipped.
    // -->
    private boolean playerEquipsArmorEvent(final Player player, final ItemStack item) {

        dItem armor = new dItem(item);

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("armor", armor);

        String determination = EventManager.doEvents(Arrays.asList
                        ("player equips armor",
                                "player equips " + getArmorType(item),
                                "player equips " + armor.identifySimple(),
                                "player equips " + armor.identifyMaterial()),
                null, new dPlayer(player), context).toUpperCase();

        return determination.startsWith("CANCELLED");

    }

    // <--[event]
    // @Events
    // player unequips armor
    // player unequips <item>
    // player unequips [helmet/chestplate/leggings/boots]
    //
    // @Regex on player unequips (m@|i@)?\w+
    //
    // @Triggers when a player unequips armor.
    // @Context
    // <context.armor> returns the dItem that was unequipped.
    //
    // @Determine
    // "CANCELLED" to stop the armor from being unequipped.
    // -->
    private boolean playerUnequipsArmorEvent(final Player player, final ItemStack item) {

        dItem armor = new dItem(item);

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("armor", armor);

        String determination = EventManager.doEvents(Arrays.asList
                        ("player unequips armor",
                                "player unequips " + getArmorType(item),
                                "player unequips " + armor.identifySimple(),
                                "player unequips " + armor.identifyMaterial()),
                null, new dPlayer(player), context).toUpperCase();

        return determination.startsWith("CANCELLED");
        
    }
}

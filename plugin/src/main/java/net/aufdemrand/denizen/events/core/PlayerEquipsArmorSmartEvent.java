package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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

public class PlayerEquipsArmorSmartEvent implements OldSmartEvent, Listener {


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
                        || string.equalsIgnoreCase("boots")) {
                    return true;
                }
                else if (dMaterial.matches(string)) {
                    dMaterial material = dMaterial.valueOf(string);
                    if (material != null) {
                        return isArmor(material.getMaterial());
                    }
                }
                else if (dItem.matches(string)) {
                    dItem item = dItem.valueOf(string);
                    if (item != null) {
                        return isArmor(item.getItemStack());
                    }
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
                if (!dEntity.isNPC(player) && Utilities.checkLocation(player, location, 2.5)) {
                    final ItemStack[] armor_contents = player.getInventory().getArmorContents().clone();
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
                                    if (playerEquipsArmorEvent(player, item, "DISPENSER")) {
                                        location.getWorld().dropItemNaturally(location, item).setVelocity(velocity);
                                        armor_contents[i] = null;
                                        player.getInventory().setArmorContents(armor_contents);
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
        dPlayer pl = dEntity.getPlayerFrom(event.getWhoClicked());
        if (pl == null) {
            return;
        }
        final Player player = pl.getPlayerEntity();
        Inventory inventory = event.getInventory();
        if (!didPlayerClickOwnInventory(player, inventory)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (item != null && item.getType() != Material.AIR
                    && (cursor == null || cursor.getType() == Material.AIR || isArmor(cursor))) {
                if (playerUnequipsArmorEvent(player, item, "INVENTORY")) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (cursor != null && cursor.getType() != Material.AIR && isArmor(cursor)) {
                if (playerEquipsArmorEvent(player, cursor, "INVENTORY")) {
                    event.setCancelled(true);
                }
            }
        }
        else if (event.getClick().isShiftClick() && item != null && isArmor(item)) {
            ItemStack currentItem = player.getInventory().getArmorContents()[3 - getArmorTypeNumber(item)];
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                if (playerEquipsArmorEvent(player, item, "INVENTORY")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void inventoryDrag(InventoryDragEvent event) {
        dPlayer pl = dEntity.getPlayerFrom(event.getWhoClicked());
        if (pl == null) {
            return;
        }
        final Player player = pl.getPlayerEntity();
        Inventory inventory = event.getInventory();
        if (!didPlayerClickOwnInventory(player, inventory)) {
            return;
        }
        ItemStack item = event.getOldCursor();
        if (!isArmor(item)) {
            return;
        }
        int[] armor_slots = new int[]{5, 6, 7, 8};
        Set<Integer> slots = event.getRawSlots();
        for (int slot : armor_slots) {
            if (slots.contains(slot) && (slot - 5 == getArmorTypeNumber(item))) {
                ItemStack before = inventory.getItem(slot);
                if (before == null || before.getType() == Material.AIR) {
                    if (playerEquipsArmorEvent(player, item, "INVENTORY")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        dPlayer pl = dEntity.getPlayerFrom(event.getPlayer());
        if (pl == null) {
            return;
        }
        final Player player = pl.getPlayerEntity();
        if (event.hasItem()) {
            ItemStack item = event.getItem();
            Action action = event.getAction();
            Block clicked = event.getClickedBlock();
            if ((action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
                    || !isArmor(item) || (clicked != null && isInteractive(clicked.getType()))) {
                return;
            }
            ItemStack currentItem = event.getPlayer().getInventory().getArmorContents()[3 - getArmorTypeNumber(item)];
            if (currentItem == null || currentItem.getType() == Material.AIR) {
                if (playerEquipsArmorEvent(player, item, "INTERACT")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        dPlayer pl = dEntity.getPlayerFrom(entity);
        if (pl == null) {
            return;
        }
        final Player player = pl.getPlayerEntity();
        final ItemStack[] oldArmor = player.getInventory().getArmorContents();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isValid() || player.isDead()) {
                    return;
                }
                ItemStack[] newArmor = player.getInventory().getArmorContents();
                for (int i = 0; i < 4; i++) {
                    ItemStack o = oldArmor[i] == null ? null: oldArmor[i].clone();
                    ItemStack n = newArmor[i] == null ? null: newArmor[i].clone();
                    if (o != null) {
                        o.setDurability((short) 0);
                        if (n != null) {
                            n.setDurability((short) 0);
                        }
                        if (n == null || !n.equals(o)) {
                            if (playerUnequipsArmorEvent(player, o, "BREAK")) {
                                newArmor[i] = o;
                            }
                        }
                    }
                    player.getInventory().setArmorContents(newArmor);
                }
            }
        }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
    }

    private boolean isArmor(ItemStack itemStack) {
        return isArmor(itemStack.getType());
    }

    private boolean isArmor(Material material) {
        int id = material.getId();
        return id >= 298 && id <= 317
                || material == Material.ELYTRA
                || material == Material.PUMPKIN
                || material == Material.JACK_O_LANTERN
                || material == Material.SKULL_ITEM;
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
        if (itemStack.getType() == Material.ELYTRA) {
            return 1;
        }
        return (itemStack.getTypeId() - 298) % 4;
    }

    private String getArmorType(ItemStack itemStack) {
        if (!isArmor(itemStack)) {
            return "helmet";
        }
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
    // player (un)equips armor
    // player (un)equips <item>
    // player (un)equips [helmet/chestplate/leggings/boots]
    //
    // @Regex ^on player (un)?equips [^\s]+$
    //
    // @Triggers when a player (un)equips armor.
    // @Context
    // <context.armor> returns the dItem that was (un)equipped.
    // <context.reason> returns the reason that the armor was (un)equipped. Can be "INVENTORY", "INTERACT", "DISPENSER", or "BREAK".
    //
    // @Determine
    // "CANCELLED" to stop the armor from being (un)equipped.
    // -->
    private boolean playerEquipsArmorEvent(final Player player, final ItemStack item, String reason) {

        dItem armor = new dItem(item);

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("armor", armor);
        context.put("reason", new Element(reason));

        String determination = BukkitWorldScriptHelper.doEvents(Arrays.asList
                        ("player equips armor",
                                "player equips " + getArmorType(item),
                                "player equips " + armor.identifySimple(),
                                "player equips " + armor.identifyMaterial()),
                null, dEntity.getPlayerFrom(player), context).toUpperCase();

        if (determination.startsWith("CANCELLED")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.updateInventory();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
            return true;
        }

        return false;

    }

    private boolean playerUnequipsArmorEvent(final Player player, final ItemStack item, String reason) {

        dItem armor = new dItem(item);

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("armor", armor);
        context.put("reason", new Element(reason));

        String determination = BukkitWorldScriptHelper.doEvents(Arrays.asList
                        ("player unequips armor",
                                "player unequips " + getArmorType(item),
                                "player unequips " + armor.identifySimple(),
                                "player unequips " + armor.identifyMaterial()),
                null, dEntity.getPlayerFrom(player), context, true).toUpperCase();

        if (determination.startsWith("CANCELLED")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.updateInventory();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
            return true;
        }

        return false;

    }
}

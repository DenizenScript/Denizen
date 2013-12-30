package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

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
            Matcher m = Pattern.compile("on player equips (m@|i@)?\\w+", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // TODO: Check if it's a valid armor material or item?
                return true;
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
        InventoryCloseEvent.getHandlerList().unregister(this);
        BlockDispenseEvent.getHandlerList().unregister(this);
    }


    //////////////
    //  MECHANICS
    ///////////

    @EventHandler
    public void blockDispense(BlockDispenseEvent event) {
        dItem item = new dItem(event.getItem());
        dLocation location = new dLocation(event.getBlock().getLocation());

        if (item.isArmor()) {
            for (Player player : location.getWorld().getPlayers())
                if (Utilities.checkLocation(player, location, 1.5))
                    playerEquipsArmorEvent(player, event.getItem(), player.getInventory().firstEmpty());
        }
    }

    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {

        Player player = (Player) event.getPlayer();
        String type = event.getInventory().getType().name();

        if (event.getInventory().getHolder() instanceof Player) {
            PlayerInventory inv = (PlayerInventory) event.getInventory().getHolder().getInventory();
            ItemStack[] armor_contents = inv.getArmorContents();
            for (int s = 0; s < 4; s++) {
                if (armor_contents[0].getType() != Material.AIR)
                    playerEquipsArmorEvent((Player) inv.getHolder(), armor_contents[s], inv.firstEmpty());
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.hasItem()) {
            dItem item = new dItem(event.getItem());
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && item.isArmor()) {
                playerEquipsArmorEvent(event.getPlayer(), event.getItem(), event.getPlayer().getInventory().getHeldItemSlot());
            }
        }
    }

    // TODO: Meta information!
    public void playerEquipsArmorEvent(final Player player, final ItemStack item, final int replaceSlot) {

        // Run this as a not-so-delayed Runnable...
        // This is to force Bukkit to see any newly equipped armor
        new BukkitRunnable() {
            @Override
            public void run() {
                dItem armor = new dItem(item);
                ItemStack[] armor_contents = player.getInventory().getArmorContents();

                int type = 3-((item.getTypeId()-298)%4);
                // TODO: Catch index error here
                if (armor.comparesTo(armor_contents[type]) == -1) {
                    for (ItemStack item : player.getInventory().getArmorContents()) {
                        // TODO: Something here?
                    }
                    return;
                }

                Map<String, dObject> context = new HashMap<String, dObject>();
                context.put("armor", armor);

                String determination = EventManager.doEvents(Arrays.asList
                        ("player equips armor",
                                "player equips " + armor.identify(),
                                "player equips " + armor.identifyMaterial()),
                        null, player, context).toUpperCase();

                if (determination.startsWith("CANCELLED")) {
                    armor_contents[type] = new ItemStack(Material.AIR);
                    player.getInventory().setArmorContents(armor_contents);
                    player.getInventory().setItem(replaceSlot, item);
                }
            }
        }.runTaskLater(DenizenAPI.getCurrentInstance(), 0);

    }


}

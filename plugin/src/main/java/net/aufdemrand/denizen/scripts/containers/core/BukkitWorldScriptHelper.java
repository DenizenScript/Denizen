package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitWorldScriptHelper implements Listener {

    public BukkitWorldScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public static String doEvents(List<String> events, dNPC npc, dPlayer player, Map<String, dObject> context) {
        return doEvents(events, npc, player, context, false);
    }

    public static String doEvents(List<String> events, dNPC npc, dPlayer player, Map<String, dObject> context, boolean useids) {
        List<String> determ;
        if (useids) {
            determ = OldEventManager.doEvents(events, new BukkitScriptEntryData(player, npc), context, true);
        }
        else {
            determ = OldEventManager.doEvents(events, new BukkitScriptEntryData(player, npc), context);
        }
        return determ.size() > 0 ? determ.get(0) : "none";
    }

    /////////////////////
    //   CUSTOM EVENTS
    /////////////////

    // <--[event]
    // @Events
    // server start
    //
    // @Regex ^on server start$
    //
    // @Triggers when the server starts
    //
    // -->
    public void serverStartEvent() {
        long ticks = Settings.worldScriptTimeEventFrequency().getTicks();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        timeEvent();
                    }
                }, ticks, ticks);

        // Fire the 'Server Start' event
        doEvents(Arrays.asList("server start"), null, null, null);
    }

    // <--[event]
    // @Events
    // server prestart
    //
    // @Regex ^on server prestart$
    //
    // @Triggers before the server finishes starting... fired after saves are loaded, but before notables are loaded.
    //
    // -->
    public void serverPreStartEvent() {
        // Fire the 'Server Start' event
        String determination = doEvents(Arrays.asList("server prestart"),
                null, null, null);
    }

    private final Map<String, Integer> current_time = new HashMap<String, Integer>();

    // <--[event]
    // @Events
    // time changes (in <world>)
    // time <0-23> in <world>
    //
    // @Regex ^on time [^\s]+( in [^\s]+)$
    //
    // @Triggers when the current time changes in a world (once per mine-hour).
    //
    // @Context
    // <context.time> returns the current time.
    // <context.world> returns the world.
    //
    // -->
    public void timeEvent() {
        for (World world : Bukkit.getWorlds()) {
            int hour = Double.valueOf(world.getTime() / 1000).intValue(); // TODO: What is this conversion math
            hour = hour + 6;
            // Get the hour
            if (hour >= 24) {
                hour = hour - 24;
            }

            dWorld currentWorld = new dWorld(world);

            if (!current_time.containsKey(currentWorld.identifySimple())
                    || current_time.get(currentWorld.identifySimple()) != hour) {
                Map<String, dObject> context = new HashMap<String, dObject>();

                context.put("time", new Element(hour));
                context.put("world", currentWorld);

                doEvents(Arrays.asList
                                ("time changes",
                                        "time changes in " + currentWorld.identifySimple(),
                                        String.valueOf(hour) + ":00 in " + currentWorld.identifySimple(), // NOTE: Deprecated
                                        "time " + String.valueOf(hour) + " in " + currentWorld.identifySimple()),
                        null, null, context, true);

                current_time.put(currentWorld.identifySimple(), hour);
            }
        }
    }

    /////////////////////
    //   INVENTORY EVENTS
    /////////////////

    // <--[language]
    // @Name Inventory Actions
    // @Group Useful Lists
    // @Description
    // Used by some inventory world events to describe the action of the inventory event.
    //
    // Actions, as described by the bukkit javadocs:
    // CLONE_STACK
    // A max-size stack of the clicked item is put on the cursor.
    // COLLECT_TO_CURSOR
    // The inventory is searched for the same material, and they are put on the cursor up to
    //      m@material.max_stack_size.
    // DROP_ALL_CURSOR
    // The entire cursor item is dropped.
    // DROP_ALL_SLOT
    // The entire clicked slot is dropped.
    // DROP_ONE_CURSOR
    // One item is dropped from the cursor.
    // DROP_ONE_SLOT
    // One item is dropped from the clicked slot.
    // HOTBAR_MOVE_AND_READD
    // The clicked item is moved to the hotbar, and the item currently there is re-added to the
    //      player's inventory.
    // HOTBAR_SWAP
    // The clicked slot and the picked hotbar slot are swapped.
    // MOVE_TO_OTHER_INVENTORY
    // The item is moved to the opposite inventory if a space is found.
    // NOTHING
    // Nothing will happen from the click.
    // PICKUP_ALL
    // All of the items on the clicked slot are moved to the cursor.
    // PICKUP_HALF
    // Half of the items on the clicked slot are moved to the cursor.
    // PICKUP_ONE
    // One of the items on the clicked slot are moved to the cursor.
    // PICKUP_SOME
    // Some of the items on the clicked slot are moved to the cursor.
    // PLACE_ALL
    // All of the items on the cursor are moved to the clicked slot.
    // PLACE_ONE
    // A single item from the cursor is moved to the clicked slot.
    // PLACE_SOME
    // Some of the items from the cursor are moved to the clicked slot (usually up to the max stack size).
    // SWAP_WITH_CURSOR
    // The clicked item and the cursor are exchanged.
    // UNKNOWN
    // An unrecognized ClickType.
    //
    // -->

    // <--[event]
    // @Events
    // player clicks in inventory
    // player (<click type>) clicks (<item>) (in <inventory>) (with <item>)
    // player (<click type>) clicks (<material>) (in <inventory>) (with <item>)
    // player (<click type>) clicks (<item>) (in <inventory>) (with <material>)
    // player (<click type>) clicks (<material>) (in <inventory>) (with <material>)
    //
    // @Regex ^on player( [^\s]+)? clicks [^\s]+( in [^\s]+)?( with [^\s]+)?$
    //
    // @Triggers when a player clicks in an inventory.
    // @Context
    // <context.item> returns the dItem the player has clicked on.
    // <context.inventory> returns the dInventory.
    // <context.cursor_item> returns the item the Player is clicking with.
    // <context.click> returns an Element with the name of the click type.
    // <context.slot_type> returns an Element with the name of the slot type that was clicked.
    // <context.slot> returns an Element with the number of the slot that was clicked.
    // <context.raw_slot> returns an Element with the raw number of the slot that was clicked.
    // <context.is_shift_click> returns true if 'shift' was used while clicking.
    // <context.action> returns the inventory_action. See <@link language Inventory Actions>.
    // <context.hotbar_button> returns an Element of the button pressed as a number, or 0 if no number button was pressed.
    //
    // @Determine
    // "CANCELLED" to stop the player from clicking.
    // dItem to set the current item for the event.
    //
    // -->
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {

        // TODO: make this a script event...

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(Material.AIR);
        dItem holding;

        dInventory inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        final dPlayer player = dEntity.getPlayerFrom(event.getWhoClicked());
        String type = event.getInventory().getType().name();
        String click = event.getClick().name();
        String slotType = event.getSlotType().name();

        List<String> events = new ArrayList<String>();
        events.add("player clicks in inventory");
        events.add("player clicks in " + type);
        events.add("player clicks in " + inventory.identifySimple());

        String interaction = "player " + click + " clicks ";

        events.add(interaction + "in inventory");
        events.add(interaction + "in " + type);
        events.add(interaction + "in " + inventory.identifySimple());

        boolean isNote = NotableManager.isSaved(inventory);

        if (isNote) {
            events.add("player clicks in notable");
            events.add(interaction + "in notable");
        }

        if (event.getCursor() != null) {
            holding = new dItem(event.getCursor());
            context.put("cursor_item", holding);

            events.add(interaction + "in inventory with " + holding.identifySimple());
            events.add(interaction + "in " + type + " with " + holding.identifySimple());
            events.add(interaction + "in " + inventory.identifySimple() + " with " + holding.identifySimple());
            events.add(interaction + "in inventory with " + holding.identifyMaterial());
            events.add(interaction + "in " + type + " with " + holding.identifyMaterial());
            events.add(interaction + "in " + inventory.identifySimple() + " with " + holding.identifyMaterial());
            events.add("player clicks in inventory with " + holding.identifySimple());
            events.add("player clicks in " + type + " with " + holding.identifySimple());
            events.add("player clicks in " + inventory.identifySimple() + " with " + holding.identifySimple());
            events.add("player clicks in inventory with " + holding.identifyMaterial());
            events.add("player clicks in " + type + " with " + holding.identifyMaterial());
            events.add("player clicks in " + inventory.identifySimple() + " with " + holding.identifyMaterial());
            if (isNote) {
                events.add(interaction + "in notable with " + holding.identifySimple());
                events.add(interaction + "in notable with " + holding.identifyMaterial());
                events.add("player clicks in notable with " + holding.identifySimple());
                events.add("player clicks in notable with " + holding.identifyMaterial());
            }
        }

        if (event.getCurrentItem() != null) {
            item = new dItem(event.getCurrentItem());

            events.add("player clicks " +
                    item.identifySimple() + " in inventory");
            events.add(interaction +
                    item.identifySimple() + " in inventory");
            events.add("player clicks " +
                    item.identifySimple() + " in " + type);
            events.add(interaction +
                    item.identifySimple() + " in " + type);
            events.add("player clicks " +
                    item.identifySimple() + " in " + inventory.identifySimple());
            events.add(interaction +
                    item.identifySimple() + " in " + inventory.identifySimple());
            events.add("player clicks " +
                    item.identifyMaterial() + " in inventory");
            events.add(interaction +
                    item.identifyMaterial() + " in inventory");
            events.add("player clicks " +
                    item.identifyMaterial() + " in " + type);
            events.add(interaction +
                    item.identifyMaterial() + " in " + type);
            events.add("player clicks " +
                    item.identifyMaterial() + " in " + inventory.identifySimple());
            events.add(interaction +
                    item.identifyMaterial() + " in " + inventory.identifySimple());
            if (isNote) {
                events.add("player clicks " +
                        item.identifySimple() + " in notable");
                events.add(interaction +
                        item.identifySimple() + " in notable");
                events.add("player clicks " +
                        item.identifyMaterial() + " in notable");
                events.add(interaction +
                        item.identifyMaterial() + " in notable");
            }

            if (event.getCursor() != null) {
                holding = new dItem(event.getCursor());

                final String[] itemStrings = new String[] {
                        item.identifySimple(),
                        item.identifyMaterial()
                };
                final String[] inventoryStrings = isNote ? new String[] {
                        "inventory",
                        "notable",
                        type,
                        inventory.identifySimple()
                } : new String[] {
                        "inventory",
                        type,
                        inventory.identifySimple()
                };
                final String[] holdingStrings = new String[] {
                        holding.identifySimple(),
                        holding.identifyMaterial()
                };

                for (String itemString : itemStrings) {
                    for (String inventoryString : inventoryStrings) {
                        for (String holdingString : holdingStrings) {
                            String fullString = itemString + " in " + inventoryString + " with " + holdingString;
                            events.add("player clicks " + fullString);
                            events.add(interaction + fullString);
                        }
                    }
                }
            }
        }

        context.put("item", item);
        context.put("inventory", inventory);
        context.put("click", new Element(click));
        context.put("slot_type", new Element(slotType));
        context.put("slot", new Element(event.getSlot() + 1));
        context.put("raw_slot", new Element(event.getRawSlot() + 1));
        context.put("is_shift_click", new Element(event.isShiftClick()));
        context.put("action", new Element(event.getAction().name()));
        context.put("hotbar_button", new Element(event.getHotbarButton() + 1));

        String determination = doEvents(events, null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            event.setCancelled(true);
            final InventoryHolder holder = event.getInventory().getHolder();
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getPlayerEntity().updateInventory();
                    if (holder != null && holder instanceof Player) {
                        ((Player) holder).updateInventory();
                    }
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
        }
        else if (dItem.matches(determination)) {
            dItem dit = dItem.valueOf(determination, player, null);
            if (dit == null) {
                dB.echoError("Invalid dItem: " + dit);
            }
            else {
                event.setCurrentItem(dit.getItemStack());
            }
        }
    }

    /////////////////////
    //   PLAYER EVENTS
    /////////////////

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoins(PlayerJoinEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        if (ScoreboardHelper.viewerMap.containsKey(event.getPlayer().getName())) {
            Scoreboard score = ScoreboardHelper.getScoreboard(ScoreboardHelper.viewerMap.get(event.getPlayer().getName()));
            if (score != null) {
                event.getPlayer().setScoreboard(score);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final String message = ChatColor.DARK_GREEN + "CHAT: " +
                event.getPlayer().getName() + ": " + event.getMessage();
        Bukkit.getScheduler().runTaskLater(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                // If currently recording debug information, add the chat message to debug output
                if (dB.record) {
                    dB.log(message);
                }
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLogin(PlayerLoginEvent event) {

        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }

        dPlayer.notePlayer(event.getPlayer());
    }

}

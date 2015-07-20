package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.objects.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.List;

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
    // @Triggers when the server starts
    //
    // @Determine
    // "CANCELLED" to save all plugins and cancel server startup.
    //
    // -->
    public void serverStartEvent() {
        // Start the 'timeEvent'
        long ticks = Settings.worldScriptTimeEventFrequency().getTicks();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        timeEvent();
                    }
                }, ticks, ticks);

        // Fire the 'Server Start' event
        String determination = doEvents(Arrays.asList("server start"),
                null, null, null);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            Bukkit.getServer().shutdown(); // TODO: WHY IS THIS AN OPTION?!
    }

    private final Map<String, Integer> current_time = new HashMap<String, Integer>();

    // <--[event]
    // @Events
    // time changes (in <world>)
    // <0-23>:00 in <world>
    // time <0-23> in <world>
    //
    // @Triggers when the current time changes in a world (once per mine-hour).
    // @Context
    // <context.time> returns the current time.
    // <context.world> returns the world.
    //
    // -->
    public void timeEvent() {
        for (World world : Bukkit.getWorlds()) {
            int hour = Double.valueOf(world.getTime() / 1000).intValue(); // TODO: WTF is this conversion math
            hour = hour + 6;
            // Get the hour
            if (hour >= 24) hour = hour - 24;

            dWorld currentWorld = new dWorld(world);

            if (!current_time.containsKey(currentWorld.identifySimple())
                    || current_time.get(currentWorld.identifySimple()) != hour) {
                Map<String, dObject> context = new HashMap<String, dObject>();

                context.put("time", new Element(hour));
                context.put("world", currentWorld);

                doEvents(Arrays.asList
                                ("time changes",
                                        "time changes in " + currentWorld.identifySimple(),
                                        String.valueOf(hour) + ":00 in " + currentWorld.identifySimple(),
                                        "time " + String.valueOf(hour) + " in " + currentWorld.identifySimple()),
                        null, null, context, true);

                current_time.put(currentWorld.identifySimple(), hour);
            }
        }
    }

    /////////////////////
    //   INVENTORY EVENTS
    /////////////////

    // <--[event]
    // @Events
    // item crafted
    // <item> crafted
    // <material> crafted
    //
    // @Triggers when an item's recipe is correctly formed.
    // @Context
    // <context.inventory> returns the dInventory of the crafting inventory.
    // <context.item> returns the dItem to be crafted.
    // <context.recipe> returns a dList of dItems in the recipe.
    //
    // @Determine
    // "CANCELLED" to stop the item from being crafted.
    // dItem to change the item that is crafted.
    //
    // -->
    @EventHandler
    public void craftItemEvent(PrepareItemCraftEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        List<String> events = new ArrayList<String>();
        events.add("item crafted");

        CraftingInventory inventory = event.getInventory();
        context.put("inventory", new dInventory(inventory));

        Recipe recipe = event.getRecipe();
        dItem result = recipe.getResult() != null ? new dItem(recipe.getResult()) : null;
        if (result != null) {
            context.put("item", result);
            events.add(result.identifySimple() + " crafted");
            events.add(result.identifyMaterial() + " crafted");
        }

        dList recipeList = new dList();
        for (ItemStack item : inventory.getMatrix()) {
            if (item != null)
                recipeList.add(new dItem(item).identify());
            else
                recipeList.add(new dItem(Material.AIR).identify());
        }
        context.put("recipe", recipeList);

        Player player = (Player) event.getView().getPlayer();

        String determination = doEvents(events, null, dEntity.getPlayerFrom(player), context);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            inventory.setResult(null);
            player.updateInventory();
        }
        else if (dItem.matches(determination)) {
            inventory.setResult(dItem.valueOf(determination).getItemStack());
            player.updateInventory();
        }
    }

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
    // player (<click type>) clicks (<item>) (in <inventory type>) (with <item>)
    // player (<click type>) clicks (<material>) (in <inventory type>) (with <item>)
    // player (<click type>) clicks (<item>) (in <inventory type>) (with <material>)
    // player (<click type>) clicks (<material>) (in <inventory type>) (with <material>)
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
    // <context.hotbar_button> returns an Element of the button pressed as a number, or -1 if no number button was pressed.
    //
    // @Determine
    // "CANCELLED" to stop the player from clicking.
    //
    // -->
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {

        // TODO: make this a script event...

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = null;
        dItem holding;

        dInventory inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        final dPlayer player = dEntity.getPlayerFrom((Player) event.getWhoClicked());
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

            if (event.getCursor() != null) {
                holding = new dItem(event.getCursor());

                final String[] itemStrings = new String[]{
                        item.identifySimple(),
                        item.identifyMaterial()
                };
                final String[] inventoryStrings = new String[]{
                        "inventory",
                        type,
                        inventory.identifySimple()
                };
                final String[] holdingStrings = new String[]{
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
        context.put("hotbar_button", new Element(event.getHotbarButton()));

        String determination = doEvents(events, null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            event.setCancelled(true);
            final InventoryHolder holder = event.getInventory().getHolder();
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getPlayerEntity().updateInventory();
                    if (holder != null && holder instanceof Player)
                        ((Player) holder).updateInventory();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
        }
    }

    // <--[event]
    // @Events
    // player drags in inventory
    // player drags (<item>) (in <inventory type>)
    //
    // @Triggers when a player drags in an inventory.
    // @Context
    // <context.item> returns the dItem the player has dragged.
    // <context.inventory> returns the dInventory.
    // <context.slots> returns a dList of the slot numbers dragged through.
    // <context.raw_slots> returns a dList of the raw slot numbers dragged through.
    //
    // @Determine
    // "CANCELLED" to stop the player from dragging.
    //
    // -->
    @EventHandler
    public void inventoryDragEvent(InventoryDragEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = null;

        Inventory inventory = event.getInventory();
        final dPlayer player = dEntity.getPlayerFrom((Player) event.getWhoClicked());
        String type = event.getInventory().getType().name();

        List<String> events = new ArrayList<String>();
        events.add("player drags");
        events.add("player drags in inventory");
        events.add("player drags in " + type);

        if (event.getOldCursor() != null) {

            item = new dItem(event.getOldCursor());

            events.add("player drags " +
                    item.identifySimple());
            events.add("player drags " +
                    item.identifySimple() + " in inventory");
            events.add("player drags " +
                    item.identifySimple() + " in " + type);
            events.add("player drags " +
                    item.identifyMaterial());
            events.add("player drags " +
                    item.identifyMaterial() + " in inventory");
            events.add("player drags " +
                    item.identifyMaterial() + " in " + type);
        }

        context.put("item", item);
        context.put("inventory", dInventory.mirrorBukkitInventory(inventory));
        dList slots = new dList();
        for (Integer slot : event.getInventorySlots()) {
            slots.add(slot.toString());
        }
        context.put("slots", slots);
        dList raw_slots = new dList();
        for (Integer raw_slot : event.getRawSlots()) {
            raw_slots.add(raw_slot.toString());
        }
        context.put("raw_slots", raw_slots);

        String determination = doEvents(events, null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            event.setCancelled(true);
            final InventoryHolder holder = inventory.getHolder();
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getPlayerEntity().updateInventory();
                    if (holder != null && holder instanceof Player)
                        ((Player) holder).updateInventory();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
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
            if (score != null)
                event.getPlayer().setScoreboard(score);
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
                if (dB.record) dB.log(message);
            }
        }, 1);
    }

    // <--[event]
    // @Events
    // player edits book
    // player signs book
    //
    // @Triggers when a player edits or signs a book.
    // @Context
    // <context.title> returns the name of the book, if any.
    // <context.pages> returns the number of pages in the book.
    // <context.book> returns the book item being edited.
    // <context.signing> returns whether the book is about to be signed.
    //
    // @Determine
    // "CANCELLED" to prevent the book from being edited.
    // "NOT_SIGNING" to prevent the book from being signed.
    // dScript to set the book information to set it to instead.
    //
    // -->
    @EventHandler
    public void playerEditBook(PlayerEditBookEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        if (event.isSigning()) context.put("title", new Element(event.getNewBookMeta().getTitle()));
        context.put("pages", new Element(event.getNewBookMeta().getPageCount()));
        context.put("book", new dItem(event.getPlayer().getInventory().getItem(event.getSlot())));
        context.put("signing", new Element(event.isSigning()));

        ArrayList<String> events = new ArrayList<String>();

        events.add("player edits book");
        if (event.isSigning()) {
            events.add("player signs book");
        }

        String determination = doEvents(events,
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (determination.toUpperCase().startsWith("NOT_SIGNING"))
            event.setSigning(false);
        else if (dScript.matches(determination)) {
            dScript script = dScript.valueOf(determination);
            if (script.getContainer() instanceof BookScriptContainer) {
                dItem book = ((BookScriptContainer) script.getContainer()).getBookFrom(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
                event.setNewBookMeta((BookMeta) book.getItemStack().getItemMeta());
                if (book.getItemStack().getType() == Material.BOOK_AND_QUILL)
                    event.setSigning(false);
            }
            else {
                dB.echoError("Script '" + determination + "' is valid, but not of type 'book'!");
            }
        }
    }

    // <--[event]
    // @Events
    // player clicks block
    // player (<click type>) clicks (<material>) (with <item>) (in <area>)
    // player (<click type>) clicks block (with <item>)
    // player stands on <pressure plate>
    //
    // @Triggers when a player clicks on a block or stands on a pressure plate.
    // @Context
    // <context.item> returns the dItem the player is clicking with.
    // <context.location> returns the dLocation the player is clicking on.
    // <context.cuboids> DEPRECATED.
    // <context.click_type> returns an Element of the click type.
    // <context.relative> returns a dLocation of the air block in front of the clicked block.
    //
    // @Determine
    // "CANCELLED" to stop the click from happening.
    // "CANCELLED:FALSE" to uncancel the event. Some plugins may have this cancelled by default.
    //
    // -->
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        Action action = event.getAction();
        dItem item = null;
        dPlayer player = dEntity.getPlayerFrom(event.getPlayer());

        List<String> events = new ArrayList<String>();

        if (event.getBlockFace() != null && event.getClickedBlock() != null) {
            context.put("relative", new dLocation(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation()));
        }

        String[] interactions;

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            interactions = new String[]{"player left clicks", "player clicks"};
        }
        else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            interactions = new String[]{"player right clicks", "player clicks"};
        }
        // The only other action is PHYSICAL, which is triggered when a player
        // stands on a pressure plate
        else interactions = new String[]{"player stands on"};
        context.put("click_type", new Element(action.name()));

        for (String interaction : interactions) // TODO: addAll?
            events.add(interaction);

        if (event.hasItem()) {
            item = new dItem(event.getItem());
            context.put("item", item);

            for (String interaction : interactions) {
                events.add(interaction + " with item");
                events.add(interaction + " with " + item.identifySimple());
                events.add(interaction + " with " + item.identifyMaterial());
            }
        }

        if (event.hasBlock()) {
            Block block = event.getClickedBlock();
            dMaterial blockMaterial = dMaterial.getMaterialFrom(block.getType(), block.getData());
            context.put("location", new dLocation(block.getLocation()));

            for (String interaction : interactions) {
                events.add(interaction + " block");
                events.add(interaction + " " + blockMaterial.identifySimple());
            }

            if (event.hasItem()) {
                for (String interaction : interactions) {
                    events.add(interaction + " block with item");
                    events.add(interaction + " block with " + item.identifySimple());
                    events.add(interaction + " block with " + item.identifyMaterial());
                    events.add(interaction + " " + blockMaterial.identifySimple() +
                            " with item");
                    events.add(interaction + " " + blockMaterial.identifySimple() +
                            " with " + item.identifySimple());
                    events.add(interaction + " " + blockMaterial.identifySimple() +
                            " with " + item.identifyMaterial());
                }
            }

            // Look for cuboids that contain the block's location
            List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getClickedBlock().getLocation());

            dList cuboid_context = new dList();
            for (String interaction : interactions) {
                if (cuboids.size() > 0) {
                    events.add(interaction + " block in notable cuboid");
                    events.add(interaction + ' ' + blockMaterial.identifySimple() + " in notable cuboid");
                }
                // TODO: Add all events + in <cuboid>
                for (dCuboid cuboid : cuboids) {
                    events.add(interaction + " block in " + cuboid.identifySimple());
                    events.add(interaction + ' ' + blockMaterial.identifySimple() + " in " + cuboid.identifySimple());
                }
            }
            for (dCuboid cuboid : cuboids) {
                cuboid_context.add(cuboid.identifySimple());
            }
            // Add in cuboids context, with either the cuboids or an empty list
            context.put("cuboids", cuboid_context);

        }

        String determination = doEvents(events, null, player, context, true).toUpperCase();

        if (determination.startsWith("CANCELLED:FALSE"))
            event.setCancelled(false);
        else if (determination.startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player right clicks at entity (with <item>)
    // player right clicks at entity in <area>
    // player right clicks at entity in notable cuboid
    // player right clicks at <entity> (with <item>)
    // player right clicks at <entity> in <area>
    // player right clicks at <entity> in notable cuboid

    // @Triggers when a player right clicks at an entity (Similar to right clicks entity, but for armor stands).
    // @Context
    // <context.entity> returns the dEntity the player is clicking on.
    // <context.item> returns the dItem the player is clicking with.
    // <context.cuboids> DEPRECATED.
    // <context.location> returns a dLocation of the clicked entity.
    //
    // @Determine
    // "CANCELLED" to stop the click from happening.
    //
    // -->
    @EventHandler
    public void playerInteractStand(PlayerInteractAtEntityEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getPlayer().getWorld(),
                event.getClickedPosition().getX(),
                event.getClickedPosition().getY(),
                event.getClickedPosition().getZ()));
        dEntity entity = new dEntity(event.getRightClicked());
        context.put("entity", entity);
        dItem item = new dItem(event.getPlayer().getItemInHand());
        context.put("item", item);
        dNPC npc = null;
        if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
        List<String> events = new ArrayList<String>();
        events.add("player right clicks at entity");
        events.add("player right clicks at " + entity.identifyType());
        events.add("player right clicks at entity with " +
                item.identifySimple());
        events.add("player right clicks at " + entity.identifyType() + " with " +
                item.identifySimple());
        events.add("player right clicks at entity with " +
                item.identifyMaterial());
        events.add("player right clicks at " + entity.identifyType() + " with " +
                item.identifyMaterial());
        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getRightClicked().getLocation());
        if (cuboids.size() > 0) {
            events.add("player right clicks at entity in notable cuboid");
            events.add("player right clicks at " + entity.identifyType() + " in notable cuboid");
        }
        dList cuboid_context = new dList();
        for (dCuboid cuboid : cuboids) {
            events.add("player right clicks at entity in " + cuboid.identifySimple());
            events.add("player right clicks at " + entity.identifyType() + " in " + cuboid.identifySimple());
            cuboid_context.add(cuboid.identifySimple());
        }
        // Add in cuboids context, with either the cuboids or an empty list
        context.put("cuboids", cuboid_context);
        List<String> determinations = OldEventManager.doEvents(events,
                new BukkitScriptEntryData(dEntity.getPlayerFrom(event.getPlayer()), npc), context, true);
        for (String determination : determinations) {
            if (determination.equalsIgnoreCase("CANCELLED")) {
                event.setCancelled(true);
            }
        }
    }

    // <--[event]
    // @Events
    // player right clicks entity (with <item>)
    // player right clicks entity in <area>
    // player right clicks entity in notable cuboid
    // player right clicks <entity> (with <item>)
    // player right clicks <entity> in <area>
    // player right clicks <entity> in notable cuboid

    // @Triggers when a player right clicks on an entity.
    // @Context
    // <context.entity> returns the dEntity the player is clicking on.
    // <context.item> returns the dItem the player is clicking with.
    // <context.cuboids> DEPRECATED.
    // <context.location> returns a dLocation of the clicked entity.
    //
    // @Determine
    // "CANCELLED" to stop the click from happening.
    //
    // -->
    @EventHandler
    public void playerInteractEntity(PlayerInteractEntityEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        String determination;
        dNPC npc = null;

        dItem item = new dItem(event.getPlayer().getItemInHand());
        dEntity entity = new dEntity(event.getRightClicked());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getRightClicked().getLocation()));
        context.put("entity", entity.getDenizenObject());
        context.put("item", item);

        if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();

        List<String> events = new ArrayList<String>();
        events.add("player right clicks entity");
        events.add("player right clicks " + entity.identifyType());
        events.add("player right clicks entity with " +
                item.identifySimple());
        events.add("player right clicks " + entity.identifyType() + " with " +
                item.identifySimple());
        events.add("player right clicks entity with " +
                item.identifyMaterial());
        events.add("player right clicks " + entity.identifyType() + " with " +
                item.identifyMaterial());

        if (entity.getBukkitEntity() instanceof ItemFrame) {
            dItem itemFrame = new dItem(((ItemFrame) entity.getBukkitEntity()).getItem());
            context.put("itemframe", itemFrame);

            events.add("player right clicks " + entity.identifyType() + " " +
                    itemFrame.identifySimple());
            events.add("player right clicks " + entity.identifyType() + " " +
                    itemFrame.identifyMaterial());
        }

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getRightClicked().getLocation());

        if (cuboids.size() > 0) {
            events.add("player right clicks entity in notable cuboid");
            events.add("player right clicks " + entity.identifyType() + " in notable cuboid");
        }

        dList cuboid_context = new dList();
        for (dCuboid cuboid : cuboids) {
            events.add("player right clicks entity in " + cuboid.identifySimple());
            events.add("player right clicks " + entity.identifyType() + " in " + cuboid.identifySimple());
            cuboid_context.add(cuboid.identifySimple());
        }
        // Add in cuboids context, with either the cuboids or an empty list
        context.put("cuboids", cuboid_context);

        determination = doEvents(events, npc, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void playerLogin(PlayerLoginEvent event) {

        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }

        dPlayer.notePlayer(event.getPlayer());
    }

}

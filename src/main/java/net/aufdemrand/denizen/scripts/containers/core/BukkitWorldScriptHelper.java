package net.aufdemrand.denizen.scripts.containers.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH.Argument;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.*;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.world.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BlockIterator;

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
        return determ.size() > 0 ? determ.get(0): "none";
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
            int hour = Double.valueOf(world.getTime() / 1000).intValue();
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
    //   Additional EVENTS
    /////////////////

    // <--[event]
    // @Events
    // item despawns
    // <item> despawns
    // <material> despawns
    //
    // @Triggers when an item entity despawns.
    // @Context
    // <context.item> returns the dItem of the entity.
    // <context.entity> returns the dEntity.
    //
    // @Determine
    // "CANCELLED" to stop the item entity from despawning.
    //
    // -->
    @EventHandler
    public void itemDespawn(ItemDespawnEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(event.getEntity().getItemStack());

        context.put("item", item);
        context.put("entity", new dEntity(event.getEntity()));

        List<String> events = new ArrayList<String>();
        events.add("item despawns");
        events.add(item.identifySimple() + " despawns");
        events.add(item.identifyMaterial() + " despawns");

        String determination = doEvents(events, null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // projectile hits block
    // projectile hits <material>
    // <projectile> hits block
    // <projectile> hits <material>
    //
    // @Triggers when a projectile hits a block.
    // @Context
    // <context.projectile> returns the dEntity of the projectile.
    // <context.shooter> returns the dEntity of the shooter, if there is one.
    // <context.location> returns the dLocation of the block that was hit.
    //
    // -->
    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {

        dPlayer player = null;
        dNPC npc = null;

        if (event.getEntity() == null)
            return;

        dEntity projectile = new dEntity(event.getEntity());

        if (projectile.getLocation() == null)
            return; // No, I can't explain how or why this would ever happen... nonetheless, it appears it does happen sometimes.

        if (Double.isNaN(projectile.getLocation().getDirection().normalize().getX()))
            return; // I can't explain this one either. It also chooses to happen whenever it pleases.

        Block block = null;
        try {
            BlockIterator bi = new BlockIterator(projectile.getLocation().getWorld(),
                    projectile.getLocation().toVector(), projectile.getLocation().getDirection().normalize(), 0, 4);
            while(bi.hasNext()) {
                block = bi.next();
                if(block.getTypeId() != 0) {
                    break;
                }
            }
        }
        catch (IllegalStateException ex) {
            // This happens because it can. Also not explainable whatsoever.
            // As this error happens on no fault of the user, display no error message... just cancel the event.
            return;
        }

        if (block == null)
            return;

        dEntity shooter = projectile.getShooter();
        dMaterial material = dMaterial.getMaterialFrom(block.getType(), block.getData());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("projectile", projectile);
        context.put("location", new dLocation(block.getLocation()));

        List<String> events = new ArrayList<String>();
        events.add("projectile hits block");
        events.add("projectile hits " + material.identifySimple());
        events.add(projectile.identifyType() + " hits block");
        events.add(projectile.identifyType() + " hits " + material.identifySimple());

        if (shooter != null) {
            context.put("shooter", shooter.getDenizenObject());

            // <--[event]
            // @Events
            // entity shoots block
            // entity shoots <material> (with <projectile>)
            // <entity> shoots block
            // <entity> shoots <material> (with <projectile>)
            //
            // @Triggers when a projectile shot by an entity hits a block.
            // @Context
            // <context.projectile> returns the dEntity of the projectile.
            // <context.shooter> returns the dEntity of the shooter, if there is one.
            // <context.location> returns the dLocation of the block that was hit.
            //
            // -->

            if (shooter.isCitizensNPC()) { npc = shooter.getDenizenNPC(); }
            else if (shooter.isPlayer()) { player = shooter.getDenizenPlayer(); }

            events.add("entity shoots block");
            events.add("entity shoots block with " + projectile.identifyType());
            events.add("entity shoots " + material.identifySimple() + " with " + projectile.identifyType());
            events.add("entity shoots " + material.identifySimple());
            events.add(shooter.identifyType() + " shoots block");
            events.add(shooter.identifyType() + " shoots block with " + projectile.identifyType());
            events.add(shooter.identifyType() + " shoots " + material.identifySimple() + " with " + projectile.identifyType());
            events.add(shooter.identifyType() + " shoots " + material.identifySimple());
        }

        doEvents(events, npc, player, context, true);
    }

    // <--[event]
    // @Events
    // projectile launched
    // <entity> launched
    //
    // @Triggers when a projectile is launched.
    // @Context
    // <context.entity> returns the projectile.
    //
    // @Determine
    // "CANCELLED" to stop it from being launched.
    //
    // -->
    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        List<String> events = new ArrayList<String>();
        dEntity projectile = new dEntity(event.getEntity());
        context.put("entity", projectile);
        events.add("projectile launched");
        events.add(projectile.identifySimple() + " launched");
        events.add(projectile.identifyType() + " launched");
        String Determination = doEvents(events, null, null, context, true);
        if (Determination.equalsIgnoreCase("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // sheep regrows wool
    //
    // @Triggers when a sheep regrows wool.
    // @Context
    // <context.entity> returns the dEntity of the sheep.
    //
    // @Determine
    // "CANCELLED" to stop it from regrowing wool.
    //
    // -->
    @EventHandler
    public void sheepRegrowWool(SheepRegrowWoolEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("entity", entity);

        String determination = doEvents(Arrays.asList
                ("sheep regrows wool"), null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // slime splits (into <#>)
    //
    // @Triggers when a slime splits into smaller slimes.
    // @Context
    // <context.entity> returns the dEntity of the slime.
    // <context.count> returns an Element(Number) of the number of smaller slimes it will split into.
    //
    // @Determine
    // "CANCELLED" to stop it from splitting.
    // Element(Number) to set the number of smaller slimes it will split into.
    //
    // -->
    @EventHandler
    public void slimeSplit(SlimeSplitEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        int count = event.getCount();

        context.put("entity", entity);
        context.put("count", new Element(count));

        String determination = doEvents(Arrays.asList
                ("slime splits",
                        "slime splits into " + count),
                null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Integer)) {
            event.setCount(aH.getIntegerFrom(determination));
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

    // <--[event]
    // @Events
    // item enchanted
    // <item> enchanted
    //
    // @Triggers when an item is enchanted.
    // @Context
    // <context.location> returns the dLocation of the enchanting table.
    // <context.inventory> returns the dInventory of the enchanting table.
    // <context.item> returns the dItem to be enchanted.
    // <context.button> returns which button was pressed to initiate the enchanting.
    // <context.cost> returns the experience level cost of the enchantment.
    //
    // @Determine
    // Element(Number) to set the experience level cost of the enchantment.
    // "CANCELLED" to stop the item from being enchanted.
    //
    // -->
    @EventHandler
    public void enchantItemEvent(EnchantItemEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        dPlayer player = dEntity.getPlayerFrom(event.getEnchanter());
        dItem item = new dItem(event.getItem());

        context.put("location", new dLocation(event.getEnchantBlock().getLocation()));
        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));
        context.put("item", item);
        context.put("button", new Element(event.whichButton()));
        context.put("cost", new Element(event.getExpLevelCost()));

        String determination = doEvents(Arrays.asList
                ("item enchanted",
                        item.identifySimple() + " enchanted"),
                null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (Argument.valueOf(determination).matchesPrimitive(aH.PrimitiveType.Integer))
            event.setExpLevelCost(Integer.valueOf(determination));
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

                final String[] itemStrings = new String[] {
                        item.identifySimple(),
                        item.identifyMaterial()
                };
                final String[] inventoryStrings = new String[] {
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
    // player closes inventory
    // player closes <inventory type>
    // npc closes inventory
    // npc closes <inventory type>
    //
    // @Triggers when a player closes an inventory. (EG, chests, not the player's main inventory.)
    // @Context
    // <context.inventory> returns the dInventory.
    //
    // -->
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();

        dEntity entity = new dEntity(event.getPlayer());

        dPlayer player = null;
        dNPC npc = null;

        if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getDenizenPlayer();

        String type = event.getInventory().getType().name();

        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));

        doEvents(Arrays.asList
                ((player != null ? "player": "npc") + " closes inventory",
                        (player != null ? "player": "npc") + " closes " + type),
                npc, player, context);
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

    // <--[event]
    // @Events
    // player opens inventory
    // player opens <inventory type>
    //
    // @Triggers when a player opens an inventory. (EG, chests, not the player's main inventory.)
    // @Context
    // <context.inventory> returns the dInventory.
    //
    // @Determine
    // "CANCELLED" to stop the player from opening the inventory.
    //
    // -->
    @EventHandler
    public void inventoryOpenEvent(InventoryOpenEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();

        String type = event.getInventory().getType().name();

        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));

        String determination = doEvents(Arrays.asList
                ("player opens inventory",
                        "player opens " + type),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // inventory picks up item
    // inventory picks up <item>
    // <inventory type> picks up item
    // <inventory type> picks up <item>
    //
    // @Triggers when a hopper or hopper minecart picks up an item.
    // @Context
    // <context.inventory> returns the dInventory that picked up the item.
    // <context.item> returns the dItem.
    //
    // @Determine
    // "CANCELLED" to stop the item from being moved.
    //
    // -->
    @EventHandler
    public void inventoryPickupItemEvent(InventoryPickupItemEvent event) {

        // Too laggy! TODO: Evaluate further.
        if (event.getInventory().getType() == InventoryType.HOPPER)
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();

        String type = event.getInventory().getType().name();
        dItem item = new dItem(event.getItem());

        List<String> events = Arrays.asList("inventory picks up item",
                "inventory picks up " + item.identifySimple(),
                type + " picks up item",
                type + " picks up " + item.identifySimple());

        dInventory inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        context.put("inventory", inventory);
        context.put("item", item);

        String determination = doEvents(events,
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }


    /////////////////////
    //   PLAYER EVENTS
    /////////////////


    // Original chat events moved to smart event, this event just retained
    // for debug chat recording.
    @EventHandler
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
                dItem book = ((BookScriptContainer)script.getContainer()).getBookFrom(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
                event.setNewBookMeta((BookMeta) book.getItemStack().getItemMeta());
                if (book.getItemStack().getType() == Material.BOOK_AND_QUILL)
                    event.setSigning(false);
            }
            else {
                dB.echoError("Script '"  + determination + "' is valid, but not of type 'book'!");
            }
        }
    }

    // <--[event]
    // @Events
    // player breaks item
    // player breaks <item>
    //
    // @Triggers when a player breaks the item they are holding.
    // @Context
    // <context.item> returns the item that broke.
    //
    // @Determine
    // "CANCELLED" to prevent the item from breaking, restoring it with one usage left.
    // -->
    @EventHandler
    public void playerBreakItem(PlayerItemBreakEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        final ItemStack itemstack = event.getBrokenItem();
        dItem item = new dItem(itemstack);
        context.put("item", item);

        String determination = doEvents(Arrays.asList
                ("player breaks item",
                        "player breaks " + item.identifySimple(),
                        "player breaks " + item.identifyMaterial()),
                null, dEntity.getPlayerFrom(event.getPlayer()), context).toUpperCase();

        if (determination.startsWith("CANCELLED")) {
            // The ItemStack isn't really gone yet, only set to stack size 0.
            // So just add 1 more item to the stack.
            itemstack.setAmount(itemstack.getAmount()+1);
            // The event automatically resets durability to 0... instead,
            // let's delay a tick and set it back to what it was before.
            final Player player = event.getPlayer();
            new BukkitRunnable() {
                @Override
                public void run() {
                    itemstack.setDurability(itemstack.getType().getMaxDurability());
                    player.updateInventory();
                }
            }.runTaskLater(DenizenAPI.getCurrentInstance(), 1);
        }

    }

    // <--[event]
    // @Events
    // player animates (<animation>)
    //
    // @Triggers when a player performs an animation.
    // @Context
    // <context.animation> returns the name of the animation.
    //
    // @Determine
    // "CANCELLED" to stop the player from animating.
    //
    // -->
    @EventHandler
    public void playerAnimation(PlayerAnimationEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        String animation = event.getAnimationType().name();
        context.put("animation", new Element(animation));

        String determination = doEvents(Arrays.asList
                ("player animates",
                        "player animates " + animation),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player enters bed
    //
    // @Triggers when a player enters a bed.
    // @Context
    // <context.location> returns the dLocation of the bed.
    //
    // @Determine
    // "CANCELLED" to stop the player from entering the bed.
    //
    // -->
    @EventHandler
    public void playerBedEnter(PlayerBedEnterEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        String determination = doEvents
                (Arrays.asList("player enters bed"),
                        null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player leaves bed
    //
    // @Triggers when a player leaves a bed.
    // @Context
    // <context.location> returns the dLocation of the bed.
    //
    // -->
    @EventHandler
    public void playerBedLeave(PlayerBedLeaveEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        doEvents(Arrays.asList
                ("player leaves bed"),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);
    }

    // <--[event]
    // @Events
    // player changes world
    // player changes world from <world>
    // player changes world to <world>
    // player changes world from <world> to <world>
    //
    // @Triggers when a player moves to a different world.
    // @Context
    // <context.origin_world> returns the dWorld that the player was previously on.
    // <context.destination_world> returns the dWorld that the player is now in.
    //
    // -->
    @EventHandler
    public void playerChangedWorld(PlayerChangedWorldEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld originWorld = new dWorld(event.getFrom());
        dWorld destinationWorld = new dWorld(event.getPlayer().getWorld());
        context.put("origin_world", originWorld);
        context.put("destination_world", destinationWorld);

        doEvents(Arrays.asList
                ("player changes world",
                        "player changes world from " + originWorld.identifySimple(),
                        "player changes world to " + destinationWorld.identifySimple(),
                        "player changes world from " + originWorld.identifySimple() +
                                " to " + destinationWorld.identifySimple()),
                null, dEntity.getPlayerFrom(event.getPlayer()), context, true);
    }

    // <--[event]
    // @Events
    // player drops item
    // player drops <item>
    //
    // @Triggers when a player drops an item.
    // @Context
    // <context.item> returns the dItem.
    // <context.entity> returns a dEntity of the item.
    // <context.location> returns a dLocation of the item's location.
    //
    // @Determine
    // "CANCELLED" to stop the item from being dropped.
    //
    // -->
    @EventHandler
    public void playerDropItem(PlayerDropItemEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(event.getItemDrop().getItemStack());
        context.put("item", item);
        context.put("entity", new dEntity(event.getItemDrop()));
        context.put("location", new dLocation(event.getItemDrop().getLocation()));

        List<String> events = new ArrayList<String>();

        events.add("player drops item");
        events.add("player drops " + item.identifySimple());

        String determination = doEvents(events, null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player throws (hatching/non-hatching) egg
    //
    // @Triggers when a player throws an egg.
    // @Context
    // <context.egg> returns the dEntity of the egg.
    // <context.is_hatching> returns an Element with a value of "true" if the egg will hatch and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the hatching.
    // dEntity to set the type of the hatching entity.
    //
    // -->
    @EventHandler
    public void playerEggThrow(PlayerEggThrowEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity egg = new dEntity(event.getEgg());
        context.put("egg", egg);
        context.put("is_hatching", new Element(event.isHatching()));

        List<String> events = new ArrayList<String>();
        events.add("player throws egg");

        if (event.isHatching()) events.add("player throws hatching egg");
        else                    events.add("player throws non-hatching egg");

        String determination = doEvents(events, null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.equalsIgnoreCase("CANCELLED")) {
            event.setHatching(false);
        }
        else if (dEntity.matches(determination)) {
            event.setHatching(true);
            event.setHatchingType(dEntity.valueOf(determination).getBukkitEntityType());
        }
    }

    // <--[event]
    // @Events
    // player changes xp
    //
    // @Triggers when a player's experience amount changes.
    // @Context
    // <context.amount> returns the amount of changed experience.
    //
    // @Determine
    // "CANCELLED" to stop the player from changing experience.
    // Element(Number) to set the amount of changed experience.
    //
    // -->
    @EventHandler
    public void playerExpChange(PlayerExpChangeEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("amount", new Element(event.getAmount()));

        String determination = doEvents(Arrays.asList
                ("player changes xp"),
                null, dEntity.getPlayerFrom(event.getPlayer()), context).toUpperCase();

        if (determination.equals("CANCELLED")) {
            event.setAmount(0);
        }
        else if (Argument.valueOf(determination).matchesPrimitive(aH.PrimitiveType.Integer)) {
            event.setAmount(Integer.valueOf(determination));
        }
    }

    // <--[event]
    // @Events
    // player fishes (<entity>) (while <state>)
    //
    // @Triggers when a player uses a fishing rod.
    // @Context
    // <context.hook> returns a dEntity of the hook.
    // <context.state> returns an Element of the fishing state.
    // <context.entity> returns a dEntity of the entity that got caught.
    // <context.item> returns a dItem of the item gotten, if any.
    //
    // @Determine
    // "CANCELLED" to stop the player from fishing.
    //
    // -->
    @EventHandler
    public void playerFish(PlayerFishEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        dNPC npc = null;
        String state = event.getState().name();

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("hook", new dEntity(event.getHook()));
        context.put("state", new Element(state));

        List<String> events = new ArrayList<String>();
        events.add("player fishes");
        events.add("player fishes while " + state);

        if (event.getCaught() != null) {

            Entity caught = event.getCaught();
            dEntity entity = new dEntity(caught);
            context.put("entity", entity.getDenizenObject());
            if (caught instanceof Item) {
                context.put("item", new dItem(((Item) caught).getItemStack()));
            }

            if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();

            events.add("player fishes " + entity.identifyType());
            events.add("player fishes " + entity.identifyType() + " while " + state);
        }

        String determination = doEvents(events, npc, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player changes gamemode (to <gamemode>)
    //
    // @Triggers when a player's gamemode is changed.
    // @Context
    // <context.gamemode> returns an Element of the gamemode.
    //
    // @Determine
    // "CANCELLED" to stop the gamemode from being changed.
    //
    // -->
    @EventHandler
    public void playerGameModeChange(PlayerGameModeChangeEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("gamemode", new Element(event.getNewGameMode().name()));

        String determination = doEvents(Arrays.asList
                ("player changes gamemode",
                        "player changes gamemode to " + event.getNewGameMode().name()),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player clicks block
    // player (<click type>) clicks (<material>) (with <item>) (in <notable cuboid>)
    // player (<click type>) clicks block (with <item>)
    // player stands on <pressure plate>
    //
    // @Triggers when a player clicks on a block or stands on a pressure plate.
    // @Context
    // <context.item> returns the dItem the player is clicking with.
    // <context.location> returns the dLocation the player is clicking on.
    // <context.cuboids> returns a dList of the notable cuboids that contain the clicked block. DEPRECATED.
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
    // player right clicks at entity in <notable cuboid>
    // player right clicks at entity in notable cuboid
    // player right clicks at <entity> (with <item>)
    // player right clicks at <entity> in <notable cuboid>
    // player right clicks at <entity> in notable cuboid

    // @Triggers when a player right clicks at an entity (Similar to right clicks entity, but for armor stands).
    // @Context
    // <context.entity> returns the dEntity the player is clicking on.
    // <context.item> returns the dItem the player is clicking with.
    // <context.cuboids> returns a dList of cuboids that contain the interacted entity. DEPRECATED.
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
        for (String determination: determinations) {
            if (determination.equalsIgnoreCase("CANCELLED")) {
                event.setCancelled(true);
            }
        }
    }

    // <--[event]
    // @Events
    // player right clicks entity (with <item>)
    // player right clicks entity in <notable cuboid>
    // player right clicks entity in notable cuboid
    // player right clicks <entity> (with <item>)
    // player right clicks <entity> in <notable cuboid>
    // player right clicks <entity> in notable cuboid

    // @Triggers when a player right clicks on an entity.
    // @Context
    // <context.entity> returns the dEntity the player is clicking on.
    // <context.item> returns the dItem the player is clicking with.
    // <context.cuboids> returns a dList of cuboids that contain the interacted entity. DEPRECATED.
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

    // <--[event]
    // @Events
    // player consumes item
    // player consumes <item>
    //
    // @Triggers when a player consumes an item.
    // @Context
    // <context.item> returns the dItem.
    //
    // @Determine
    // "CANCELLED" to stop the item from being consumed.
    // dItem to change the item being consumed.
    //
    // -->
    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        dItem item = new dItem(event.getItem());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("item", item);

        List<String> events = new ArrayList<String>();
        events.add("player consumes item");
        events.add("player consumes " + item.identifySimple());
        events.add("player consumes " + item.identifyMaterial());

        String determination = doEvents(events, null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        else if (dItem.matches(determination)) {
            dItem newitem = dItem.valueOf(determination, dEntity.getPlayerFrom(event.getPlayer()), null);
            if (newitem != null)
                event.setItem(newitem.getItemStack());
        }
    }

    // <--[event]
    // @Events
    // player joins
    // player join
    //
    // @Triggers when a player joins the server.
    // @Context
    // <context.message> returns an Element of the join message.
    //
    // @Determine
    // Element(String) to change the join message.
    //
    // -->
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Player player = event.getPlayer();
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getJoinMessage()));

        String determination = doEvents(Arrays.asList
                ("player joins",
                        "player join"),
                null, dEntity.getPlayerFrom(player), context);

        // Handle message
        if (!determination.equals("none")) {
            event.setJoinMessage(determination);
        }

        // As a tie-in with ScoreboardHelper, make this player view
        // the scoreboard he/she is supposed to view
        if (ScoreboardHelper.viewerMap.containsKey(player.getName())) {
            Scoreboard score = ScoreboardHelper.getScoreboard(ScoreboardHelper.viewerMap.get(player.getName()));
            if (score != null)
                player.setScoreboard(score);
        }
    }

    // <--[event]
    // @Events
    // player kicked
    //
    // @Triggers when a player is kicked from the server.
    // @Context
    // <context.message> returns an Element of the kick message.
    //
    // @Determine
    // Element(String) to change the kick message.
    //
    // -->
    @EventHandler
    public void playerKick(PlayerKickEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getLeaveMessage()));

        String determination = doEvents(Arrays.asList
                ("player kicked"),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (!determination.equals("none")) {
            event.setLeaveMessage(determination);
        }
    }

    // <--[event]
    // @Events
    // player leashes entity
    // player leashes <entity>
    //
    // @Triggers when a player leashes an entity.
    // @Context
    // <context.entity> returns the dEntity of the leashed entity.
    // <context.holder> returns the dEntity that is holding the leash.
    //
    // @Determine
    // "CANCELLED" to cancel the leashing.
    //
    // -->
    @EventHandler
    public void playerLeashEntity(PlayerLeashEntityEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("entity", entity);
        context.put("holder", new dEntity(event.getLeashHolder()));

        String determination = doEvents(Arrays.asList
                ("player leashes entity",
                        "player leashes " + entity.identifyType()),
                null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.equalsIgnoreCase("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player levels up (from <level>/to <level>)
    //
    // @Triggers when a player levels up.
    // @Context
    // <context.level> returns an Element of the player's new level.
    //
    // -->
    @EventHandler
    public void playerLevelChange(PlayerLevelChangeEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("level", new Element(event.getNewLevel()));

        doEvents(Arrays.asList
                ("player levels up",
                        "player levels up to " + event.getNewLevel(),
                        "player levels up from " + event.getOldLevel()),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);
    }

    // <--[event]
    // @Events
    // player logs in (for the first time)
    // player (first) login
    //
    // @Triggers when a player logs in to the server.
    // @Context
    // <context.hostname> returns an Element of the player's hostname.
    //
    // @Determine
    // "KICKED" to kick the player from the server.
    // "KICKED Element(String)" to kick the player and specify a message to show.
    //
    // -->
    @EventHandler
    public void playerLogin(PlayerLoginEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        List<String> events = new ArrayList<String>();
        context.put("hostname", new Element(event.getHostname()));

        if (!dPlayer.isNoted(event.getPlayer())) {
            events.add("player logs in for the first time");
            events.add("player first login");
        }

        dPlayer.notePlayer(event.getPlayer());

        events.add("player logs in");
        events.add("player login");
        String determination = doEvents(events,
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("KICKED"))
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, determination.length() > 7 ? determination.substring(7): determination);
    }

    // <--[event]
    // @Events
    // player walks over notable
    // player walks over <location>
    //
    // @Triggers when a player walks over a notable location.
    // @Context
    // <context.notable> returns an Element of the notable location's name.
    //
    // @Determine
    // "CANCELLED" to stop the player from moving to the notable location.
    //
    // -->
    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        String name = NotableManager.getSavedId(new dLocation(event.getTo().getBlock().getLocation()));

        if (name != null) {
            Map<String, dObject> context = new HashMap<String, dObject>();
            context.put("notable", new Element(name));

            String determination = doEvents(Arrays.asList
                    ("player walks over notable",
                            "player walks over " + name,
                            "walked over notable",
                            "walked over " + name),
                    null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

            if (determination.toUpperCase().startsWith("CANCELLED") ||
                    determination.toUpperCase().startsWith("FROZEN"))
                event.setCancelled(true);
        }
    }

    // <--[event]
    // @Events
    // player picks up item
    // player picks up <item>
    // player takes item
    // player takes <item>
    //
    // @Triggers when a player picks up an item.
    // @Context
    // <context.item> returns the dItem.
    // <context.entity> returns a dEntity of the item.
    // <context.location> returns a dLocation of the item's location.
    //
    // @Determine
    // "CANCELLED" to stop the item from picked up.
    //
    // -->
    @EventHandler
    public void playerPickupItem(PlayerPickupItemEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(event.getItem().getItemStack());
        context.put("item", item);
        context.put("entity", new dEntity(event.getItem()));
        context.put("location", new dLocation(event.getItem().getLocation()));

        List<String> events = new ArrayList<String>();

        events.add("player picks up item");
        events.add("player picks up " + item.identifySimple());
        events.add("player picks up " + item.identifyMaterial());
        events.add("player takes item");
        events.add("player takes " + item.identifySimple());
        events.add("player takes " + item.identifyMaterial());

        String determination = doEvents(events, null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player quits
    // player quit
    //
    // @Triggers when a player quit the server.
    // @Context
    // <context.message> returns an Element of the quit message.
    //
    // @Determine
    // Element(String) to change the quit message.
    //
    // -->
    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getQuitMessage()));

        String determination = doEvents(Arrays.asList
                ("player quits",
                        "player quit"),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (!determination.equals("none")) {
            event.setQuitMessage(determination);
        }
    }

    // <--[event]
    // @Events
    // player respawns (at bed/elsewhere)
    //
    // @Triggers when a player respawns.
    // @Context
    // <context.location> returns a dLocation of the respawn location.
    //
    // @Determine
    // dLocation to change the respawn location.
    //
    // -->
    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getRespawnLocation()));

        List<String> events = new ArrayList<String>();
        events.add("player respawns");

        if (event.isBedSpawn()) events.add("player respawns at bed");
        else                    events.add("player respawns elsewhere");

        String determination = doEvents(events, null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (dLocation.matches(determination)) {
            dLocation location = dLocation.valueOf(determination);

            if (location != null) event.setRespawnLocation(location);
        }
    }

    // <--[event]
    // @Events
    // player shears entity
    // player shears <entity>
    // player shears <color> sheep
    //
    // @Triggers when a player shears an entity.
    // @Context
    // <context.state> returns the dEntity of the sheep.
    //
    // @Determine
    // "CANCELLED" to stop the player from shearing the entity.
    //
    // -->
    @EventHandler
    public void playerShearEntity(PlayerShearEntityEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("entity", entity);

        List<String> events = new ArrayList<String>();
        events.add("player shears entity");
        events.add("player shears " + entity.identifyType());

        if (entity.getEntityType().equals(EntityType.SHEEP)) {
            String color = ((Sheep) entity.getBukkitEntity()).getColor().name();
            events.add("player shears " + color + " sheep");
        }

        String determination = doEvents(events, null, dEntity.getPlayerFrom(event.getPlayer()), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player toggles flight
    // player starts flying
    // player stops flying
    //
    // @Triggers when a player starts or stops flying.
    // @Context
    // <context.state> returns an Element(Boolean) with a value of "true" if the player is now flying and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the player from toggling flying.
    //
    // -->
    @EventHandler
    public void playerToggleFlight(PlayerToggleFlightEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("state", new Element(event.isFlying()));

        String determination = doEvents(Arrays.asList
                ("player toggles flight",
                        "player " + (event.isFlying() ? "starts" : "stops") + " flying"),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player toggles sneak
    // player starts sneaking
    // player stops sneaking
    //
    // @Triggers when a player starts or stops sneaking.
    // @Context
    // <context.state> returns an Element(Boolean) with a value of "true" if the player is now sneaking and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the player from toggling sneaking.
    //
    // -->
    @EventHandler
    public void playerToggleSneak(PlayerToggleSneakEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("state", new Element(event.isSneaking()));

        String determination = doEvents(Arrays.asList
                ("player toggles sneak",
                        "player " + (event.isSneaking() ? "starts" : "stops") + " sneaking"),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player toggles sprint
    // player starts sprinting
    // player stops sprinting
    //
    // @Triggers when a player starts or stops sprinting.
    // @Context
    // <context.state> returns an Element(Boolean) with a value of "true" if the player is now sprinting and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the player from toggling sprinting.
    //
    // -->
    @EventHandler
    public void playerToggleSprint(PlayerToggleSprintEvent event) {

        if (dEntity.isNPC(event.getPlayer()))
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("state", new Element(event.isSprinting()));

        String determination = doEvents(Arrays.asList
                ("player toggles sprint",
                        "player " + (event.isSprinting() ? "starts" : "stops") + " sprinting"),
                null, dEntity.getPlayerFrom(event.getPlayer()), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }


    /////////////////////
    //   VEHICLE EVENTS
    /////////////////


    // <--[event]
    // @Events
    // vehicle created
    // <vehicle> created
    //
    // @Triggers when a vehicle is created.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    //
    // -->
    @EventHandler
    public void vehicleCreate(VehicleCreateEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        dEntity vehicle = new dEntity(event.getVehicle());

        context.put("vehicle", vehicle);

        doEvents(Arrays.asList
                ("vehicle created",
                        vehicle.identifyType() + " created",
                        vehicle.identifySimple() + " created"),
                null, null, context, true);
    }

    // <--[event]
    // @Events
    // vehicle damaged
    // <vehicle> damaged
    // entity damages vehicle
    // <entity> damages vehicle
    // entity damages <vehicle>
    // <entity> damages <vehicle>
    //
    // @Triggers when a vehicle is damaged.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the attacking entity.
    //
    // @Determine
    // "CANCELLED" to stop the entity from damaging the vehicle.
    // Element(Decimal) to set the value of the damage received by the vehicle.
    //
    // -->
    @EventHandler
    public void vehicleDamage(VehicleDamageEvent event) {

        dPlayer player = null;
        dNPC npc = null;

        dEntity vehicle = new dEntity(event.getVehicle());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("damage", new Element(event.getDamage()));
        context.put("vehicle", vehicle);

        List<String> events = new ArrayList<String>();
        events.add("vehicle damaged");
        events.add(vehicle.identifyType() + " damaged");
        events.add(vehicle.identifySimple() + " damaged");

        if (event.getAttacker() != null) {

            dEntity entity = new dEntity(event.getAttacker());
            context.put("entity", entity.getDenizenObject());

            if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
            else if (entity.isPlayer()) player = entity.getDenizenPlayer();

            events.add("entity damages vehicle");
            events.add("entity damages " + vehicle.identifyType());
            events.add("entity damages " + vehicle.identifySimple());
            events.add(entity.identifyType() + " damages vehicle");
            events.add(entity.identifyType() + " damages " + vehicle.identifyType());
            events.add(entity.identifyType() + " damages " + vehicle.identifySimple());
            events.add(entity.identifySimple() + " damages vehicle");
            events.add(entity.identifySimple() + " damages " + vehicle.identifyType());
            events.add(entity.identifySimple() + " damages " + vehicle.identifySimple());
        }

        String determination = doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Double)) {
            event.setDamage(aH.getDoubleFrom(determination));
        }
    }

    @EventHandler
    public void vehicleDestroy(VehicleDestroyEvent event) {

        // <--[event]
        // @Events
        // vehicle destroyed
        // <vehicle> destroyed
        // entity destroys vehicle
        // <entity> destroys vehicle
        // entity destroys <vehicle>
        // <entity> destroys <vehicle>
        //
        // @Triggers when a vehicle is destroyed.
        // @Context
        // <context.vehicle> returns the dEntity of the vehicle.
        // <context.entity> returns the dEntity of the attacking entity.
        //
        // @Determine
        // "CANCELLED" to stop the entity from destroying the vehicle.
        //
        // -->

        dPlayer player = null;
        dNPC npc = null;

        dEntity vehicle = new dEntity(event.getVehicle());
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("vehicle", vehicle);

        List<String> events = new ArrayList<String>();
        events.add("vehicle destroyed");
        events.add(vehicle.identifyType() + " destroyed");
        events.add(vehicle.identifySimple() + " destroyed");

        if (event.getAttacker() != null) {

            dEntity entity = new dEntity(event.getAttacker());
            context.put("entity", entity.getDenizenObject());

            if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
            else if (entity.isPlayer()) player = entity.getDenizenPlayer();

            events.add("entity destroys vehicle");
            events.add("entity destroys " + vehicle.identifyType());
            events.add("entity destroys " + vehicle.identifySimple());
            events.add(entity.identifyType() + " destroys vehicle");
            events.add(entity.identifyType() + " destroys " + vehicle.identifyType());
            events.add(entity.identifyType() + " destroys " + vehicle.identifySimple());
            events.add(entity.identifySimple() + " destroys vehicle");
            events.add(entity.identifySimple() + " destroys " + vehicle.identifyType());
            events.add(entity.identifySimple() + " destroys " + vehicle.identifySimple());
        }

        String determination = doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity enters vehicle
    // <entity> enters vehicle
    // entity enters <vehicle>
    // <entity> enters <vehicle>
    //
    // @Triggers when an entity enters a vehicle.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the entering entity.
    //
    // @Determine
    // "CANCELLED" to stop the entity from entering the vehicle.
    //
    // -->
    @EventHandler
    public void vehicleEnter(VehicleEnterEvent event) {

        dPlayer player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();

        dEntity vehicle = new dEntity(event.getVehicle());
        dEntity entity = new dEntity(event.getEntered());

        context.put("vehicle", vehicle);
        context.put("entity", entity.getDenizenObject());

        if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getDenizenPlayer();

        String determination = doEvents(Arrays.asList
                ("entity enters vehicle",
                        entity.identifyType() + " enters vehicle",
                        entity.identifySimple() + " enters vehicle",
                        "entity enters " + vehicle.identifyType(),
                        "entity enters " + vehicle.identifySimple(),
                        entity.identifyType() + " enters " + vehicle.identifyType(),
                        entity.identifySimple() + " enters " + vehicle.identifyType(),
                        entity.identifyType() + " enters " + vehicle.identifySimple(),
                        entity.identifySimple() + " enters " + vehicle.identifySimple()),
                npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity exits vehicle
    // entity exits <vehicle>
    // <entity> exits vehicle
    // <entity> exits <vehicle>
    //
    // @Triggers when an entity exits a vehicle.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the exiting entity.
    //
    // @Determine
    // "CANCELLED" to stop the entity from exiting the vehicle.
    //
    // -->
    @EventHandler
    public void vehicleExit(VehicleExitEvent event) {

        dPlayer player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();

        dEntity vehicle = new dEntity(event.getVehicle());
        dEntity entity = new dEntity(event.getExited());

        context.put("vehicle", vehicle);
        context.put("entity", entity.getDenizenObject());

        if (entity.isCitizensNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getDenizenPlayer();

        String determination = doEvents(Arrays.asList
                ("entity exits vehicle",
                        "entity exits " + vehicle.identifyType(),
                        "entity exits " + vehicle.identifySimple(),
                        entity.identifyType() + " exits vehicle",
                        entity.identifyType() + " exits " + vehicle.identifyType(),
                        entity.identifyType() + " exits " + vehicle.identifySimple(),
                        entity.identifySimple() + " exits " + vehicle.identifyType(),
                        entity.identifySimple() + " exits " + vehicle.identifySimple()),
                npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }


    /////////////////////
    //   WEATHER EVENTS
    /////////////////

    // <--[event]
    // @Events
    // lightning strikes (in <world>)
    //
    // @Triggers when lightning strikes in a world.
    // @Context
    // <context.world> returns the dWorld the lightning struck in.
    // <context.location> returns the dLocation where the lightning struck.
    //
    // @Determine
    // "CANCELLED" to stop the lightning from striking.
    //
    // -->
    @EventHandler
    public void lightningStrike(LightningStrikeEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());
        context.put("world", world);
        context.put("location", new dLocation(event.getLightning().getLocation()));

        String determination = doEvents(Arrays.asList
                ("lightning strikes",
                        "lightning strikes in " + world.identifySimple()),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // weather changes/rains/clears (in <world>)
    //
    // @Triggers when weather changes in a world.
    // @Context
    // <context.world> returns the dWorld the weather changed in.
    // <context.weather> returns an Element with the name of the new weather. (rain or clear).
    //
    // @Determine
    // "CANCELLED" to stop the weather from changing.
    //
    // -->
    @EventHandler
    public void weatherChange(WeatherChangeEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());
        context.put("world", world);

        List<String> events = new ArrayList<String>();
        events.add("weather changes");
        events.add("weather changes in " + world.identifySimple());

        if (event.toWeatherState()) {
            context.put("weather", new Element("rain"));
            events.add("weather rains");
            events.add("weather rains in " + world.identifySimple());
        }
        else {
            context.put("weather", new Element("clear"));
            events.add("weather clears");
            events.add("weather clears in " + world.identifySimple());
        }

        String determination = doEvents(events, null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }


    /////////////////////
    //   WORLD EVENTS
    /////////////////

    // <--[event]
    // @Events
    // portal created (in <world>) (because <reason>)
    //
    // @Triggers when a portal is created in a world.
    // @Context
    // <context.world> returns the dWorld the portal was created in.
    // <context.reason> returns an Element of the reason the portal was created. (FIRE or OBC_DESTINATION)
    // <context.list> returns a dList of all the blocks that will become portal blocks.
    //
    // @Determine
    // "CANCELLED" to stop the portal from being created.
    //
    // -->
    @EventHandler
    public void portalCreate(PortalCreateEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());
        String reason = event.getReason().name();

        context.put("world", world);
        context.put("reason", new Element(reason));

        dList list = new dList();
        for (Block block: event.getBlocks()) {
            list.add(new dLocation(block.getLocation()).identify());
        }
        context.put("blocks", list);

        String determination = doEvents(Arrays.asList
                ("portal created",
                        "portal created because " + reason,
                        "portal created in " + world.identifySimple(),
                        "portal created in " + world.identifySimple() + " because " + reason),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // spawn changes (in <world>)
    //
    // @Triggers when the world's spawn point changes.
    // @Context
    // <context.world> returns the dWorld that the spawn point changed in.
    // <context.old_location> returns the dLocation of the old spawn point.
    // <context.new_location> returns the dLocation of the new spawn point.
    //
    // -->
    @EventHandler
    public void spawnChange(SpawnChangeEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());

        context.put("world", world);
        context.put("old_location", new dLocation(event.getPreviousLocation()));
        context.put("new_location", new dLocation(world.getWorld().getSpawnLocation()));

        doEvents(Arrays.asList
                ("spawn changes",
                        "spawn changes in " + world.identifySimple()),
                null, null, context, true);
    }

    // <--[event]
    // @Events
    // structure grows (naturally/from bonemeal) (in <world>)
    // <structure> grows (naturally/from bonemeal) (in <world>)
    //
    // @Triggers when a structure (a tree or a mushroom) grows in a world.
    // @Context
    // <context.world> returns the dWorld the structure grew in.
    // <context.location> returns the dLocation the structure grew at.
    // <context.structure> returns an Element of the structure's type.
    // <context.blocks> returns a dList of all block locations to be modified.
    // <context.new_materials> returns a dList of the new block materials, to go with <context.blocks>.
    //
    // @Determine
    // "CANCELLED" to stop the structure from growing.
    //
    // -->
    @EventHandler
    public void structureGrow(StructureGrowEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());
        String treeType = event.getSpecies().name();

        context.put("world", world);
        context.put("location", new dLocation(event.getLocation()));
        context.put("structure", new Element(treeType));

        dList structure = new dList();
        dList mats = new dList();
        for (BlockState state: event.getBlocks()) {
            structure.add(new dLocation(state.getLocation()).identify());
            mats.add(dMaterial.getMaterialFrom(state.getType(), state.getRawData()).identify());
        }
        context.put("blocks", structure);
        context.put("new_materials", mats);

        List<String> events = new ArrayList<String>();
        events.add("structure grows");
        events.add("structure grows in " + world.identifySimple());
        events.add(treeType + " grows");
        events.add(treeType + " grows in " + world.identifySimple());

        if (event.isFromBonemeal()) {
            events.add("structure grows from bonemeal");
            events.add("structure grows from bonemeal in " + world.identifySimple());
            events.add(treeType + " grows from bonemeal");
            events.add(treeType + " grows from bonemeal in " + world.identifySimple());
        }
        else {
            events.add("structure grows naturally");
            events.add("structure grows naturally in " + world.identifySimple());
            events.add(treeType + " grows naturally");
            events.add(treeType + " grows naturally in " + world.identifySimple());
        }

        String determination = doEvents(events, null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // world initializes
    // <world> initializes
    //
    // @Triggers when a world is initialized.
    // @Context
    // <context.world> returns the dWorld that was initialized.
    //
    // -->
    @EventHandler
    public void worldInit(WorldInitEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());

        context.put("world", world);

        doEvents(Arrays.asList
                ("world initializes",
                        world.identifySimple() + " initializes"),
                null, null, context, true);
    }

    // <--[event]
    // @Events
    // world loads
    // <world> loads
    //
    // @Triggers when a world is loaded.
    // @Context
    // <context.world> returns the dWorld that was loaded.
    //
    // -->
    @EventHandler
    public void worldLoad(WorldLoadEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());

        context.put("world", world);

        doEvents(Arrays.asList
                ("world loads",
                        world.identifySimple() + " loads"),
                null, null, context, true);
    }

    // <--[event]
    // @Events
    // world saves
    // <world> saves
    //
    // @Triggers when a world is saved.
    // @Context
    // <context.world> returns the dWorld that was saved.
    //
    // -->
    @EventHandler
    public void worldSave(WorldSaveEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());

        context.put("world", world);

        doEvents(Arrays.asList
                ("world saves",
                        world.identifySimple() + " saves"),
                null, null, context, true);
    }

    // <--[event]
    // @Events
    // world unloads
    // <world> unloads
    //
    // @Triggers when a world is unloaded.
    // @Context
    // <context.world> returns the dWorld that was unloaded.
    //
    // -->
    @EventHandler
    public void worldUnload(WorldUnloadEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld world = new dWorld(event.getWorld());

        context.put("world", world);

        doEvents(Arrays.asList
                ("world unloads",
                        world.identifySimple() + " unloads"),
                null, null, context, true);
    }
}

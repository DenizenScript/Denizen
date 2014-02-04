package net.aufdemrand.denizen.scripts.containers.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.aH.Argument;
import net.aufdemrand.denizen.objects.aH.PrimitiveType;
import net.aufdemrand.denizen.tags.core.EscapeTags;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.ScoreboardHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.Position;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.*;
import org.bukkit.event.player.*;
import org.bukkit.event.hanging.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.world.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

@SuppressWarnings("deprecation")
public class WorldScriptHelper implements Listener {

    public WorldScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }


    //////////////////
    // MAPS
    ///////////


    // Store the UUIDs of dying entities along with the dEntities of their killers,
    // to bring <context.damager> from "on entity killed" to "on entity dies"
    public static Map<UUID, dEntity> entityKillers = new HashMap<UUID, dEntity>();


    /////////////////////
    //   BLOCK EVENTS
    /////////////////


    // <--[event]
    // @Events
    // player breaks block
    // player breaks block in <notable cuboid>
    // player breaks <material>
    // player breaks <material> in <notable cuboid>
    // player breaks block with <item>
    // player breaks <material> with <item>
    // player breaks <material> with <item> in <notable cuboid>
    // player breaks block with <material>
    // player breaks <material> with <material>
    // player breaks <material> with <material> in <notable cuboid>
    //
    // @Triggers when a player breaks a block.
    // @Context
    // <context.location> returns the dLocation the block was broken at.
    // <context.material> returns the dMaterial of the block that was broken.
    // <context.cuboids> returns a dList of notable cuboids surrounding the block broken.
    //
    // @Determine
    // "CANCELLED" to stop the block from breaking.
    // "NOTHING" to make the block drop no items.
    // dList(dItem) to make the block drop a specified list of items.
    //
    // -->
    @EventHandler
    public void blockBreak(BlockBreakEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        Block block = event.getBlock();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        dItem item = new dItem(event.getPlayer().getItemInHand());

        // Add events
        List<String> events = new ArrayList<String>();
        events.add("player breaks block");
        events.add("player breaks " + material.identifySimple());
        events.add("player breaks block with " + item.identifySimple());
        events.add("player breaks " + material.identifySimple() +
                " with " + item.identifySimple());
        events.add("player breaks block with " + item.identifyMaterial());
        events.add("player breaks " + material.identifySimple() +
                " with " + item.identifyMaterial());

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getBlock().getLocation());

        if (cuboids.size() > 0) {
            dList cuboid_context = new dList();
            for (dCuboid cuboid : cuboids) {
                events.add("player breaks block in " + cuboid.identifySimple());
                events.add("player breaks " + material.identifySimple() + " in " + cuboid.identifySimple());
                events.add("player breaks " + material.identifySimple() + " with " + item.identifySimple() + " in " + cuboid.identifySimple());
                events.add("player breaks " + material.identifySimple() + " with " + item.identifyMaterial() + " in " + cuboid.identifySimple());
                events.add("player breaks block in cuboid");
                events.add("player breaks " + material.identifySimple() + " in cuboid");
                events.add("player breaks " + material.identifySimple() + " with " + item.identifySimple() + " in cuboid");
                events.add("player breaks " + material.identifySimple() + " with " + item.identifyMaterial() + " in cuboid");
                cuboid_context.add(cuboid.identifySimple());
            }
            // Add in cuboids context, if inside a cuboid
            context.put("cuboids", cuboid_context);
        }

        // Trim events not used
        events = EventManager.trimEvents(events);

        // Don't continue if there are no events to run
        if (events.size() == 0) return;

        // Add in more context
        context.put("location", new dLocation(block.getLocation()));
        context.put("material", material);

        // Do events, get the determination
        String determination = EventManager.doEvents(events, null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            // Straight up cancel the event
            event.setCancelled(true);

        } else if (determination.toUpperCase().startsWith("NOTHING")) {
            // Make nothing drop, usually used as "drop:nothing"
            event.setCancelled(true);
            block.setType(Material.AIR);

        } else if (Argument.valueOf(determination).matchesArgumentList(dItem.class)) {
            // Get a dList of dItems to drop
            event.setCancelled(true);
            block.setType(Material.AIR);

            // Get the list of items
            Object list = dList.valueOf(determination).filter(dItem.class);

            @SuppressWarnings("unchecked")
            List<dItem> newItems = (List<dItem>) list;

            for (dItem newItem : newItems) {
                block.getWorld().dropItemNaturally(block.getLocation(),
                        newItem.getItemStack()); // Drop each item
            }
        }
    }


    // <--[event]
    // @Events
    // block burns
    // <block> burns
    //
    // @Triggers when a block is destroyed by fire.
    // @Context
    // <context.location> returns the dLocation the block was burned at.
    // <context.material> returns the dMaterial of the block that was burned.
    //
    // @Determine
    // "CANCELLED" to stop the block from being destroyed.
    //
    // -->
    @EventHandler
    public void blockBurn(BlockBurnEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        context.put("location", new dLocation(event.getBlock().getLocation()));
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        String determination = EventManager.doEvents(Arrays.asList
                ("block burns",
                        material.identifySimple() + " burns"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // block being built
    // block being built on <material>
    // <material> being built
    // <material> being built on <material>
    //
    // @Triggers when an attempt is made to build a block on another block. Not necessarily caused by players.
    // @Context
    // <context.location> returns the dLocation of the block the player is trying to build on.
    // <context.old_material> returns the dMaterial of the block the player is trying to build on.
    // <context.new_material> returns the dMaterial of the block the player is trying to build.
    //
    // @Determine
    // "BUILDABLE" to allow the building.
    // "CANCELLED" to cancel the building.
    //
    // -->
    @EventHandler
    public void blockCanBuild(BlockCanBuildEvent event) {

        // TODO: Remove when Bukkit fixes error?
        if (event.getMaterial() == null)
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial oldMaterial = dMaterial.getMaterialFrom(event.getBlock().getType());
        dMaterial newMaterial = dMaterial.getMaterialFrom(event.getMaterial());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("old_material", oldMaterial);
        context.put("new_material", newMaterial);

        String determination = EventManager.doEvents(Arrays.asList
                ("block being built",
                        "block being built on " + oldMaterial.identifySimple(),
                        newMaterial.identifySimple() + " being built",
                        newMaterial.identifySimple() + " being built on " +
                                oldMaterial.identifySimple()),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("BUILDABLE"))
            event.setBuildable(true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setBuildable(false);
    }

    // <--[event]
    // @Events
    // player damages block
    // player damages <material>
    // player damages block in <notable cuboid>
    // player damages <material> in <notable cuboid>
    //
    // @Triggers when a block is damaged by a player.
    // @Context
    // <context.location> returns the dLocation the block that was damaged.
    // <context.material> returns the dMaterial of the block that was damaged.
    // <context.cuboids> returns a dList of notable cuboids which the damaged block is contained.
    //
    // @Determine
    // "CANCELLED" to stop the block from being damaged.
    // "INSTABREAK" to make the block get broken instantly.
    //
    // -->
    @EventHandler
    public void blockDamage(BlockDamageEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        List<String> events = new ArrayList<String>();

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getBlock().getLocation());

        if (cuboids.size() > 0) {
            dList cuboid_context = new dList();
            for (dCuboid cuboid : cuboids) {
                events.add("player damages block in " + cuboid.identifySimple());
                events.add("player damages " + material.identifySimple() + " in " + cuboid.identifySimple());
                cuboid_context.add(cuboid.identifySimple());
            }
            // Add in cuboids context, if inside a cuboid
            context.put("cuboids", cuboid_context);
        }

        events.add("player damages block");
        events.add("player damages " + material.identifySimple());

        // Trim events not used
        events = EventManager.trimEvents(events);

        // Return if no events left to parse
        if (events.size() == 0) return;

        // Add in add'l context
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(events,
                null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        if (determination.toUpperCase().startsWith("INSTABREAK"))
            event.setInstaBreak(true);
    }

    // <--[event]
    // @Events
    // block dispenses item
    // block dispenses <item>
    // <block> dispenses item
    // <block> dispenses <item>
    //
    // @Triggers when a block dispenses an item.
    // @Context
    // <context.location> returns the dLocation of the dispenser.
    // <context.item> returns the dItem of the item being dispensed.
    //
    // @Determine
    // "CANCELLED" to stop the block from dispensing.
    // Element(Double) to set the power with which the item is shot.
    //
    // -->
    @EventHandler
    public void blockDispense(BlockDispenseEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(event.getItem());
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        dLocation location = new dLocation(event.getBlock().getLocation());

        context.put("location", location);
        context.put("item", item);

        String determination = EventManager.doEvents(Arrays.asList
                ("block dispenses item",
                        "block dispenses " + item.identifySimple(),
                        material.identifySimple() + " dispenses item",
                        material.identifySimple() + " dispenses " + item.identifySimple()),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Double)) {
            event.setVelocity(event.getVelocity().normalize()
                    .multiply(aH.getDoubleFrom(determination)));
        }
    }

    // <--[event]
    // @Events
    // block fades
    // <block> fades
    //
    // @Triggers when a block fades, melts or disappears based on world conditions.
    // @Context
    // <context.location> returns the dLocation the block faded at.
    // <context.material> returns the dMaterial of the block that faded.
    //
    // @Determine
    // "CANCELLED" to stop the block from fading.
    //
    // -->
    @EventHandler
    public void blockFade(BlockFadeEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("block fades",
                        material.identifySimple() + " fades"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // block forms
    // <block> forms
    //
    // @Triggers when a block is formed based on world conditions,
    //           e.g. when snow forms in a snow storm or ice forms
    //           in a snowy biome
    // @Context
    // <context.location> returns the dLocation the block.
    // <context.material> returns the dMaterial of the block.
    //
    // @Determine
    // "CANCELLED" to stop the block from forming.
    //
    // -->
    @EventHandler
    public void blockForm(BlockFormEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("block forms",
                        material.identifySimple() + " forms"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // liquid spreads
    // <liquid block> spreads
    //
    // @Triggers when a liquid block spreads.
    // @Context
    // <context.destination> returns the dLocation the block spread to.
    // <context.location> returns the dLocation the block spread from.
    // <context.material> returns the dMaterial of the block that spread.
    //
    // @Determine
    // "CANCELLED" to stop the block from spreading.
    //
    // -->
    @EventHandler
    public void blockFromTo(BlockFromToEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("destination", new dLocation(event.getToBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("liquid spreads",
                        material.identifySimple() + " spreads"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // block grows
    // <block> grows
    //
    // @Triggers when a block grows naturally in the world,
    //           e.g. when wheat, sugar canes, cactuses,
    //           watermelons or pumpkins grow
    // @Context
    // <context.location> returns the dLocation the block.
    // <context.material> returns the dMaterial of the block.
    //
    // @Determine
    // "CANCELLED" to stop the block from growing.
    //
    // -->
    @EventHandler
    public void blockGrow(BlockGrowEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("block grows",
                        material.identifySimple() + " grows"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // block ignites
    // <block> ignites
    //
    // @Triggers when a block is set on fire.
    // @Context
    // <context.location> returns the dLocation the block was set on fire at.
    // <context.material> returns the dMaterial of the block that was set on fire.
    //
    // @Determine
    // "CANCELLED" to stop the block from being ignited.
    //
    // -->
    @EventHandler
    public void blockIgnite(BlockIgniteEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("block ignites",
                        material.identifySimple() + " ignites"),
                null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // piston extends
    // <block> extends
    //
    // @Triggers when a piston extends.
    // @Context
    // <context.location> returns the dLocation of the piston.
    // <context.material> returns the dMaterial of the piston.
    // <context.length> returns the number of blocks that will be moved by the piston.
    //
    // @Determine
    // "CANCELLED" to stop the piston from extending.
    //
    // -->
    @EventHandler
    public void blockPistonExtend(BlockPistonExtendEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);
        context.put("length", new Element(event.getLength()));

        String determination = EventManager.doEvents(Arrays.asList
                ("piston extends",
                        material.identifySimple() + " extends"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // piston retracts
    // <block> retracts
    //
    // @Triggers when a piston retracts.
    // @Context
    // <context.location> returns the dLocation of the piston.
    // <context.retract_location> returns the new dLocation of the block that
    //                            will be moved by the piston if it is sticky.
    // <context.material> returns the dMaterial of the piston.
    //
    // @Determine
    // "CANCELLED" to stop the piston from retracting.
    //
    // -->
    @EventHandler
    public void blockPistonRetract(BlockPistonRetractEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("retract_location", new dLocation(event.getRetractLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("piston retracts",
                        material.identifySimple() + " retracts"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player places block
    // player places <material>
    // player place <item>
    // player places block in <notable cuboid>
    // player places <material> in <notable cuboid>
    // player places <item> in <notable cuboid>
    //
    // @Triggers when a player places a block.
    // @Context
    // <context.location> returns the dLocation of the block that was placed.
    // <context.material> returns the dMaterial of the block that was placed.
    // <context.item_in_hand> returns the dItem of the item in hand.
    //
    // @Determine
    // "CANCELLED" to stop the block from being placed.
    //
    // -->
    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        dItem item = new dItem(event.getItemInHand());
        List<String> events = new ArrayList<String>();

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getBlock().getLocation());

        if (cuboids.size() > 0) {
            dList cuboid_context = new dList();
            for (dCuboid cuboid : cuboids) {
                events.add("player places block in " + cuboid.identifySimple());
                events.add("player places " + material.identifySimple() + " in " + cuboid.identifySimple());
                events.add("player places " + item.identifySimple() + " in " + cuboid.identifySimple());
                cuboid_context.add(cuboid.identifySimple());
            }
            // Add in cuboids context, if inside a cuboid
            context.put("cuboids", cuboid_context);
        }

        events.add("player places block");
        events.add("player places " + material.identifySimple());
        events.add("player places " + item.identifySimple());

        // Trim events not used
        events = EventManager.trimEvents(events);

        // Return if no events left to parse
        if (events.size() == 0) return;

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);
        context.put("item_in_hand", item);

        String determination = EventManager.doEvents(events,
                null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // block spreads
    // <block> spreads
    //
    // @Triggers when a block spreads based on world conditions,
    //           e.g. when fire spreads, when mushrooms spread
    // @Context
    // <context.location> returns the dLocation the block.
    // <context.material> returns the dMaterial of the block.
    //
    // @Determine
    // "CANCELLED" to stop the block from growing.
    //
    // -->
    @EventHandler
    public void blockSpread(BlockSpreadEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("block spreads",
                        material.identifySimple() + " spreads"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // brewing stand brews
    //
    // @Triggers when a brewing stand brews a potion.
    // @Context
    // <context.location> returns the dLocation of the brewing stand.
    // <context.inventory> returns the dInventory of the brewing stand's contents.
    //
    // @Determine
    // "CANCELLED" to stop the brewing stand from brewing.
    //
    // -->
    @EventHandler
    public void brew(BrewEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("inventory", dInventory.mirrorBukkitInventory(event.getContents()));

        String determination = EventManager.doEvents(Arrays.asList
                ("brewing stand brews"),
                null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity forms block
    // entity forms <block>
    // <entity> forms block
    // <entity> forms <block>
    //
    // @Triggers when a block is formed by an entity,
    //           e.g. when a snowman forms snow
    // @Context
    // <context.location> returns the dLocation the block.
    // <context.material> returns the dMaterial of the block.
    // <context.entity> returns the dEntity that formed the block.
    //
    // @Determine
    // "CANCELLED" to stop the block from forming.
    //
    // -->
    @EventHandler
    public void entityBlockForm(EntityBlockFormEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        dEntity entity = new dEntity(event.getEntity());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);
        context.put("entity", entity.getDenizenObject());

        String determination = EventManager.doEvents(Arrays.asList
                ("entity forms block",
                        "entity forms " + material.identifySimple(),
                        entity.identifyType() + " forms block",
                        entity.identifyType() + " forms " + material.identifySimple()),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // furnace burns item
    // furnace burns <item>
    //
    // @Triggers when a furnace burns an item used as fuel.
    // @Context
    // <context.location> returns the dLocation of the furnace.
    // <context.item> returns the dItem burnt.
    //
    // @Determine
    // "CANCELLED" to stop the furnace from burning the item.
    // Element(Integer) to set the burn time for this fuel.
    //
    // -->
    @EventHandler
    public void furnaceBurn(FurnaceBurnEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(event.getFuel());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("item", item);

        String determination = EventManager.doEvents(Arrays.asList
                ("furnace burns item",
                        "furnace burns " + item.identifySimple()),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Integer)) {
            event.setBurnTime(aH.getIntegerFrom(determination));
        }
    }

    // <--[event]
    // @Events
    // player takes item from furnace
    // player takes <item> from furnace
    // player takes <material> from furnace
    //
    // @Triggers when a player takes an item from a furnace.
    // @Context
    // <context.location> returns the dLocation of the furnace.
    // <context.item> returns the dItem taken out of the furnace.
    //
    // @Determine
    // Element(Integer) to set the amount of experience the player will get.
    //
    // -->
    @EventHandler
    public void furnaceExtract(FurnaceExtractEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial itemMaterial = dMaterial.getMaterialFrom(event.getItemType());
        dItem item = new dItem(itemMaterial, event.getItemAmount());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("item", item);

        String determination = EventManager.doEvents(Arrays.asList
                ("player takes item from furnace",
                        "player takes " + item.identifySimple() + " from furnace",
                        "player takes " + item.identifyMaterial() + " from furnace"),
                null, event.getPlayer(), context, true);

        if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Integer)) {
            event.setExpToDrop(aH.getIntegerFrom(determination));
        }
    }

    // <--[event]
    // @Events
    // furnace smelts item (into <item>)
    // furnace smelts <item> (into <item>)
    //
    // @Triggers when a furnace smelts an item.
    // @Context
    // <context.location> returns the dLocation of the furnace.
    // <context.source_item> returns the dItem that is being smelted.
    // <context.result_item> returns the dItem that is the result of the smelting.
    //
    // @Determine
    // "CANCELLED" to stop the furnace from smelting the item.
    // dItem to set the item that is the result of the smelting.
    //
    // -->
    @EventHandler
    public void furnaceSmelt(FurnaceSmeltEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem source = new dItem(event.getSource());
        dItem result = new dItem(event.getResult());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("source_item", source);
        context.put("result_item", result);

        String determination = EventManager.doEvents(Arrays.asList
                ("furnace smelts item",
                        "furnace smelts " + source.identifySimple(),
                        "furnace smelts item into " + result.identifySimple(),
                        "furnace smelts " + source.identifySimple() + " into " + result.identifySimple()),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (dItem.matches(determination)) {
            event.setResult(dItem.valueOf(determination).getItemStack());
        }
    }

    // <--[event]
    // @Events
    // leaves decay
    // <block> decay
    //
    // @Triggers when leaves decay.
    // @Context
    // <context.location> returns the dLocation of the leaves.
    // <context.material> returns the dMaterial of the leaves.
    //
    // @Determine
    // "CANCELLED" to stop the leaves from decaying.
    //
    // -->
    @EventHandler
    public void leavesDecay(LeavesDecayEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("leaves decay",
                        material.identifySimple() + " decay"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player changes sign
    // player changes (<material>)
    //
    // @Triggers when a player changes a sign.
    // @Context
    // <context.location> returns the dLocation of the sign.
    // <context.new> returns the new sign text as a dList.
    // <context.old> returns the old sign text as a dList.
    // <context.new_escaped> returns the new sign text as a dList, pre-escaped to prevent issues.
    // <context.old_escaped> returns the old sign text as a dList, pre-escaped to prevent issues.
    // <context.material> returns the dMaterial of the sign.
    //
    // @Determine
    // "CANCELLED" to stop the sign from being changed.
    // dList to change the lines (Uses escaping, see <@link language Property Escaping>)
    //
    // -->
    @EventHandler
    public void signChange(final SignChangeEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block == null || !(block.getState() instanceof Sign)) {
            return; // Fix error induced by dark magic.
        }
        Sign sign = (Sign) block.getState();
        dMaterial material = dMaterial.getMaterialFrom(block.getType(), block.getData());

        context.put("old", new dList(Arrays.asList(sign.getLines())));
        context.put("new", new dList(Arrays.asList(event.getLines())));

        dList old_escaped = new dList();
        for (String str: sign.getLines()) {
            old_escaped.add(EscapeTags.Escape(str));
        }
        context.put("old_escaped", old_escaped);

        dList new_escaped = new dList();
        for (String str: event.getLines()) {
            new_escaped.add(EscapeTags.Escape(str));
        }
        context.put("new_escaped", new_escaped);

        context.put("location", new dLocation(block.getLocation()));
        context.put("material", material);

        String determination = EventManager.doEvents(Arrays.asList
                ("player changes sign",
                        "player changes " + material.identifySimple()),
                null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        else if (determination.length() > 0 && !determination.equalsIgnoreCase("none")) {
            dList lines = new dList(determination);
            for (int i = 0; i < 4 && i < lines.size(); i++) {
                event.setLine(i, EscapeTags.unEscape(lines.get(i)));
            }
        }
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
    // @Determine "CANCELLED" to save all plugins and cancel server startup.
    //
    // -->
    public void serverStartEvent() {
        // Start the 'timeEvent'
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        timeEvent();
                    }
                }, Settings.WorldScriptTimeEventFrequency().getTicks(), Settings.WorldScriptTimeEventFrequency().getTicks());

        // Fire the 'Server Start' event
        String determination = EventManager.doEvents(Arrays.asList("server start"),
                null, null, null);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            Bukkit.getServer().shutdown();
    }

    private final Map<String, Integer> current_time = new HashMap<String, Integer>();

    // <--[event]
    // @Events
    // time changes in <world>
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

                EventManager.doEvents(Arrays.asList
                        ("time changes in " + currentWorld.identifySimple(),
                                String.valueOf(hour) + ":00 in " + currentWorld.identifySimple(),
                                "time " + String.valueOf(hour) + " in " + currentWorld.identifySimple()),
                        null, null, context, true);

                current_time.put(currentWorld.identifySimple(), hour);
            }
        }
    }



    /////////////////////
    //   HANGING EVENTS
    /////////////////

    // <--[event]
    // @Events
    // hanging breaks
    // hanging breaks because <cause>
    // <hanging> breaks
    // <hanging> breaks because <cause>
    //
    // @Triggers when a hanging entity (painting or itemframe) is broken.
    // @Context
    // <context.cause> returns the cause of the entity breaking.
    // <context.entity> returns the dEntity that broke the hanging entity, if any.
    // <context.hanging> returns the dEntity of the hanging.
    //
    // @Determine
    // "CANCELLED" to stop the hanging from being broken.
    //
    // -->
    @EventHandler
    public void hangingBreak(HangingBreakEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity hanging = new dEntity(event.getEntity());
        String cause =  event.getCause().name();

        List<String> events = new ArrayList<String>();
        events.add("hanging breaks");
        events.add("hanging breaks because " + cause);
        events.add(hanging.identifyType() + " breaks");
        events.add(hanging.identifyType() +
                " breaks because " + cause);

        if (event instanceof HangingBreakByEntityEvent) {

            // <--[event]
            // @Events
            // <entity> breaks hanging
            // <entity> breaks hanging because <cause>
            // <entity> breaks <hanging> in <notable cuboid>
            // <entity> breaks <hanging> because
            // <entity> breaks <hanging> because <cause>
            //
            // @Triggers when a hanging entity is broken by an entity.
            // @Context
            // <context.cause> returns the cause of the entity breaking.
            // <context.entity> returns the dEntity that broke the hanging entity.
            // <context.hanging> returns the hanging entity as a dEntity.
            //
            // @Determine
            // "CANCELLED" to stop the hanging entity from being broken.
            //
            // -->

            HangingBreakByEntityEvent subEvent = (HangingBreakByEntityEvent) event;

            dEntity entity = new dEntity(subEvent.getRemover());
            context.put("entity", entity.getDenizenObject());

            if (entity.isNPC()) npc = entity.getDenizenNPC();
            else if (entity.isPlayer()) player = entity.getPlayer();

            // Look for cuboids that contain the block's location
            List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getEntity().getLocation());

            if (cuboids.size() > 0) {
                dList cuboid_context = new dList();
                for (dCuboid cuboid : cuboids) {
                    events.add(entity.identifyType() + " breaks " + hanging.identifyType() + " in " + cuboid.identifySimple());

                    cuboid_context.add(cuboid.identifySimple());
                }
                // Add in cuboids context, if inside a cuboid
                context.put("cuboids", cuboid_context);
            }

            events.add("entity breaks hanging");
            events.add("entity breaks hanging because " + cause);
            events.add("entity breaks " + hanging.identifyType());
            events.add("entity breaks " + hanging.identifyType() + " because " + cause);
            events.add(entity.identifyType() + " breaks hanging");
            events.add(entity.identifyType() + " breaks hanging because " + cause);
            events.add(entity.identifyType() + " breaks " + hanging.identifyType());
            events.add(entity.identifyType() + " breaks " + hanging.identifyType() + " because " + cause);
        }

        // Trim events not used
        events = EventManager.trimEvents(events);

        // Return if no events left to parse
        if (events.size() == 0) return;

        // Add context
        context.put("hanging", hanging);
        context.put("cause", new Element(cause));

        String determination = EventManager.doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player places hanging
    // player places <hanging>
    //
    // @Triggers when a hanging entity (painting or itemframe) is placed.
    // @Context
    // <context.hanging> returns the dEntity of the hanging.
    // <context.location> returns the dLocation of the block the hanging was placed on.
    //
    // @Determine
    // "CANCELLED" to stop the hanging from being placed.
    //
    // -->
    @EventHandler
    public void hangingPlace(HangingPlaceEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity hanging = new dEntity(event.getEntity());

        context.put("hanging", hanging);
        context.put("location", new dLocation(event.getBlock().getLocation()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player places hanging",
                        "player places " + hanging.identifyType()),
                null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }



    /////////////////////
    //   ENTITY EVENTS
    /////////////////


    // <--[event]
    // @Events
    // creeper powered (because <cause>)
    //
    // @Triggers when a creeper is struck by lightning and turned into a powered creeper.
    // @Context
    // <context.entity> returns the dEntity of the creeper.
    // <context.lightning> returns the dEntity of the lightning.
    // <context.cause> returns an Element of the cause for the creeper being powered.
    //
    // @Determine
    // "CANCELLED" to stop the creeper from being powered.
    //
    // -->
    @EventHandler
    public void creeperPower(CreeperPowerEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        dEntity lightning = new dEntity(event.getLightning());
        String cause = event.getCause().name();

        context.put("entity", entity);
        context.put("lightning", lightning);
        context.put("cause", new Element(cause));

        String determination = EventManager.doEvents(Arrays.asList
                ("creeper powered",
                        "creeper powered because " + cause),
                null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity changes block
    // <entity> changes block
    // <entity> changes block
    //
    // @Triggers when an entity changes the material of a block.
    // @Context
    // <context.entity> returns the dEntity that changed the block.
    // <context.location> returns the dLocation of the changed block.
    // <context.old_material> returns the old material of the block.
    // <context.new_material> returns the new material of the block.
    //
    // @Determine
    // "CANCELLED" to stop the entity from changing the block.
    //
    // -->
    @EventHandler
    public void entityChangeBlock(EntityChangeBlockEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        dMaterial oldMaterial = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        dMaterial newMaterial = dMaterial.getMaterialFrom(event.getTo()); // Not able to get DATA here?

        context.put("entity", entity.getDenizenObject());
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("old_material", oldMaterial);
        context.put("new_material", newMaterial);

        String determination = EventManager.doEvents(Arrays.asList
                ("entity changes block",
                        "entity changes " + oldMaterial.identifySimple(),
                        "entity changes block into " + newMaterial.identifySimple(),
                        "entity changes " + oldMaterial.identifySimple() +
                                " into " + newMaterial.identifySimple(),
                        entity.identifyType() + " changes block",
                        entity.identifyType() + " changes " + oldMaterial.identifySimple(),
                        entity.identifyType() + " changes block into " + newMaterial.identifySimple(),
                        entity.identifyType() + " changes " + oldMaterial.identifySimple() +
                                " into " + newMaterial.identifySimple()),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity combusts
    // <entity> combusts
    //
    // @Triggers when an entity combusts.
    // @Context
    // <context.duration> returns how long the entity takes to combust.
    // <context.entity> returns the dEntity that combusted.
    //
    // @Determine
    // "CANCELLED" to stop the entity from combusting.
    //
    // -->
    @EventHandler
    public void entityCombust(EntityCombustEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        Entity entity = event.getEntity();

        context.put("entity", new dEntity(entity).getDenizenObject());
        context.put("duration", new Duration((long) event.getDuration()));

        String determination = EventManager.doEvents(Arrays.asList
                ("entity combusts",
                        entity.getType().name() + " combusts"),
                null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity damaged
    // entity damaged by <cause>
    // <entity> damaged
    // <entity> damaged by <cause>
    //
    // @Triggers when an entity is damaged.
    // @Context
    // <context.cause> returns the reason the entity was damaged.
    // <context.damage> returns the amount of damage dealt.
    // <context.entity> returns the dEntity that was damaged.
    //
    // @Determine
    // "CANCELLED" to stop the entity from being damaged.
    // Element(Double) to set the amount of damage the entity receives.
    //
    // -->
    @EventHandler
    public void entityDamage(EntityDamageEvent event) {

        Player player = null;
        dNPC npc = null;
        String determination;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        String cause = event.getCause().name();

        context.put("entity", entity.getDenizenObject());
        context.put("damage", new Element(event.getDamage()));
        context.put("cause", new Element(event.getCause().name()));

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        boolean isFatal = false;

        if (entity.isValid() && entity.isLivingEntity()) {
            if (event.getDamage() >= entity.getLivingEntity().getHealth()) {
                isFatal = true;
            }
        }

        List<String> events = new ArrayList<String>();
        events.add("entity damaged");
        events.add("entity damaged by " + cause);
        events.add(entity.identifyType() + " damaged");
        events.add(entity.identifyType() + " damaged by " + cause);

        if (isFatal) {

            // <--[event]
            // @Events
            // entity killed
            // entity killed by <cause>
            // <entity> killed
            // <entity> killed by <cause>
            //
            // @Triggers when an entity is killed.
            // @Context
            // <context.cause> returns the reason the entity was killed.
            // <context.entity> returns the dEntity that was killed.
            // <context.damage> returns the amount of damage dealt.
            // <context.damager> returns the dEntity damaging the other entity.
            // <context.projectile> returns the projectile shot by the damager, if any.
            //
            // @Determine
            // "CANCELLED" to stop the entity from being killed.
            // Element(Double) to set the amount of damage the entity receives, instead of dying.
            //
            // -->

            events.add("entity killed");
            events.add("entity killed by " + cause);
            events.add(entity.identifyType() + " killed");
            events.add(entity.identifyType() + " killed by " + cause);
        }

        if (event instanceof EntityDamageByEntityEvent) {

            // <--[event]
            // @Events
            // entity damages entity
            // entity damages <entity>
            // entity damaged by entity
            // entity damaged by <entity>
            // <entity> damages entity
            // <entity> damaged by entity
            // <entity> damaged by <entity>
            // <entity> damages <entity>
            //
            // @Triggers when an entity damages another entity.
            // @Context
            // <context.cause> returns the reason the entity was damaged.
            // <context.entity> returns the dEntity that was damaged.
            // <context.damage> returns the amount of damage dealt.
            // <context.damager> returns the dEntity damaging the other entity.
            // <context.projectile> returns the projectile, if one caused the event.
            //
            // @Determine
            // "CANCELLED" to stop the entity from being damaged.
            // Element(Double) to set the amount of damage the entity receives.
            //
            // -->

            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;

            // Have a different set of player and NPC contexts for events
            // like "player damages player" from the one we have for
            // "player damaged by player"

            Player subPlayer = null;
            dNPC subNPC = null;
            dEntity projectile = null;
            dEntity damager = new dEntity(subEvent.getDamager());

            if (damager.isProjectile()) {
                projectile = damager;
                context.put("projectile", projectile);

                if (damager.hasShooter()) {
                    damager = damager.getShooter();
                }

                if (!damager.getEntityType().equals(projectile.getEntityType())) {
                    events.add("entity damaged by " + projectile.identifyType());
                    events.add(entity.identifyType() + " damaged by " + projectile.identifyType());
                }
            }

            context.put("damager", damager.getDenizenObject());

            events.add("entity damaged by entity");
            events.add("entity damaged by " + damager.identifyType());
            events.add(entity.identifyType() + " damaged by entity");
            events.add(entity.identifyType() + " damaged by " + damager.identifyType());

            if (damager.isNPC()) {
                subNPC = damager.getDenizenNPC();

                // If we had no NPC in our regular context, use this one
                if (npc == null) npc = subNPC;
            }

            else if (damager.isPlayer()) {
                subPlayer = damager.getPlayer();

                // If we had no player in our regular context, use this one
                if (player == null) player = subPlayer;
            }

            // Have a new list of events for the subContextPlayer
            // and subContextNPC

            List<String> subEvents = new ArrayList<String>();

            subEvents.add("entity damages entity");
            subEvents.add("entity damages " + entity.identifyType());
            subEvents.add(damager.identifyType() + " damages entity");
            subEvents.add(damager.identifyType() + " damages " + entity.identifyType());

            if (projectile != null && !damager.getEntityType().equals(projectile.getEntityType())) {
                subEvents.add(projectile.identifyType() + " damages entity");
                subEvents.add(projectile.identifyType() + " damages " + entity.identifyType());
            }

            if (isFatal) {

                // If this entity's UUID isn't stored in entityKillers
                // along with its killer, store it
                if (!entityKillers.containsKey(entity.getUUID())) {
                    entityKillers.put(entity.getUUID(), damager);
                }

                events.add("entity killed by entity");
                events.add("entity killed by " + damager.identifyType());
                events.add(entity.identifyType() + " killed by entity");
                events.add(entity.identifyType() + " killed by " + damager.identifyType());

                if (projectile != null && !damager.getEntityType().equals(projectile.getEntityType())) {
                    events.add("entity killed by " + projectile.identifyType());
                    events.add(entity.identifyType() + " killed by " + projectile.identifyType());
                }

                // <--[event]
                // @Events
                // entity kills entity
                // entity kills <entity>
                // <entity> kills entity
                // <entity> kills <entity>
                //
                // @Triggers when an entity kills another entity.
                // @Context
                // <context.cause> returns the reason the entity was killed.
                // <context.entity> returns the dEntity that was killed.
                // <context.damager> returns the dEntity killing the other entity.
                // <context.projectile> returns the projectile, if one caused the event.
                //
                // @Determine
                // "CANCELLED" to stop the entity from being killed.
                // Element(Number) to set the amount of damage the entity receives, instead of dying.
                //
                // -->

                subEvents.add("entity kills entity");
                subEvents.add("entity kills " + entity.identifyType());
                subEvents.add(damager.identifyType() + " kills entity");
                subEvents.add(damager.identifyType() + " kills " + entity.identifyType());

                if (projectile != null && !damager.getEntityType().equals(projectile.getEntityType())) {
                    subEvents.add(projectile.identifyType() + " kills entity");
                    subEvents.add(projectile.identifyType() + " kills " + entity.identifyType());
                }
            }

            determination = EventManager.doEvents(subEvents, subNPC, subPlayer, context, true);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);

            else if (Argument.valueOf(determination)
                    .matchesPrimitive(aH.PrimitiveType.Double)) {
                event.setDamage(aH.getDoubleFrom(determination));
            }
        }

        determination = EventManager.doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Double)) {
            event.setDamage(aH.getDoubleFrom(determination));
        }
    }

    // <--[event]
    // @Events
    // entity dies
    // <entity> death
    //
    // @Triggers when an entity dies.
    // @Context
    // <context.entity> returns the dEntity that died.
    // <context.damager> returns the dEntity damaging the other entity, if any.
    // <context.message> returns an Element of a player's death message.
    // <context.inventory> returns the dInventory of the entity if it was a player.
    //
    // @Determine
    // Element(String) to change the death message.
    // "NO_DROPS" to specify that any drops should be removed.
    // "NO_DROPS_OR_XP" to specify that any drops or XP orbs should be removed.
    // "NO_XP" to specify that any XP orbs should be removed.
    // dList(dItem) to specify new items to be dropped.
    // Element(Number) to specify the new amount of XP to be dropped.
    //
    // -->
    @EventHandler
    public void entityDeath(EntityDeathEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        // If this entity has a stored killer, get it and then
        // remove it from the entityKillers map
        if (entityKillers.containsKey(entity.getUUID())) {
            context.put("damager", entityKillers.get(entity.getUUID()));
            entityKillers.remove(entity.getUUID());
        }

        PlayerDeathEvent subEvent = null;

        if (event instanceof PlayerDeathEvent) {
            subEvent = (PlayerDeathEvent) event;
            context.put("message", new Element(subEvent.getDeathMessage()));

            // Null check to prevent NPCs from causing an NPE
            if (player != null)
                context.put("inventory", dInventory.mirrorBukkitInventory(player.getInventory()));
        }

        String determination = EventManager.doEvents(Arrays.asList
                ("entity dies",
                        entity.identifyType() + " dies",
                        "entity death",
                        entity.identifyType() + " death"),
                npc, player, context, true);

        // Handle message
        if (determination.toUpperCase().startsWith("DROPS ")) {
            determination = determination.substring(6);
        }

        if (determination.toUpperCase().startsWith("NO_DROPS")) {
            event.getDrops().clear();
            if (determination.endsWith("_OR_XP")) {
                event.setDroppedExp(0);
            }
        }

        else if (determination.toUpperCase().equals("NO_XP")) {
            event.setDroppedExp(0);
        }

        // Drops
        else if (Argument.valueOf(determination).matchesArgumentList(dItem.class)) {
            dList drops = dList.valueOf(determination);
            drops.filter(dItem.class);
            event.getDrops().clear();
            for (String drop : drops) {
                dItem item = dItem.valueOf(drop);
                if (item != null)
                    event.getDrops().add(item.getItemStack());
            }

        }

        // XP
        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Integer)) {
            int xp = Integer.valueOf(determination.substring(3));
            event.setDroppedExp(xp);
        }

        else if (!determination.toUpperCase().equals("NONE")) {
            if (event instanceof PlayerDeathEvent) {
                subEvent.setDeathMessage(determination);
            }
        }
    }

    // <--[event]
    // @Events
    // entity explodes
    // <entity> explodes
    //
    // @Triggers when an entity explodes.
    // @Context
    // <context.blocks> returns a dList of blocks that the entity blew up.
    // <context.entity> returns the dEntity that exploded.
    // <context.location> returns the dLocation the entity blew up at.
    //
    // @Determine
    // "CANCELLED" to stop the entity from exploding.
    // dList(dLocation) to set a new lists of blocks that are to be affected by the explosion.
    //
    // -->
    @EventHandler
    public void entityExplode(EntityExplodeEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        if (event.getEntity() == null) {
            return; // Fix for other plugins doing weird stuff.
        }
        dEntity entity = new dEntity(event.getEntity());

        context.put("entity", entity.getDenizenObject());
        context.put("location", new dLocation(event.getLocation()));

        String blocks = "";
        for (Block block : event.blockList()) {
            blocks = blocks + new dLocation(block.getLocation()) + "|";
        }
        context.put("blocks", blocks.length() > 0 ? new dList(blocks) : null);

        String determination = EventManager.doEvents(Arrays.asList
                ("entity explodes",
                        entity.identifyType() + " explodes"),
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        else if (determination.length() > 0 && !determination.equalsIgnoreCase("none")) {
            dList list = dList.valueOf(determination);
            event.blockList().clear();
            for (String loc: list) {
                dLocation location = dLocation.valueOf(loc);
                if (location == null)
                    dB.echoError("Invalid location '" + loc + "'");
                else
                    event.blockList().add(location.getWorld().getBlockAt(location));
            }
        }
    }

    // <--[event]
    // @Events
    // entity heals (because <cause>)
    // <entity> heals (because <cause>)
    //
    // @Triggers when an entity heals.
    // @Context
    // <context.amount> returns the amount the entity healed.
    // <context.entity> returns the dEntity that healed.
    // <context.reason> returns the cause of the entity healing.
    //
    // @Determine
    // "CANCELLED" to stop the entity from healing.
    // Element(Double) to set the amount of health the entity receives.
    //
    // -->
    @EventHandler
    public void entityRegainHealth(EntityRegainHealthEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        String reason = event.getRegainReason().name();

        context.put("reason", new Element(event.getRegainReason().name()));
        context.put("amount", new Element(event.getAmount()));
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        String determination = EventManager.doEvents(Arrays.asList
                ("entity heals",
                        "entity heals because " + reason,
                        entity.identifyType() + " heals",
                        entity.identifyType() + " heals because " + reason),
                npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Double)) {
            event.setAmount(aH.getDoubleFrom(determination));
        }
    }

    // <--[event]
    // @Events
    // entity enters portal
    // <entity> enters portal
    //
    // @Triggers when an entity enters a portal.
    // @Context
    // <context.entity> returns the dEntity.
    // <context.location> returns the dLocation of the portal block touched by the entity.
    //
    // -->
    @EventHandler
    public void entityPortalEnter(EntityPortalEnterEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("location", new dLocation(event.getLocation()));
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        EventManager.doEvents(Arrays.asList
                ("entity enters portal",
                        entity.identifyType() + " enters portal"),
                npc, player, context, true);
    }

    // <--[event]
    // @Events
    // entity exits portal
    // <entity> exits portal
    //
    // @Triggers when an entity exits a portal.
    // @Context
    // <context.entity> returns the dEntity.
    // <context.location> returns the dLocation of the portal block touched by the entity.
    //
    // -->
    @EventHandler
    public void entityPortalExit(EntityPortalExitEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("location", new dLocation(event.getTo()));
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        EventManager.doEvents(Arrays.asList
                ("entity exits portal",
                        entity.identifyType() + " exits portal"),
                npc, player, context, true);
    }

    // <--[event]
    // @Events
    // entity shoots bow
    // <entity> shoots bow
    // entity shoots <item>
    // <entity> shoots <item>
    //
    // @Triggers when an entity shoots something out of a bow.
    // @Context
    // <context.entity> returns the dEntity that shot the bow.
    // <context.projectile> returns a dEntity of the projectile.
    // <context.bow> returns the bow item used to shoot.
    // <context.force> returns the force of the shot.
    //
    // @Determine
    // "CANCELLED" to stop the entity from shooting the bow.
    // dList(dEntity) to change the projectile(s) being shot.
    //
    // -->
    @EventHandler
    public void entityShootBow(EntityShootBowEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        Player player = null;
        dNPC npc = null;

        dItem bow = new dItem(event.getBow());
        dEntity projectile = new dEntity(event.getProjectile());
        dEntity entity = new dEntity(event.getEntity());

        context.put("bow", bow);
        context.put("projectile", projectile);
        context.put("entity", entity.getDenizenObject());
        context.put("force", new Element(event.getForce() * 3));

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        String determination = EventManager.doEvents(Arrays.asList
                ("entity shoots bow",
                        "entity shoots " + bow.identifySimple(),
                        entity.identifyType() + " shoots bow",
                        entity.identifyType() + " shoots " + bow.identifySimple()),
                npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED")) {
            event.setCancelled(true);
        }

        // Don't use event.setProjectile() because it doesn't work
        else if (Argument.valueOf(determination).matchesArgumentList(dEntity.class)) {

            event.setCancelled(true);

            // Get the list of entities
            Object list = dList.valueOf(determination).filter(dEntity.class);

            @SuppressWarnings("unchecked")
            List<dEntity> newProjectiles = (List<dEntity>) list;

            // Go through all the entities, spawning/teleporting them
            for (dEntity newProjectile : newProjectiles) {
                newProjectile.spawnAt(entity.getEyeLocation()
                        .add(entity.getEyeLocation().getDirection()));

                // Set the entity as the shooter of the projectile,
                // where applicable
                if (newProjectile.isProjectile()) {
                    newProjectile.setShooter(entity);
                }
            }

            // Mount the projectiles on top of each other
            Position.mount(Conversion.convertEntities(newProjectiles));

            // Get the last entity on the list, i.e. the one at the bottom
            // if there are many mounted on top of each other
            Entity lastProjectile = newProjectiles.get
                    (newProjectiles.size() - 1).getBukkitEntity();

            // Give it the same velocity as the arrow that would
            // have been shot by the bow
            // Note: No, I can't explain why this has to be multiplied by three, it just does.
            lastProjectile.setVelocity(event.getEntity().getLocation()
                    .getDirection().multiply(event.getForce() * 3));
        }
    }

    // <--[event]
    // @Events
    // entity tamed
    // <entity> tamed
    // player tames entity
    // player tames <entity>
    //
    // @Triggers when an entity is tamed.
    // @Context
    // <context.entity> returns a dEntity of the tamed entity.
    //
    // @Determine
    // "CANCELLED" to stop the entity from being tamed.
    //
    // -->
    @EventHandler
    public void entityTame(EntityTameEvent event) {

        Player player = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        context.put("entity", entity);

        List<String> events = new ArrayList<String>();
        events.add("entity tamed");
        events.add(entity.identifyType() + " tamed");

        if (event.getOwner() instanceof Player) {
            player = (Player) event.getOwner();
            events.add("player tames entity");
            events.add("player tames " + entity.identifyType());
        }

        String determination = EventManager.doEvents(events, null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity targets (<entity>)
    // entity targets (<entity>) because <cause>
    // <entity> targets (<entity>)
    // <entity> targets (<entity>) because <cause>
    //
    // @Triggers when an entity targets a new entity.
    // @Context
    // <context.entity> returns the targeting entity.
    // <context.reason> returns the reason the entity changed targets.
    // <context.target> returns the targeted entity.
    //
    // @Determine
    // "CANCELLED" to stop the entity from being targeted.
    // dEntity to make the entity target a different entity instead.
    //
    // -->
    @EventHandler
    public void entityTarget(EntityTargetEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        final dEntity entity = new dEntity(event.getEntity());

        String reason = event.getReason().name();

        context.put("entity", entity);
        context.put("reason", new Element(reason));

        List<String> events = new ArrayList<String>();
        events.add("entity targets");
        events.add("entity targets because " + reason);
        events.add(entity.identifyType() + " targets");
        events.add(entity.identifyType() + " targets because " + reason);

        if (event.getTarget() != null) {

            dEntity target = new dEntity(event.getTarget());
            context.put("target", target.getDenizenObject());

            if (target.isNPC()) { npc = target.getDenizenNPC(); }
            else if (target.isPlayer()) player = target.getPlayer();

            events.add("entity targets entity");
            events.add("entity targets entity because " + reason);
            events.add("entity targets " + target.identifyType());
            events.add("entity targets " + target.identifyType() + " because " + reason);
            events.add(entity.identifyType() + " targets entity");
            events.add(entity.identifyType() + " targets entity because " + reason);
            events.add(entity.identifyType() + " targets " + target.identifyType());
            events.add(entity.identifyType() + " targets " + target.identifyType() + " because " + reason);
        }

        String determination = EventManager.doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);

            // If the determination matches a dEntity, change the event's target
            // using a scheduled task (otherwise, the target will not be changed)
            //
            // Note: this does not work with all monster types
        else if (dEntity.matches(determination)) {

            final dEntity newTarget = dEntity.valueOf(determination);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                @Override
                public void run() {
                    entity.target(newTarget.getLivingEntity());
                }
            }, 1);
        }
    }

    // <--[event]
    // @Events
    // entity teleports
    // <entity> teleports
    //
    // @Triggers when an entity teleports.
    // @Context
    // <context.entity> returns the dEntity.
    // <context.origin> returns the dLocation the entity teleported from.
    // <context.destination> returns the dLocation the entity teleported to.
    //
    // @Determine
    // "CANCELLED" to stop the entity from teleporting.
    //
    // -->
    @EventHandler
    public void entityTeleport(EntityTeleportEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("origin", new dLocation(event.getFrom()));
        context.put("destination", new dLocation(event.getTo()));
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        String determination = EventManager.doEvents(Arrays.asList
                ("entity teleports",
                        entity.identifyType() + " teleports"),
                npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity unleashed (because <reason>)
    // <entity> unleashed (because <reason>)
    //
    // @Triggers when an entity is unleashed.
    // @Context
    // <context.entity> returns the dEntity.
    // <context.reason> returns an Element of the reason for the unleashing.
    //
    // -->
    @EventHandler
    public void entityUnleash(EntityUnleashEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        String reason = event.getReason().name();

        context.put("entity", entity.getDenizenObject());
        context.put("reason", new Element(reason));

        EventManager.doEvents(Arrays.asList
                ("entity unleashed",
                        "entity unleashed because " + reason,
                        entity.identifyType() + " unleashed",
                        entity.identifyType() + " unleashed because " + reason),
                null, null, context, true);
    }

    // <--[event]
    // @Events
    // entity explosion primes
    // <entity> explosion primes
    //
    // @Triggers when an entity decides to explode.
    // @Context
    // <context.entity> returns the dEntity.
    // <context.origin> returns an Element of the explosion's radius.
    // <context.fire> returns an Element with a value of "true" if the explosion will create fire and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the entity from deciding to explode.
    //
    // -->
    @EventHandler
    public void explosionPrimeEvent(ExplosionPrimeEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        Entity entity = event.getEntity();

        context.put("entity", new dEntity(entity));
        context.put("radius", new Element(event.getRadius()));
        context.put("fire", new Element(event.getFire()));

        String determination = EventManager.doEvents(Arrays.asList
                ("entity explosion primes",
                        entity.getType().name() + " explosion primes"),
                null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // entity changes food level
    // <entity> changes food level
    //
    // @Triggers when an entity's food level changes.
    // @Context
    // <context.entity> returns the dEntity.
    // <context.food> returns an Element(Integer) of the entity's new food level.
    //
    // @Determine
    // "CANCELLED" to stop the entity's food level from changing.
    // Element(Double) to set the entity's new food level.
    //
    // -->
    @EventHandler
    public void foodLevelChange(FoodLevelChangeEvent event) {

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("food", new Element(event.getFoodLevel()));
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        String determination = EventManager.doEvents(Arrays.asList
                ("entity changes food level",
                        entity.identifyType() + " changes food level"),
                npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Integer)) {
            event.setFoodLevel(aH.getIntegerFrom(determination));
        }
    }

    // <--[event]
    // @Events
    // horse jumps
    // (<color>) (<type>) jumps
    //
    // @Triggers when a horse jumps.
    // @Context
    // <context.entity> returns the dEntity of the horse.
    // <context.color> returns an Element of the horse's color.
    // <context.variant> returns an Element of the horse's variant.
    // <context.food> returns an Element(Float) of the jump's power.
    //
    // @Determine
    // "CANCELLED" to stop the horse from jumping.
    // Element(Double) to set the power of the jump.
    //
    // -->
    @EventHandler
    public void horseJump(HorseJumpEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        String variant = event.getEntity().getVariant().name();
        String color = event.getEntity().getColor().name();
        dEntity entity = new dEntity(event.getEntity());

        context.put("variant", new Element(variant));
        context.put("color", new Element(color));
        context.put("power", new Element(event.getPower()));
        context.put("entity", entity);

        String determination = EventManager.doEvents(Arrays.asList
                ("horse jumps",
                        variant + " jumps",
                        color + " jumps",
                        color + " " + variant + " jumps"),
                null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Float)) {
            event.setPower(aH.getFloatFrom(determination));
        }
    }

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

        String determination = EventManager.doEvents(events, null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // item spawns
    // <item> spawns
    // <material> spawns
    //
    // @Triggers when an item entity spawns.
    // @Context
    // <context.item> returns the dItem of the entity.
    // <context.entity> returns the dEntity.
    //
    // @Determine
    // "CANCELLED" to stop the item entity from spawning.
    //
    // -->
    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(event.getEntity().getItemStack());

        context.put("item", item);
        context.put("entity", new dEntity(event.getEntity()));

        List<String> events = new ArrayList<String>();
        events.add("item spawns");
        events.add(item.identifySimple() + " spawns");
        events.add(item.identifyMaterial() + " spawns");

        String determination = EventManager.doEvents(events, null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // pig zapped
    //
    // @Triggers when a pig is zapped by lightning and turned into a pig zombie.
    // @Context
    // <context.pig> returns the dEntity of the pig.
    // <context.pig_zombie> returns the dEntity of the pig zombie.
    // <context.lightning> returns the dEntity of the lightning.
    //
    // @Determine
    // "CANCELLED" to stop the pig from being zapped.
    //
    // -->
    @EventHandler
    public void pigZap(PigZapEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity pig = new dEntity(event.getEntity());
        dEntity pigZombie = new dEntity(event.getPigZombie());
        dEntity lightning = new dEntity(event.getLightning());

        context.put("pig", pig);
        context.put("pig_zombie", pigZombie);
        context.put("lightning", lightning);

        String determination = EventManager.doEvents(Arrays.asList
                ("pig zapped"), null, null, context);

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

        Player player = null;
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

            if (shooter.isNPC()) { npc = shooter.getDenizenNPC(); }
            else if (shooter.isPlayer()) { player = shooter.getPlayer(); }

            events.add("entity shoots block");
            events.add("entity shoots block with " + projectile.identifyType());
            events.add("entity shoots " + material.identifySimple() + " with " + projectile.identifyType());
            events.add(shooter.identifyType() + " shoots block");
            events.add(shooter.identifyType() + " shoots block with " + projectile.identifyType());
            events.add(shooter.identifyType() + " shoots " + material.identifySimple() + " with " + projectile.identifyType());
        }

        EventManager.doEvents(events, npc, player, context, true);
    }

    // <--[event]
    // @Events
    // player dyes sheep (<color>)
    // sheep dyed (<color>)
    //
    // @Triggers when a sheep is dyed by a player.
    // @Context
    // <context.entity> returns the dEntity of the sheep.
    // <context.color> returns an Element of the color the sheep is being dyed.
    //
    // @Determine
    // "CANCELLED" to stop it from being dyed.
    // Element(String) that matches DyeColor to dye it a different color.
    //
    // -->
    @EventHandler
    public void sheepDyeWool(SheepDyeWoolEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        String color = event.getColor().name();

        context.put("entity", entity);
        context.put("color", new Element(color));

        String determination = EventManager.doEvents(Arrays.asList
                ("player dyes sheep",
                        "player dyes sheep " + color,
                        "sheep dyed",
                        "sheep dyed " + color), null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (determination.equals(determination.toUpperCase())) {
            try {
                event.setColor(DyeColor.valueOf(determination));
            }
            catch (IllegalArgumentException e) {
                dB.echoError("Unknown dye color " + determination);
            }
        }
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

        String determination = EventManager.doEvents(Arrays.asList
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
    // <context.count> returns an Element(Integer) of the number of smaller slimes it will split into.
    //
    // @Determine
    // "CANCELLED" to stop it from splitting.
    // Element(Integer) to set the number of smaller slimes it will split into.
    //
    // -->
    @EventHandler
    public void slimeSplit(SlimeSplitEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());
        int count = event.getCount();

        context.put("entity", entity);
        context.put("count", new Element(count));

        String determination = EventManager.doEvents(Arrays.asList
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
    // item enchanted
    // <item> enchanted
    //
    // @Triggers when an item is enchanted.
    // @Context
    // <context.location> returns the dLocation of the enchanting table.
    // <context.inventory> returns the dInventory of the enchanting table.
    // <context.item> returns the dItem to be enchanted.
    //
    // @Determine
    // "CANCELLED" to stop the item from being enchanted.
    //
    // -->
    @EventHandler
    public void enchantItemEvent(EnchantItemEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        Player player = event.getEnchanter();
        dItem item = new dItem(event.getItem());

        context.put("location", new dLocation(event.getEnchantBlock().getLocation()));
        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));
        context.put("item", item);

        String determination = EventManager.doEvents(Arrays.asList
                ("item enchanted",
                        item.identifySimple() + " enchanted"),
                null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
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
    // player (<click type>) clicks (<item>) (in <inventory>) (with <item>)
    // player (<click type>) clicks (<material>) (in <inventory>) (with <item>)
    // player (<click type>) clicks (<item>) (in <inventory>) (with <material>)
    // player (<click type>) clicks (<material>) (in <inventory>) (with <material>)
    //
    // @Triggers when a player clicks in an inventory.
    // @Context
    // <context.item> returns the dItem the player has clicked on.
    // <context.inventory> returns the dInventory.
    // <context.cursor_item> returns the item the Player is clicking with.
    // <context.click> returns an Element with the name of the click type.
    // <context.slot_type> returns an Element with the name of the slot type that was clicked.
    // <context.slot> returns an Element with the number of the slot that was clicked.
    // <context.is_shift_click> returns true if 'shift' was used while clicking.
    // <context.action> returns the inventory_action. See <@link language Inventory Actions>.
    //
    // @Determine
    // "CANCELLED" to stop the player from clicking.
    //
    // -->
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = null;
        dItem holding;

        Player player = (Player) event.getWhoClicked();
        String type = event.getInventory().getType().name();
        String click = event.getClick().name();
        String slotType = event.getSlotType().name();

        List<String> events = new ArrayList<String>();
        events.add("player clicks in inventory");
        events.add("player clicks in " + type);

        String interaction = "player " + click + " clicks ";

        events.add(interaction + "in inventory");
        events.add(interaction + "in " + type);

        if (event.getCursor() != null) {
            holding = new dItem(event.getCursor());
            context.put("cursor_item", holding);

            events.add(interaction + "in inventory with " + holding.identifySimple());
            events.add(interaction + "in " + type + " with " + holding.identifySimple());
            events.add(interaction + "in inventory with " + holding.identifyMaterial());
            events.add(interaction + "in " + type + " with " + holding.identifyMaterial());
            events.add("player clicks in inventory with " + holding.identifySimple());
            events.add("player clicks in " + type + " with " + holding.identifySimple());
            events.add("player clicks in inventory with " + holding.identifyMaterial());
            events.add("player clicks in " + type + " with " + holding.identifyMaterial());
        }

        if (event.getCurrentItem() != null) {
            item = new dItem(event.getCurrentItem());

            events.add("player clicks " +
                    item.identifySimple() + " in inventory");
            events.add(interaction +
                    item.identifySimple() + " in inventory");
            events.add(interaction +
                    item.identifySimple() + " in " + type);
            events.add("player clicks " +
                    item.identifyMaterial() + " in inventory");
            events.add(interaction +
                    item.identifyMaterial() + " in inventory");
            events.add(interaction +
                    item.identifyMaterial() + " in " + type);

            if (event.getCursor() != null) {
                holding = new dItem(event.getCursor());

                events.add("player clicks " +
                        item.identifySimple() + " in inventory with " + holding.identifySimple());
                events.add(interaction +
                        item.identifySimple() + " in inventory with " + holding.identifySimple());
                events.add(interaction +
                        item.identifySimple() + " in " + type + " with " + holding.identifySimple());
                events.add("player clicks " +
                        item.identifySimple() + " in inventory with " + holding.identifyMaterial());
                events.add(interaction +
                        item.identifySimple() + " in inventory with " + holding.identifyMaterial());
                events.add(interaction +
                        item.identifySimple() + " in " + type + " with " + holding.identifyMaterial());
                events.add("player clicks " +
                        item.identifyMaterial() + " in inventory with " + holding.identifyMaterial());
                events.add(interaction +
                        item.identifyMaterial() + " in inventory with " + holding.identifyMaterial());
                events.add(interaction +
                        item.identifyMaterial() + " in " + type + " with " + holding.identifyMaterial());
                events.add("player clicks " +
                        item.identifyMaterial() + " in inventory with " + holding.identifySimple());
                events.add(interaction +
                        item.identifyMaterial() + " in inventory with " + holding.identifySimple());
                events.add(interaction +
                        item.identifyMaterial() + " in " + type + " with " + holding.identifySimple());
            }
        }

        events = EventManager.trimEvents(events);

        if (events.size() == 0) return;

        context.put("item", item);
        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));
        context.put("click", new Element(click));
        context.put("slot_type", new Element(slotType));
        context.put("slot", new Element(event.getSlot()));
        context.put("is_shift_click", new Element(event.isShiftClick()));
        context.put("action", new Element(event.getAction().name()));

        String determination = EventManager.doEvents(events, null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player closes inventory
    // player closes <inventory>
    //
    // @Triggers when a player closes an inventory.
    // @Context
    // <context.inventory> returns the dInventory.
    //
    // -->
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        Player player = (Player) event.getPlayer();
        String type = event.getInventory().getType().name();

        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));

        EventManager.doEvents(Arrays.asList
                ("player closes inventory",
                        "player closes " + type),
                null, player, context);
    }

    // <--[event]
    // @Events
    // player drags in inventory
    // player drags (<item>) (in <inventory>)
    //
    // @Triggers when a player drags in an inventory.
    // @Context
    // <context.item> returns the dItem the player has dragged.
    // <context.inventory> returns the dInventory.
    //
    // @Determine
    // "CANCELLED" to stop the player from dragging.
    //
    // -->
    @EventHandler
    public void inventoryDragEvent(InventoryDragEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = null;

        Player player = (Player) event.getWhoClicked();
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

        events = EventManager.trimEvents(events);

        if (events.size() == 0 ) return;

        context.put("item", item);
        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));

        String determination = EventManager.doEvents(events, null, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // item moves from inventory (to <inventory>)
    // item moves from <inventory> (to <inventory>)
    //
    // @Triggers when an entity or block moves an item from one inventory to another.
    // @Context
    // <context.origin> returns the origin dInventory.
    // <context.destination> returns the destination dInventory.
    // <context.initiator> returns the dInventory that initiatied the item's transfer.
    // <context.item> returns the dItem that was moved.
    //
    // @Determine
    // "CANCELLED" to stop the item from being moved.
    // dItem to set a different item to be moved.
    //
    // -->
    @EventHandler
    public void inventoryMoveItemEvent(InventoryMoveItemEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        dItem item = new dItem(event.getItem());
        String originType = event.getSource().getType().name();
        String destinationType = event.getDestination().getType().name();

        List<String> events = Arrays.asList("item moves from inventory",
                "item moves from " + originType,
                "item moves from " + originType
                        + " to " + destinationType,
                item.identifySimple() + " moves from inventory",
                item.identifySimple() + " moves from " + originType,
                item.identifySimple() + " moves from " + originType
                        + " to " + destinationType);

        events = EventManager.trimEvents(events);

        if (events.size() == 0) return;

        context.put("origin", dInventory.mirrorBukkitInventory(event.getSource()));
        context.put("destination", dInventory.mirrorBukkitInventory(event.getDestination()));
        context.put("initiator", dInventory.mirrorBukkitInventory(event.getInitiator()));
        context.put("item", item);

        String determination = EventManager.doEvents(events,
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (dItem.matches(determination))
            event.setItem(dItem.valueOf(determination).getItemStack());
    }

    // <--[event]
    // @Events
    // player opens inventory
    // player opens <inventory>
    //
    // @Triggers when a player opens an inventory.
    // @Context
    // <context.inventory> returns the dInventory.
    //
    // @Determine
    // "CANCELLED" to stop the player from opening the inventory.
    //
    // -->
    @EventHandler
    public void inventoryOpenEvent(InventoryOpenEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        Player player = (Player) event.getPlayer();
        String type = event.getInventory().getType().name();

        context.put("inventory", dInventory.mirrorBukkitInventory(event.getInventory()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player opens inventory",
                        "player opens " + type),
                null, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // inventory picks up item
    // inventory picks up <item>
    // <inventory> picks up item
    // <inventory> picks up <item>
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

        events = EventManager.trimEvents(events);

        if (events.size() == 0) return;

        dInventory inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        context.put("inventory", inventory);
        context.put("item", item);

        String determination = EventManager.doEvents(events,
                null, null, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }


    /////////////////////
    //   PLAYER EVENTS
    /////////////////

    // <--[event]
    // @Events
    // player chats
    //
    // @Triggers when a player chats.
    // @Context
    // <context.message> returns the player's message as an Element.
    //
    // @Determine
    // "CANCELLED" to stop the player from chatting.
    // Element(String) to change the message.
    //
    // -->
    @EventHandler(priority = EventPriority.LOWEST)
    public void asyncPlayerChat(final AsyncPlayerChatEvent event) {

        // Return if "Use asynchronous event" is false in config file
        if (!Settings.WorldScriptChatEventAsynchronous()) return;

        final Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getMessage()));

        Callable<String> call = new Callable<String>() {
            @Override
            public String call() {
                return EventManager.doEvents(Arrays.asList("player chats"),
                        null, event.getPlayer(), context);
            }
        };
        String determination = null;
        try {
            determination = event.isAsynchronous() ? Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), call).get() : call.call();
        } catch (InterruptedException e) {
            // TODO: Need to find a way to fix this eventually
            // dB.echoError(e);
        } catch (ExecutionException e) {
            dB.echoError(e);
        } catch (Exception e) {
            dB.echoError(e);
        }

        if (determination == null)
            return;
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (!determination.equals("none")) {
            event.setMessage(determination);
        }
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

        String determination = EventManager.doEvents(events,
                null, event.getPlayer(), context);

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

        Map<String, dObject> context = new HashMap<String, dObject>();
        final ItemStack itemstack = event.getBrokenItem();
        dItem item = new dItem(itemstack);
        context.put("item", item);

        String determination = EventManager.doEvents(Arrays.asList
                ("player breaks item",
                        "player breaks " + item.identifySimple(),
                        "player breaks " + item.identifyMaterial()),
                null, event.getPlayer(), context).toUpperCase();

        if (determination.startsWith("CANCELLED")) {
            // The ItemStack isn't really gone yet, only set to stack size 0.
            // So just add 1 more item to the stack.
            itemstack.setAmount(itemstack.getAmount()+1);
            // The event automatically resets durability to 0... instead,
            // let's delay a tick and set it back to what it was before.
            final short durability = itemstack.getDurability();
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

        Map<String, dObject> context = new HashMap<String, dObject>();
        String animation = event.getAnimationType().name();
        context.put("animation", new Element(animation));

        String determination = EventManager.doEvents(Arrays.asList
                ("player animates",
                        "player animates " + animation),
                null, event.getPlayer(), context);

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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        String determination = EventManager.doEvents
                (Arrays.asList("player enters bed"),
                        null, event.getPlayer(), context);

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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        EventManager.doEvents(Arrays.asList
                ("player leaves bed"),
                null, event.getPlayer(), context);
    }

    // <--[event]
    // @Events
    // player empties bucket
    //
    // @Triggers when a player empties a bucket.
    // @Context
    // <context.item> returns the dItem of the bucket.
    // <context.location> returns the dLocation of the block clicked with the bucket.
    //
    // @Determine
    // "CANCELLED" to stop the player from emptying the bucket.
    // dItem to set the item in the player's hand after the event.
    //
    // -->
    @EventHandler
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("item", new dItem(event.getBucket()));
        context.put("location", new dLocation(event.getBlockClicked().getLocation()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player empties bucket"),
                null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (dItem.matches(determination)) {
            ItemStack is = dItem.valueOf(determination).getItemStack();
            event.setItemStack( is != null ? is : new ItemStack(Material.AIR));
        }

    }

    // <--[event]
    // @Events
    // player fills bucket
    //
    // @Triggers when a player fills a bucket.
    // @Context
    // <context.item> returns the dItem of the bucket.
    // <context.location> returns the dLocation of the block clicked with the bucket.
    //
    // @Determine
    // "CANCELLED" to stop the player from filling the bucket.
    // dItem to set the item in the player's hand after the event.
    //
    // -->
    @EventHandler
    public void playerBucketFill(PlayerBucketFillEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("item", new dItem(event.getBucket()));
        context.put("location", new dLocation(event.getBlockClicked().getLocation()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player fills bucket"),
                null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (dItem.matches(determination)) {
            ItemStack is = dItem.valueOf(determination).getItemStack();
            event.setItemStack( is != null ? is : new ItemStack(Material.AIR));
        }
    }

    // <--[event]
    // @Events
    // player changes world (from <world>) to (<world>)
    //
    // @Triggers when a player moves to a different world.
    // @Context
    // <context.origin_world> returns the dWorld that the player was previously on.
    //
    // -->
    @EventHandler
    public void playerChangedWorld(PlayerChangedWorldEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dWorld originWorld = new dWorld(event.getFrom());
        dWorld destinationWorld = new dWorld(event.getPlayer().getWorld());
        context.put("origin_world", originWorld);

        EventManager.doEvents(Arrays.asList
                ("player changes world",
                        "player changes world from " + originWorld.identifySimple(),
                        "player changes world to " + destinationWorld.identifySimple(),
                        "player changes world from " + originWorld.identifySimple() +
                                " to " + destinationWorld.identifySimple()),
                null, event.getPlayer(), context, true);
    }

    // Shares description with asyncPlayerChat
    @EventHandler
    public void playerChat(final PlayerChatEvent event) {

        // If currently recording debug information, quickly add the chat message to debug output
        // (Intentionally placed in the sync event to prevent glitching)
        if (dB.record) dB.log(ChatColor.DARK_GREEN + "CHAT: " +
                event.getPlayer().getName() + ": " + event.getMessage());

        // Return if "Use asynchronous event" is true in config file
        if (Settings.WorldScriptChatEventAsynchronous()) return;

        final Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getMessage()));

        String determination = EventManager.doEvents(Arrays.asList("player chats"),
                null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        else if (!determination.equals("none")) {
            event.setMessage(determination);
        }
    }

    // <--[example]
    // @Title On Command Event tutorial
    // @Description
    // Denizen contains the ability to run script entries in the form
    // of a bukkit /command. Here's an example script that shows basic usage.
    //
    // @Code
    // # +--------------------
    // # | On Command Event tutorial
    // # |
    // # | Denizen contains the ability to run script entries in the form
    // # | of a Bukkit /command. Here's an example script that shows basic usage.
    //
    // On Command Event Tutorial:
    //  type: world
    //
    // # +-- EVENTS: Node --+
    // # To 'hook' into the on command event, just create a 'on <command_name> command'
    // # node as a child of the events node in any world script. Change out <command_name>
    // # with the desired name of the command. This can only be one word.
    //
    //   events:
    //
    //     # The following example will trigger on the use of '/testcommand'
    //     on testcommand command:
    //
    //     # Why not state the obvious? Just to be sure!
    //     - narrate 'You just used the /testcommand command!'
    //
    //     # You can utilize any arguments that come along with the command, too!
    //     # <context.args> returns a list of the arguments, run through the Denizen argument
    //     # interpreter. Using quotes will allow the use of multiple word arguments,
    //     # just like Denizen!
    //     # Just need what was typed after the command? Use <context.raw_args> for a String
    //     # Element containing the uninterpreted arguments.
    //     - define arg_size <context.args.size>
    //     - narrate "'%arg_size%' arguments were used."
    //     - if %arg_size% > 0 {
    //       - narrate "'<context.args.get[1]>' was the first argument."
    //       - narrate "Here's a list of all the arguments<&co> <context.args.as_cslist>"
    //       }
    //
    //     # Commands won't be checked for <replaceable tags> So if you type /testcommand <player.name>
    //     # It won't be read as /testcommand mcmonkey
    //     # If you want tags to be parsed (read and translated), you can instead use '<context.parsed_args>'
    //     - narrate "With tag parsing, you input <context.parsed_args>"
    //     - if %arg_size% > 0 {
    //       - narrate "'<context.parsed_args.get[1]>' was the first argument."
    //       }
    //
    //     # When a command isn't found, Bukkit reports an error. To let Bukkit know
    //     # that the command was handled, use the 'determine fulfilled' command/arg.
    //     - determine fulfilled
    //
    // -->

    // <--[event]
    // @Events
    // command
    // <command_name> command (in <notable cuboid>)
    //
    // @Triggers when a player or console runs a Bukkit command. This happens before
    // any code of established commands allowing scripters to 'override' existing commands.
    // @Context
    // <context.command> returns the command name as an Element.
    // <context.raw_args> returns any args used as an Element.
    // <context.args> returns a dList of the arguments.
    // <context.server> returns true if the command was run from the console.
    // <context.cuboids> returns a list of cuboids the player is in when using the command.
    //
    // @Determine
    // "FULFILLED" to tell Bukkit the command was handled.
    //
    // -->
    @EventHandler
    public void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();

        dPlayer player = dPlayer.valueOf(event.getPlayer().getName());

        String message = event.getMessage();
        String command = message.split(" ")[0].replace("/", "").toUpperCase();

        List<String> events = new ArrayList<String>();

        events.add("command");
        events.add(command + " command");

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getPlayer().getLocation());

        if (cuboids.size() > 0) {
            dList cuboid_context = new dList();
            for (dCuboid cuboid : cuboids) {
                events.add(command + " command in " + cuboid.identifySimple());
                cuboid_context.add(cuboid.identifySimple());
            }
            // Add in cuboids context, if inside a cuboid
            context.put("cuboids", cuboid_context);
        }

        events = EventManager.trimEvents(events);

        if (events.size() == 0)
            return;

        List<String> args = Arrays.asList(aH.buildArgs(message.split(" ").length > 1 ? message.split(" ", 2)[1] : ""));
        List<String> parsed_args = Arrays.asList(aH.buildArgs(event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : ""));

        // Fill context
        context.put("args", new dList(args));
        context.put("parsed_args", new dList(parsed_args));
        context.put("command", new Element(command));
        context.put("raw_args", new Element((message.split(" ").length > 1
                ? message.split(" ", 2)[1] : "")));
        context.put("server", Element.FALSE);
        String determination;

        // Run any event scripts and get the determination.
        determination = EventManager.doEvents(events,
                null, event.getPlayer(), context).toUpperCase();

        // If a script has determined fulfilled, cancel this event so the player doesn't
        // receive the default 'Invalid command' gibberish from bukkit.
        if (determination.equals("FULFILLED") || determination.equals("CANCELLED"))
            event.setCancelled(true);
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

        Map<String, dObject> context = new HashMap<String, dObject>();
        dItem item = new dItem(event.getItemDrop().getItemStack());
        context.put("item", item);
        context.put("entity", new dEntity(event.getItemDrop()));
        context.put("location", new dLocation(event.getItemDrop().getLocation()));

        List<String> events = new ArrayList<String>();

        events.add("player drops item");
        events.add("player drops " + item.identifySimple());

        String determination = EventManager.doEvents(events, null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player throws (hatching/non-hatching) egg
    //
    // @Triggers when a player throws an egg.
    // @Context
    // <context.item> returns the dEntity of the egg.
    // <context.is_hatching> returns an Element with a value of "true" if the egg will hatch and "false" otherwise.
    //
    // @Determine
    // dEntity to set the type of the hatching entity.
    //
    // -->
    @EventHandler
    public void playerEggThrow(PlayerEggThrowEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity egg = new dEntity(event.getEgg());
        context.put("egg", egg);
        context.put("is_hatching", new Element(event.isHatching()));

        List<String> events = new ArrayList<String>();
        events.add("player throws egg");

        if (event.isHatching()) events.add("player throws hatching egg");
        else                    events.add("player throws non-hatching egg");

        String determination = EventManager.doEvents(events, null, event.getPlayer(), context);

        if (dEntity.matches(determination)) {
            event.setHatching(true);
            event.setHatchingType(dEntity.valueOf(determination).getEntityType());
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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("amount", new Element(event.getAmount()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player changes xp"),
                null, event.getPlayer(), context).toUpperCase();

        if (determination.equals("CANCELLED")) {
            event.setAmount(0);
        }
        else if (Argument.valueOf(determination).matchesPrimitive(PrimitiveType.Integer)) {
            event.setAmount(Integer.valueOf(determination));
        }

    }

    // <--[event]
    // @Events
    // player fishes (while <state>)
    //
    // @Triggers when a player uses a fishing rod.
    // @Context
    // <context.hook> returns a dItem of the hook.
    // <context.state> returns an Element of the fishing state.
    // <context.entity> returns a dEntity, dPlayer or dNPC of the entity being fished.
    //
    // @Determine
    // "CANCELLED" to stop the player from fishing.
    //
    // -->
    @EventHandler
    public void playerFish(PlayerFishEvent event) {

        dNPC npc = null;
        String state = event.getState().name();

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("hook", new dEntity(event.getHook()));
        context.put("state", new Element(state));

        List<String> events = new ArrayList<String>();
        events.add("player fishes");
        events.add("player fishes while " + state);

        if (event.getCaught() != null) {

            dEntity entity = new dEntity(event.getCaught());
            context.put("entity", entity.getDenizenObject());

            if (entity.isNPC()) npc = entity.getDenizenNPC();

            events.add("player fishes " + entity.identifyType());
            events.add("player fishes " + entity.identifyType() + " while " + state);
        }

        String determination = EventManager.doEvents(events, npc, event.getPlayer(), context, true);

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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("gamemode", new Element(event.getNewGameMode().name()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player changes gamemode",
                        "player changes gamemode to " + event.getNewGameMode().name()),
                null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player <click type> clicks (<material>) (with <item>) (in <notable cuboid>)
    // player <click type> clicks block (with <item>)
    // player stands on <pressure plate>
    //
    // @Triggers when a player clicks on a block or stands on a pressure plate.
    // @Context
    // <context.item> returns the dItem the player is clicking with.
    // <context.location> returns the dLocation the player is clicking on.
    // <context.cuboids> returns the notable cuboids that contain the clicked block
    //
    // @Determine
    // "CANCELLED" to stop the click from happening.
    // "CANCELLED:FALSE" to uncancel the event. Some plugins may have this cancelled by default.
    //
    // -->
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        Action action = event.getAction();
        dItem item = null;
        Player player = event.getPlayer();

        List<String> events = new ArrayList<String>();

        String interaction;

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
            interaction = "player left clicks";
        else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
            interaction = "player right clicks";
            // The only other action is PHYSICAL, which is triggered when a player
            // stands on a pressure plate
        else interaction = "player stands on";

        events.add(interaction);

        if (event.hasItem()) {
            item = new dItem(event.getItem());
            context.put("item", item);

            events.add(interaction + " with item");
            events.add(interaction + " with " + item.identifySimple());
            events.add(interaction + " with " + item.identifyMaterial());
        }

        if (event.hasBlock()) {
            Block block = event.getClickedBlock();
            dMaterial blockMaterial = dMaterial.getMaterialFrom(block.getType(), block.getData());
            context.put("location", new dLocation(block.getLocation()));

            events.add(interaction + " block");
            events.add(interaction + " " + blockMaterial.identifySimple());

            if (event.hasItem()) {
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

            // Look for cuboids that contain the block's location
            List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getClickedBlock().getLocation());

            if (cuboids.size() > 0) {
                dList cuboid_context = new dList();
                for (dCuboid cuboid : cuboids) {
                    events.add(interaction + " block in " + cuboid.identifySimple());
                    events.add(interaction + " block in cuboid");
                    events.add(interaction + ' ' + blockMaterial.identifySimple() + " in " + cuboid.identifySimple());
                    events.add(interaction + ' ' + blockMaterial.identifySimple() + " in cuboid");
                    cuboid_context.add(cuboid.identifySimple());
                }
                // Add in cuboids context, if inside a cuboid
                context.put("cuboids", cuboid_context);
            }

        }

        String determination = EventManager.doEvents(events, null, player, context, true).toUpperCase();

        if (determination.startsWith("CANCELLED:FALSE"))
            event.setCancelled(false);
        else if (determination.startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player right clicks entity (with <item>)
    // player right clicks entity in <notable cuboid>
    // player right clicks entity in cuboid
    // player right clicks <entity> (with <item>)
    // player right clicks <entity> in <notable cuboid>
    // player right clicks <entity> in cuboid

    // @Triggers when a player right clicks on an entity.
    // @Context
    // <context.entity> returns the dEntity the player is clicking on.
    // <context.item> returns the dItem the player is clicking with.
    // <context.cuboids> returns a dList of cuboids that contain the interacted entity
    //
    // @Determine
    // "CANCELLED" to stop the click from happening.
    //
    // -->
    @EventHandler
    public void playerInteractEntity(PlayerInteractEntityEvent event) {

        String determination;
        dNPC npc = null;

        dItem item = new dItem(event.getPlayer().getItemInHand());
        dEntity entity = new dEntity(event.getRightClicked());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getRightClicked().getLocation()));
        context.put("entity", entity.getDenizenObject());
        context.put("item", item);

        if (entity.isNPC()) npc = entity.getDenizenNPC();

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
            dList cuboid_context = new dList();
            for (dCuboid cuboid : cuboids) {
                events.add("player right clicks entity in " + cuboid.identifySimple());
                events.add("player right clicks entity in cuboid");
                events.add("player right clicks " + entity.identifyType() + " in cuboid");
                events.add("player right clicks " + entity.identifyType() + " in " + cuboid.identifySimple());
                cuboid_context.add(cuboid.identifySimple());
            }
            // Add in cuboids context, if inside a cuboid
            context.put("cuboids", cuboid_context);
        }

        events = EventManager.trimEvents(events);

        if (events.size() == 0) return;

        determination = EventManager.doEvents(events, npc, event.getPlayer(), context, true);

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
    //
    // -->
    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {

        dItem item = new dItem(event.getItem());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("item", item);

        List<String> events = new ArrayList<String>();
        events.add("player consumes " + item.identifySimple());
        events.add("player consumes " + item.identifyMaterial());

        String determination = EventManager.doEvents(events, null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
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

        Player player = event.getPlayer();
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getJoinMessage()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player joins",
                        "player join"),
                null, player, context);

        // Handle message
        if (!determination.equals("none")) {
            event.setJoinMessage(determination);
        }

        // As a tie-in with ScoreboardHelper, make this player view
        // the scoreboard he/she is supposed to view
        if (ScoreboardHelper.viewerMap.containsKey(player.getName())) {
            player.setScoreboard(ScoreboardHelper
                    .getScoreboard(ScoreboardHelper.viewerMap.get(player.getName())));
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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getLeaveMessage()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player kicked"),
                null, event.getPlayer(), context);

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
    // -->
    @EventHandler
    public void playerLeashEntity(PlayerLeashEntityEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        dEntity entity = new dEntity(event.getEntity());

        context.put("entity", entity);
        context.put("holder", new dEntity(event.getLeashHolder()));

        EventManager.doEvents(Arrays.asList
                ("player leashes entity",
                        "entity leashes " + entity.identifyType()),
                null, event.getPlayer(), context, true);
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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("level", new Element(event.getNewLevel()));

        EventManager.doEvents(Arrays.asList
                ("player levels up",
                        "player levels up to " + event.getNewLevel(),
                        "player levels up from " + event.getOldLevel()),
                null, event.getPlayer(), context);
    }

    // <--[event]
    // @Events
    // player logs in
    // player login
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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("hostname", new Element(event.getHostname()));

        if (!dPlayer.offlinePlayers.contains(event.getPlayer()))
            dPlayer.offlinePlayers.add(event.getPlayer());

        String determination = EventManager.doEvents(Arrays.asList
                ("player logs in",
                        "player login"),
                null, event.getPlayer(), context);

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

        String name = dLocation.getSaved(event.getTo());

        if (name != null) {
            Map<String, dObject> context = new HashMap<String, dObject>();
            context.put("notable", new Element(name));

            String determination = EventManager.doEvents(Arrays.asList
                    ("player walks over notable",
                            "player walks over " + name,
                            "walked over notable",
                            "walked over " + name),
                    null, event.getPlayer(), context, true);

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

        String determination = EventManager.doEvents(events, null, event.getPlayer(), context, true);

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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(event.getQuitMessage()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player quits",
                        "player quit"),
                null, event.getPlayer(), context);

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

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getRespawnLocation()));

        List<String> events = new ArrayList<String>();
        events.add("player respawns");

        if (event.isBedSpawn()) events.add("player respawns at bed");
        else                    events.add("player respawns elsewhere");

        String determination = EventManager.doEvents(events, null, event.getPlayer(), context);

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
    // <context.state> returns the dEntity.
    //
    // @Determine
    // "CANCELLED" to stop the player from shearing the entity.
    //
    // -->
    @EventHandler
    public void playerShearEntity(PlayerShearEntityEvent event) {

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

        String determination = EventManager.doEvents(events, null, event.getPlayer(), context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player toggles flight
    // player starts/stops flying
    //
    // @Triggers when a player starts or stops flying.
    // @Context
    // <context.state> returns an Element with a value of "true" if the player is now flying and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the player from toggling flying.
    //
    // -->
    @EventHandler
    public void playerToggleFlight(PlayerToggleFlightEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("state", new Element(event.isFlying()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player toggles flight",
                        "player " + (event.isFlying() ? "starts" : "stops") + " flying"),
                null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player toggles sneak
    // player starts/stops sneaking
    //
    // @Triggers when a player starts or stops sneaking.
    // @Context
    // <context.state> returns an Element with a value of "true" if the player is now sneaking and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the player from toggling sneaking.
    //
    // -->
    @EventHandler
    public void playerToggleSneak(PlayerToggleSneakEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("state", new Element(event.isSneaking()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player toggles sneak",
                        "player " + (event.isSneaking() ? "starts" : "stops") + " sneaking"),
                null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }

    // <--[event]
    // @Events
    // player toggles sprint
    // player starts/stops sprinting
    //
    // @Triggers when a player starts or stops sprinting.
    // @Context
    // <context.state> returns an Element with a value of "true" if the player is now sprinting and "false" otherwise.
    //
    // @Determine
    // "CANCELLED" to stop the player from toggling sprinting.
    //
    // -->
    @EventHandler
    public void playerToggleSprint(PlayerToggleSprintEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("state", new Element(event.isSprinting()));

        String determination = EventManager.doEvents(Arrays.asList
                ("player toggles sprint",
                        "player " + (event.isSprinting() ? "starts" : "stops") + " sprinting"),
                null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }


    /////////////////////
    //   SERVER EVENTS
    /////////////////

    // Shares description with playerCommandPreprocess
    @EventHandler
    public void serverCommand(ServerCommandEvent event) {

        if (event.getCommand().trim().length() == 0)
            return;

        Map<String, dObject> context = new HashMap<String, dObject>();

        String message = event.getCommand();
        String command = event.getCommand().split(" ")[0].replace("/", "").toUpperCase();

        List<String> events = EventManager.trimEvents(Arrays.asList
                ("command",
                        command + " command"));

        if (events.size() == 0)
            return;

        List<String> args = Arrays.asList(aH.buildArgs(message.split(" ").length > 1 ? message.split(" ", 2)[1] : ""));
        List<String> parsed_args = Arrays.asList(aH.buildArgs(event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : ""));

        // Fill context
        context.put("args", new dList(args));
        context.put("parsed_args", new dList(parsed_args));
        context.put("command", new Element(command));
        context.put("raw_args", new Element((message.split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : "")));
        context.put("server", Element.TRUE);

        EventManager.doEvents(events, null, null, context);
    }


    /////////////////////
    //   VEHICLE EVENTS
    /////////////////

    // <--[event]
    // @Events
    // vehicle collides with block
    // vehicle collides with <material>
    // <vehicle> collides with block
    // <vehicle> collides with <material>
    //
    // @Triggers when a vehicle collides with a block.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.location> returns the dLocation of the block.
    //
    // -->
    @EventHandler
    public void vehicleBlockCollision(VehicleBlockCollisionEvent event) {

        // Bukkit seems to be triggering collision on air.. let's filter that out.
        if (event.getBlock().getType() == Material.AIR) return;

        Player player = null;
        dNPC npc = null;

        dEntity vehicle = new dEntity(event.getVehicle());
        dMaterial material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("vehicle", vehicle);
        context.put("location", new dLocation(event.getBlock().getLocation()));

        List<String> events = new ArrayList<String>();
        events.add("vehicle collides with block");
        events.add("vehicle collides with " + material.identifySimple());
        events.add(vehicle.identifyType() + " collides with block");
        events.add(vehicle.identifyType() + " collides with " + material.identifySimple());

        EventManager.doEvents(events, npc, player, context, true);
    }

    // <--[event]
    // @Events
    // vehicle collides with entity
    // vehicle collides with <entity>
    // <vehicle> collides with entity
    // <vehicle> collides with <entity>
    //
    // @Triggers when a vehicle collides with an entity.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    // <context.entity> returns the dEntity of the entity the vehicle has collided with.
    //
    // @Determine
    // "CANCELLED" to stop the collision from happening.
    // "NOPICKUP" to stop the vehicle from picking up the entity.
    //
    // -->
    @EventHandler
    public void vehicleEntityCollision(VehicleEntityCollisionEvent event) {

        Player player = null;
        dNPC npc = null;

        dEntity vehicle = new dEntity(event.getVehicle());
        dEntity entity = new dEntity(event.getEntity());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("vehicle", vehicle);
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        List<String> events = new ArrayList<String>();
        events.add("vehicle collides with entity");
        events.add("vehicle collides with " + entity.identifyType());
        events.add(vehicle.identifyType() + " collides with entity");
        events.add(vehicle.identifyType() + " collides with " + entity.identifyType());

        String determination = EventManager.doEvents(events, npc, player, context, true);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (determination.toUpperCase().startsWith("NOPICKUP"))
            event.setPickupCancelled(true);
    }

    // <--[event]
    // @Events
    // vehicle created
    // <vehicle> created
    //
    // @Triggers when a vehicle is created.
    // @Context
    // <context.vehicle> returns the dEntity of the vehicle.
    //
    // @Determine
    // "CANCELLED" to stop the entity from entering the vehicle.
    //
    // -->
    @EventHandler
    public void vehicleCreate(VehicleCreateEvent event) {

        Map<String, dObject> context = new HashMap<String, dObject>();

        dEntity vehicle = new dEntity(event.getVehicle());

        context.put("vehicle", vehicle);

        EventManager.doEvents(Arrays.asList
                ("vehicle created",
                        vehicle.identifyType() + " created"),
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
    // Element(Double) to set the value of the damage received by the vehicle.
    //
    // -->
    @EventHandler
    public void vehicleDamage(VehicleDamageEvent event) {

        Player player = null;
        dNPC npc = null;

        dEntity vehicle = new dEntity(event.getVehicle());

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("damage", new Element(event.getDamage()));
        context.put("vehicle", vehicle);

        List<String> events = new ArrayList<String>();
        events.add("vehicle damaged");
        events.add(vehicle.identifyType() + " damaged");

        if (event.getAttacker() != null) {

            dEntity entity = new dEntity(event.getAttacker());
            context.put("entity", entity.getDenizenObject());

            if (entity.isNPC()) npc = entity.getDenizenNPC();
            else if (entity.isPlayer()) player = entity.getPlayer();

            events.add("entity damages vehicle");
            events.add("entity damages " + vehicle.identifyType());
            events.add(entity.identifyType() + " damages vehicle");
            events.add(entity.identifyType() + " damages " + vehicle.identifyType());
        }

        String determination = EventManager.doEvents(events, npc, player, context, true);

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

        Player player = null;
        dNPC npc = null;

        dEntity vehicle = new dEntity(event.getVehicle());
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("vehicle", vehicle);

        List<String> events = new ArrayList<String>();
        events.add("vehicle destroyed");
        events.add(vehicle.identifyType() + " destroyed");

        if (event.getAttacker() != null) {

            dEntity entity = new dEntity(event.getAttacker());
            context.put("entity", entity.getDenizenObject());

            if (entity.isNPC()) npc = entity.getDenizenNPC();
            else if (entity.isPlayer()) player = entity.getPlayer();

            events.add("entity destroys vehicle");
            events.add("entity destroys " + vehicle.identifyType());
            events.add(entity.identifyType() + " destroys vehicle");
            events.add(entity.identifyType() + " destroys " + vehicle.identifyType());
        }

        String determination = EventManager.doEvents(events, npc, player, context, true);

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

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();

        dEntity vehicle = new dEntity(event.getVehicle());
        dEntity entity = new dEntity(event.getEntered());

        context.put("vehicle", vehicle);
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        String determination = EventManager.doEvents(Arrays.asList
                ("entity enters vehicle",
                        entity.identifyType() + " enters vehicle",
                        "entity enters " + vehicle.identifyType(),
                        entity.identifyType() + " enters " + vehicle.identifyType()),
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

        Player player = null;
        dNPC npc = null;

        Map<String, dObject> context = new HashMap<String, dObject>();

        dEntity vehicle = new dEntity(event.getVehicle());
        dEntity entity = new dEntity(event.getExited());

        context.put("vehicle", vehicle);
        context.put("entity", entity.getDenizenObject());

        if (entity.isNPC()) npc = entity.getDenizenNPC();
        else if (entity.isPlayer()) player = entity.getPlayer();

        String determination = EventManager.doEvents(Arrays.asList
                ("entity exits vehicle",
                        "entity exits " + vehicle.identifyType(),
                        entity.identifyType() + " exits vehicle",
                        entity.identifyType() + " exits " + vehicle.identifyType()),
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
    // <context.reason> returns the dLocation where the lightning struck.
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

        String determination = EventManager.doEvents(Arrays.asList
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
    // <context.weather> returns an Element with the name of the new weather.
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

        String determination = EventManager.doEvents(events, null, null, context, true);

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
    // <context.reason> returns an Element of the reason the portal was created.
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

        String determination = EventManager.doEvents(Arrays.asList
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

        EventManager.doEvents(Arrays.asList
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

        String determination = EventManager.doEvents(events, null, null, context, true);

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

        EventManager.doEvents(Arrays.asList
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

        EventManager.doEvents(Arrays.asList
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

        EventManager.doEvents(Arrays.asList
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

        EventManager.doEvents(Arrays.asList
                ("world unloads",
                        world.identifySimple() + " unloads"),
                null, null, context, true);
    }
}

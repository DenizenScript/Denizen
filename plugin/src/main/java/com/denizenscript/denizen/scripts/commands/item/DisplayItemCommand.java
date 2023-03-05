package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

public class DisplayItemCommand extends AbstractCommand implements Listener {

    public DisplayItemCommand() {
        setName("displayitem");
        setSyntax("displayitem [<item>] [<location>] (duration:<value>)");
        setRequiredArguments(2, 3);
        Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
        isProcedural = false;
    }

    // <--[command]
    // @Name DisplayItem
    // @Syntax displayitem [<item>] [<location>] (duration:<value>)
    // @Required 2
    // @Maximum 3
    // @Short Makes a non-touchable item spawn for players to view.
    // @Group item
    //
    // @Description
    // This command drops an item at the specified location which cannot be picked up by players.
    //
    // It accepts a duration which determines how long the item will stay for until disappearing.
    // If no duration is specified the item will stay for 1 minute, after which the item will disappear.
    // Use "duration:infinite" to indicate that the item should never remove itself.
    //
    // @Tags
    // <EntityTag.item>
    // <entry[saveName].dropped> returns a EntityTag of the spawned item.
    //
    // @Usage
    // Use to display a stone block dropped at a players location.
    // - displayitem stone <player.location>
    //
    // @Usage
    // Use to display a diamond sword dropped at a relevant location.
    // - displayitem diamond_sword <context.location>
    //
    // @Usage
    // Use to display redstone dust dropped at a related location disappear after 10 seconds.
    // - displayitem redstone <context.location> duration:10s
    //
    // @Usage
    // Use to save the dropped item to save entry 'item_dropped'.
    // - displayitem redstone <context.location> duration:10s save:item_dropped
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        TabCompleteHelper.tabCompleteItems(tab);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesArgumentType(DurationTag.class)
                    && !scriptEntry.hasObject("duration")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (arg.matchesArgumentType(LocationTag.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && !scriptEntry.hasObject("item")) {
                scriptEntry.addObject("item", arg.asType(ItemTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("item")) {
            throw new InvalidArgumentsException("Must specify an item to display.");
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
        if (!scriptEntry.hasObject("duration")) {
            scriptEntry.addObject("duration", new DurationTag(60));
        }
    }

    public final HashSet<UUID> protectedEntities = new HashSet<>();

    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if (protectedEntities.contains(event.getEntity().getUniqueId())
                || protectedEntities.contains(event.getTarget().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemInventoryPickup(InventoryPickupItemEvent event) {
        if (protectedEntities.contains(event.getItem().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemEntityPickup(EntityPickupItemEvent event) {
        if (protectedEntities.contains(event.getItem().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public void onItemDespawn(ItemDespawnEvent event) {
        if (protectedEntities.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> {
                if (event.getEntity().isValid() && !event.getEntity().isDead()) {
                    NMSHandler.entityHelper.setTicksLived(event.getEntity(), -1000);
                }
            }, 1);
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ItemTag item = scriptEntry.getObjectTag("item");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        LocationTag location = scriptEntry.getObjectTag("location");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), item, duration, location);
        }
        // Drop the item
        final Item dropped = location.getWorld().dropItem(location.getBlockLocation().clone().add(0.5, 1.5, 0.5), item.getItemStack());
        dropped.setVelocity(new Vector(0, 0, 0));
        dropped.setGravity(false);
        dropped.setPickupDelay(32767);
        int ticks = duration.getTicksAsInt();
        NMSHandler.entityHelper.setTicksLived(dropped, ticks <= 0 ? -32768 : -ticks);
        if (!dropped.isValid()) {
            Debug.echoDebug(scriptEntry, "Item failed to spawned (likely blocked by some plugin).");
            return;
        }
        final UUID itemUUID = dropped.getUniqueId();
        protectedEntities.add(itemUUID);
        scriptEntry.saveObject("dropped", new EntityTag(dropped));
        if (ticks > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(),
                    () -> {
                        protectedEntities.remove(itemUUID);
                        if (dropped.isValid() && !dropped.isDead()) {
                            dropped.remove();
                        }
                    }, ticks);
        }
    }
}

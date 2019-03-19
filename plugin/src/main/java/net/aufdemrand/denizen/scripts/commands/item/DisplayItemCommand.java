package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

import java.util.HashSet;
import java.util.UUID;


public class DisplayItemCommand extends AbstractCommand implements Listener {

    @Override
    public void onEnable() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_12_R1)) {
            Bukkit.getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesArgumentType(Duration.class)
                    && !scriptEntry.hasObject("duration")) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else if (arg.matchesArgumentType(dLocation.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && !scriptEntry.hasObject("item")) {
                scriptEntry.addObject("item", arg.asType(dItem.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check required args
        if (!scriptEntry.hasObject("item")) {
            throw new InvalidArgumentsException("Must specify an item to display.");
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }

        if (!scriptEntry.hasObject("duration")) {
            scriptEntry.addObject("duration", Duration.valueOf("1m"));
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

    @Override
    public void execute(ScriptEntry scriptEntry) {

        dItem item = (dItem) scriptEntry.getObject("item");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        dLocation location = (dLocation) scriptEntry.getObject("location");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    item.debug()
                            + duration.debug()
                            + location.debug());

        }

        // Drop the item
        final Item dropped = location.getWorld()
                .dropItem(location.getBlock().getLocation().clone().add(0.5, 1.5, 0.5), item.getItemStack());
        dropped.setVelocity(dropped.getVelocity().multiply(0));
        dropped.setPickupDelay(duration.getTicksAsInt() + 1000);
        dropped.setTicksLived(duration.getTicksAsInt() + 1000);
        if (!dropped.isValid()) {
            return;
        }
        final UUID itemUUID = dropped.getUniqueId();
        protectedEntities.add(itemUUID);

        // Remember the item entity
        scriptEntry.addObject("dropped", new dEntity(dropped));

        // Remove it later
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        if (dropped.isValid() && !dropped.isDead()) {
                            dropped.remove();
                            protectedEntities.remove(itemUUID);
                        }
                    }
                }, duration.getTicks());
    }
}

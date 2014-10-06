package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.events.SmartEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemMoveSmartEvent implements SmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on (i@)?\\w+ moves from (in@)?\\w+(to (in@)?\\w+)", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Any match is sufficient
                return true;
            }
        }
        // No matches at all, so return false.
        return false;
    }


    @Override
    public void _initialize() {
        // Yay! Your event is in use! Register it here.
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        // Record that you loaded in the debug.
        dB.log("Loaded Item Move SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        InventoryMoveItemEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // item moves from inventory (to <inventory type>)
    // item moves from <inventory type> (to <inventory type>)
    // <item> moves from inventory (to <inventory type>)
    // <item> moves from <inventory type> (to <inventory type>)
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
}

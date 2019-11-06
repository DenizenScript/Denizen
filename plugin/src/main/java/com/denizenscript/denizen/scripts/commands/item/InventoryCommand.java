package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.AbstractMap;
import java.util.List;

public class InventoryCommand extends AbstractCommand {

    // <--[language]
    // @name Virtual Inventories
    // @group Inventory System
    // @description
    // Virtual inventories are inventories that have no attachment to anything within the world of Minecraft. They can
    // be used for a wide range of purposes - from looting fallen enemies to serving as interactive menus with item
    // 'buttons'.
    //
    // In Denizen, all Notable dInventories (saved by the Note command) are automatically converted into a
    // virtual copy of the saved inventory. This enables you to open and edit the items inside freely, with automatic
    // saving, as if it were a normal inventory.
    //
    // Notables are not the only way to create virtual inventories, however. Using in@generic along with inventory
    // properties will allow you to create temporary custom inventories to do with as you please. The properties that
    // can be used like this are:
    //
    // size=<size>
    // contents=<item>|...
    // title=<title>
    // holder=<inventory type>
    //
    // For example, the following task script opens a virtual inventory with 18 slots, of which the second slot is a
    // snowball, all the rest are empty, and the title is "My Awesome Inventory" with some colors in it.
    // <code>
    // open random inventory:
    //   type: task
    //   script:
    //   - inventory open "d:in@generic[size=18;title=<red>My <green>Awesome <blue>Inventory;contents=li@air|snow_ball]"
    // </code>
    //
    // -->

    // <--[command]
    // @Name Inventory
    // @Syntax inventory [open/close/copy/move/swap/add/remove/set/keep/exclude/fill/clear/update/adjust <mechanism>:<value>] (destination:<inventory>) (origin:<inventory>/<item>|...) (slot:<slot>)
    // @Required 1
    // @Short Edits the inventory of a player, NPC, or chest.
    // @Group item
    //
    // @Description
    // Use this command to edit the state of inventories. By default, the destination inventory
    // is the current attached player's inventory. If you are copying, swapping, removing from
    // (including via "keep" and "exclude"), adding to, moving, or filling inventories, you'll need
    // both destination and origin inventories. Origin inventories may be specified as a list of
    // ItemTags, but destinations must be actual dInventories.
    // Using "open", "clear", or "update" only require a destination. "Update" also requires the
    // destination to be a valid player inventory.
    // Using "close" closes any inventory that the currently attached player has opened.
    //
    // @Tags
    // <PlayerTag.inventory>
    // <PlayerTag.enderchest>
    // <PlayerTag.open_inventory>
    // <NPCTag.inventory>
    // <LocationTag.inventory>
    //
    // @Usage
    // Use to open a chest inventory, at a location.
    // - inventory open d:l@123,123,123,world
    //
    // @Usage
    // Use to open a virtual inventory with a title and some items.
    // - inventory open d:in@generic[size=27;title=BestInventory;contents=li@snow_ball|clay_brick]
    //
    // @Usage
    // Use to open another player's inventory.
    // - inventory open d:<p@calico-kid.inventory>
    //
    // @Usage
    // Use to remove all items from a chest, except any items in the specified list.
    // - inventory keep d:in@location[holder=l@123,123,123,world] o:li@snow_ball|ItemScript
    //
    // @Usage
    // Use to remove items specified in a chest from the current player's inventory, regardless of the item count.
    // - inventory exclude origin:l@123,123,123,world
    //
    // @Usage
    // Use to swap two players' inventories.
    // - inventory swap d:in@player[holder=p@bob] o:<p@joe.inventory>
    //
    // @Usage
    // Use to adjust a specific item in the player's inventory.
    // - inventory adjust slot:5 "lore:Item modified!"
    // -->

    private enum Action {OPEN, CLOSE, COPY, MOVE, SWAP, ADD, REMOVE, SET, KEEP, EXCLUDE, FILL, CLEAR, UPDATE, ADJUST}

    @SuppressWarnings("unchecked")
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean isAdjust = false;

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            // Check for a ListTag of actions
            if (!scriptEntry.hasObject("actions")
                    && arg.matchesEnumList(Action.values())) {
                scriptEntry.addObject("actions", arg.asType(ListTag.class).filter(Action.values()));
                isAdjust = arg.toString().equalsIgnoreCase("adjust");
            }

            // Check for an origin, which can be a InventoryTag, EntityTag, LocationTag
            // or a ListTag of ItemTags
            else if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "items", "item", "i", "from", "f")
                    && (arg.matchesArgumentTypes(InventoryTag.class, EntityTag.class, LocationTag.class)
                    || arg.matchesArgumentList(ItemTag.class))) {
                scriptEntry.addObject("origin", Conversion.getInventory(arg, scriptEntry));
            }

            // Check for a destination, which can be a InventoryTag, EntityTag
            // or LocationTag
            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesPrefix("destination", "dest", "d", "target", "to", "t")
                    && arg.matchesArgumentTypes(InventoryTag.class, EntityTag.class, LocationTag.class)) {
                scriptEntry.addObject("destination", Conversion.getInventory(arg, scriptEntry));
            }

            // Check for specified slot number
            else if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot, s")) {
                scriptEntry.addObject("slot", arg.asElement());
            }

            else if (!scriptEntry.hasObject("mechanism")
                    && isAdjust) {
                if (arg.hasPrefix()) {
                    scriptEntry.addObject("mechanism", new ElementTag(arg.getPrefix().getValue()));
                    scriptEntry.addObject("mechanism_value", arg.asElement());
                }
                else {
                    scriptEntry.addObject("mechanism", arg.asElement());
                }
            }

            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("actions")) {
            throw new InvalidArgumentsException("Must specify an Inventory action!");
        }

        if (isAdjust && !scriptEntry.hasObject("mechanism")) {
            throw new InvalidArgumentsException("Inventory adjust must have a mechanism!");
        }

        if (isAdjust && !scriptEntry.hasObject("slot")) {
            throw new InvalidArgumentsException("Inventory adjust must have an explicit slot!");
        }

        scriptEntry.defaultObject("slot", new ElementTag(1));

        scriptEntry.defaultObject("destination",
                Utilities.entryHasPlayer(scriptEntry) ?
                        new AbstractMap.SimpleEntry<>(0,
                                Utilities.getEntryPlayer(scriptEntry).getDenizenEntity().getInventory()) : null);

        if (!scriptEntry.hasObject("destination")) {
            throw new InvalidArgumentsException("Must specify a Destination Inventory!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<String> actions = (List<String>) scriptEntry.getObject("actions");
        AbstractMap.SimpleEntry<Integer, InventoryTag> originentry = (AbstractMap.SimpleEntry<Integer, InventoryTag>) scriptEntry.getObject("origin");
        InventoryTag origin = originentry != null ? originentry.getValue() : null;
        AbstractMap.SimpleEntry<Integer, InventoryTag> destinationentry = (AbstractMap.SimpleEntry<Integer, InventoryTag>) scriptEntry.getObject("destination");
        InventoryTag destination = destinationentry.getValue();
        ElementTag slot = scriptEntry.getElement("slot");
        ElementTag mechanism = scriptEntry.getElement("mechanism");
        ElementTag mechanismValue = scriptEntry.getElement("mechanism_value");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("actions", actions.toString())
                            + (destination.debug())
                            + (origin != null ? origin.debug() : "")
                            + (mechanism != null ? mechanism.debug() : "")
                            + (mechanismValue != null ? mechanismValue.debug() : "")
                            + slot.debug());
        }

        int slotId = SlotHelper.nameToIndex(slot.asString());
        if (slotId < 0) {
            if (slotId == -1) {
                Debug.echoError(scriptEntry.getResidingQueue(), "The input '" + slot.asString() + "' is not a valid slot (unrecognized)!");
            }
            else {
                Debug.echoError(scriptEntry.getResidingQueue(), "The input '" + slot.asString() + "' is not a valid slot (negative values are invalid)!");
            }
            return;
        }

        for (String action : actions) {
            switch (Action.valueOf(action.toUpperCase())) {

                // Make the attached player open the destination inventory
                case OPEN:
                    // Use special method to make opening workbenches work properly
                    if (destination.getIdType().equals("workbench")
                            || destination.getIdHolder().equalsIgnoreCase("workbench")) {
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity()
                                .openWorkbench(null, true);
                    }
                    // Otherwise, open inventory as usual
                    else {
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().openInventory(destination.getInventory());
                    }
                    break;

                // Make the attached player close any open inventory
                case CLOSE:
                    Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().closeInventory();
                    break;

                // Turn destination's contents into a copy of origin's
                case COPY:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    origin.replace(destination);
                    break;

                // Copy origin's contents to destination, then empty origin
                case MOVE:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    origin.replace(destination);
                    origin.clear();
                    break;

                // Swap the contents of the two inventories
                case SWAP:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    InventoryTag temp = new InventoryTag(destination.getInventory());
                    origin.replace(destination);
                    temp.replace(origin);
                    break;

                // Add origin's contents to destination
                case ADD:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.add(slotId, origin.getContents());
                    break;

                // Remove origin's contents from destination
                case REMOVE:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.remove(origin.getContents());
                    break;

                // Set items by slot
                case SET:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.setSlots(slotId, origin.getContents(), originentry.getKey());
                    break;

                // Keep only items from the origin's contents in the
                // destination
                case KEEP:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.keep(origin.getContents());
                    break;

                // Exclude all items from the origin's contents in the
                // destination
                case EXCLUDE:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.exclude(origin.getContents());
                    break;

                // Add origin's contents over and over to destination
                // until it is full
                case FILL:
                    if (origin == null) {
                        Debug.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.fill(origin.getContents());
                    break;

                // Clear the content of the destination inventory
                case CLEAR:
                    destination.clear();
                    break;

                // If this is a player inventory, update it
                case UPDATE:
                    if (!destination.update()) {
                        Debug.echoError("Only player inventories can be force-updated!");
                    }
                    break;

                case ADJUST:
                    ItemTag toAdjust = new ItemTag(destination.getInventory().getItem(slotId));
                    toAdjust.safeAdjust(new Mechanism(mechanism, mechanismValue, scriptEntry.entryData.getTagContext()));
                    NMSHandler.getItemHelper().setInventoryItem(destination.getInventory(), toAdjust.getItemStack(), slotId);
                    break;
            }
        }
    }
}

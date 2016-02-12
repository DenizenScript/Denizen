package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

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
    //   - inventory open "d:in@generic[size=18;title=<red>My <green>Awesome <blue>Inventory;contents=li@i@air|i@snow_ball]"
    // </code>
    //
    // -->

    private enum Action {OPEN, CLOSE, COPY, MOVE, SWAP, ADD, REMOVE, SET, KEEP, EXCLUDE, FILL, CLEAR, UPDATE}

    @SuppressWarnings("unchecked")
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // Check for a dList of actions
            if (arg.matchesEnumList(Action.values())) {
                scriptEntry.addObject("actions", arg.asType(dList.class).filter(Action.values()));
            }

            // Check for an origin, which can be a dInventory, dEntity, dLocation
            // or a dList of dItems
            else if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "items", "item", "i", "from", "f")
                    && (arg.matchesArgumentTypes(dInventory.class, dEntity.class, dLocation.class)
                    || arg.matchesArgumentList(dItem.class))) {
                scriptEntry.addObject("origin", Conversion.getInventory(arg, scriptEntry));
            }

            // Check for a destination, which can be a dInventory, dEntity
            // or dLocation
            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesPrefix("destination", "dest", "d", "target", "to", "t")
                    && arg.matchesArgumentTypes(dInventory.class, dEntity.class, dLocation.class)) {
                scriptEntry.addObject("destination", Conversion.getInventory(arg, scriptEntry));
            }

            // Check for specified slot number
            else if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot, s")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("slot", arg.asElement());
            }

            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("actions")) {
            throw new InvalidArgumentsException("Must specify an Inventory action!");
        }

        scriptEntry.defaultObject("slot", new Element(1)).defaultObject("destination",
                ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity().getInventory() : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<String> actions = (List<String>) scriptEntry.getObject("actions");
        AbstractMap.SimpleEntry<Integer, dInventory> originentry = (AbstractMap.SimpleEntry<Integer, dInventory>) scriptEntry.getObject("origin");
        dInventory origin = originentry.getValue();
        AbstractMap.SimpleEntry<Integer, dInventory> destinationentry = (AbstractMap.SimpleEntry<Integer, dInventory>) scriptEntry.getObject("destination");
        dInventory destination = destinationentry.getValue();
        Element slot = scriptEntry.getElement("slot");

        dB.report(scriptEntry, getName(),
                aH.debugObj("actions", actions.toString())
                        + (destination.debug())
                        + (origin != null ? origin.debug() : "")
                        + slot.debug());

        for (String action : actions) {
            switch (Action.valueOf(action.toUpperCase())) {

                // Make the attached player open the destination inventory
                case OPEN:
                    // Use special method to make opening workbenches work properly
                    if (destination.getIdType().equals("workbench")
                            || destination.getIdHolder().equalsIgnoreCase("workbench")) {
                        ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity()
                                .openWorkbench(null, true);
                    }
                    // Otherwise, open inventory as usual
                    else {
                        ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity().openInventory(destination.getInventory());
                    }
                    break;

                // Make the attached player close any open inventory
                case CLOSE:
                    ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity().closeInventory();
                    break;

                // Turn destination's contents into a copy of origin's
                case COPY:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    origin.replace(destination);
                    break;

                // Copy origin's contents to destination, then empty origin
                case MOVE:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    origin.replace(destination);
                    origin.clear();
                    break;

                // Swap the contents of the two inventories
                case SWAP:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    dInventory temp = new dInventory(destination.getInventory());
                    origin.replace(destination);
                    temp.replace(origin);
                    break;

                // Add origin's contents to destination
                case ADD:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.add(slot.asInt() - 1, origin.getContents());
                    break;

                // Remove origin's contents from destination
                case REMOVE:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.remove(origin.getContents());
                    break;

                // Set items by slot
                case SET:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.setSlots(slot.asInt() - 1, origin.getContents(), originentry.getKey());
                    break;

                // Keep only items from the origin's contents in the
                // destination
                case KEEP:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.keep(origin.getContents());
                    break;

                // Exclude all items from the origin's contents in the
                // destination
                case EXCLUDE:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
                        return;
                    }
                    destination.exclude(origin.getContents());
                    break;

                // Add origin's contents over and over to destination
                // until it is full
                case FILL:
                    if (origin == null) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Missing origin argument!");
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
                        dB.echoError("Only player inventories can be force-updated!");
                    }
                    break;

            }
        }
    }
}

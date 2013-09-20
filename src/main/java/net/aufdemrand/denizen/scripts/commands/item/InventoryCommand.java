package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Lets you store and edit inventories.
 *
 * @author David Cernat
 */

public class InventoryCommand extends AbstractCommand {

    private enum Action { OPEN, COPY, MOVE, SWAP, ADD, REMOVE, KEEP, EXCLUDE, FILL, CLEAR }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                // add Action
                scriptEntry.addObject("action", new Element(arg.getValue().toUpperCase()));
            }

            else if (!scriptEntry.hasObject("originEntity") &&
                !scriptEntry.hasObject("originLocation") &&
                !scriptEntry.hasObject("originInventory") &&
                arg.matchesPrefix("origin, o, source, s, items, item, i, from, f")) {

                // Is entity
                if (arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("originEntity", arg.asType(dEntity.class));
                // Is location
                else if (arg.matchesArgumentType(dLocation.class))
                    scriptEntry.addObject("originLocation", arg.asType(dLocation.class));
                // Is inventory
                else if (arg.matchesArgumentType(dInventory.class))
                    scriptEntry.addObject("originInventory", arg.asType(dInventory.class));
            }

            else if (!scriptEntry.hasObject("destinationEntity") &&
                     !scriptEntry.hasObject("destinationLocation") &&
                     !scriptEntry.hasObject("destinationInventory") &&
                     arg.matchesPrefix("destination, dest, d, target, to, t")) {

                // Is entity
                if (arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("destinationEntity", arg.asType(dEntity.class));
                // Is location
                else if (arg.matchesArgumentType(dLocation.class))
                    scriptEntry.addObject("destinationLocation", arg.asType(dLocation.class));
                // Is inventory
                else if (arg.matchesArgumentType(dInventory.class))
                    scriptEntry.addObject("destinationInventory", arg.asType(dInventory.class));
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify an Inventory action!");

        if (!scriptEntry.hasObject("destinationEntity") &&
            !scriptEntry.hasObject("destinationLocation") &&
            !scriptEntry.hasObject("destinationInventory")) {
            if (scriptEntry.hasPlayer())
                scriptEntry.addObject("destinationInventory", new dInventory(scriptEntry.getPlayer().getPlayerEntity()));
            else
                throw new InvalidArgumentsException("Must specify a destination inventory!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        Element action = scriptEntry.getElement("action");

        dEntity originEntity = (dEntity) scriptEntry.getObject("originEntity");
        dLocation originLocation = (dLocation) scriptEntry.getObject("originLocation");

        dEntity destinationEntity = (dEntity) scriptEntry.getObject("destinationEntity");
        dLocation destinationLocation = (dLocation) scriptEntry.getObject("destinationLocation");

        dInventory origin = (dInventory) scriptEntry.getObject("originInventory");
        dInventory destination = (dInventory) scriptEntry.getObject("destinationInventory");

        if (origin == null) {
            if (originLocation != null) {
                origin = new dInventory(originLocation.getBlock().getState());
            }
            else if (originEntity != null) {
                origin = new dInventory(originEntity.getLivingEntity());
            }
        }

        if (destination == null) {
            if (destinationLocation != null) {
                destination = new dInventory(destinationLocation.getBlock().getState());
            }
            else if (destinationEntity != null) {
                destination = new dInventory(destinationEntity.getLivingEntity());
            }
            else {
                return;
            }
        }

        dB.report(getName(),
                destination.debug()
                + (origin != null?origin.debug():"")
                + action.debug());

        switch (Action.valueOf(action.asString())) {

            // Make the attached player open the destination inventory
            case OPEN:
                scriptEntry.getPlayer().getPlayerEntity().openInventory(destination.getInventory());
                return;

            // Turn destination's contents into a copy of origin's
            case COPY:
                origin.replace(destination);
                return;

            // Copy origin's contents to destination, then empty origin
            case MOVE:
                origin.replace(destination);
                origin.clear();
                return;

            // Swap the contents of the two inventories
            case SWAP:
                dInventory temp = new dInventory(destination.getInventoryType())
                                      .add(destination.getContents());
                origin.replace(destination);
                temp.replace(origin);
                return;

            // Add origin's contents to destination
            case ADD:
                destination.add(origin.getContents());
                return;

            // Remove origin's contents from destination
            case REMOVE:
                destination.remove(origin.getContents());
                return;

            // Keep only items from the origin's contents in the
            // destination
            case KEEP:
                   destination.keep(origin.getContents());
                   return;

            // Exclude all items from the origin's contents in the
            // destination
            case EXCLUDE:
                   destination.exclude(origin.getContents());
                   return;

            // Add origin's contents over and over to destination
            // until it is full
            case FILL:
                destination.fill(origin.getContents());
                   return;

            // Clear the content of the destination inventory
            case CLEAR:
                destination.clear();
                return;

            default:
                return;

        }



    }
}

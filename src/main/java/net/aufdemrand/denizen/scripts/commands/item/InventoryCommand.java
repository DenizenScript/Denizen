package net.aufdemrand.denizen.scripts.commands.item;

import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Lets you store and edit inventories.
 *
 * @author David Cernat
 */

public class InventoryCommand extends AbstractCommand {

    private enum Action { OPEN, COPY, MOVE, SWAP, ADD, REMOVE, KEEP, EXCLUDE, FILL, CLEAR }

    @SuppressWarnings("unchecked")
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // Check for a dList of actions
            if (arg.matchesEnumList(Action.values())) {
                scriptEntry.addObject("actions", ((dList) arg.asType(dList.class)).filter(Action.values()));
            }

            // Check for an origin, which can be a dInventory, dEntity, dLocation
            // or a dList of dItems
            else if (!scriptEntry.hasObject("origin")
                     && arg.matchesPrefix("origin, o, source, s, items, item, i, from, f")
                     && (arg.matchesArgumentTypes(dInventory.class, dEntity.class, dLocation.class)
                         || arg.matchesArgumentList(dItem.class))) {
                scriptEntry.addObject("origin", Conversion.getInventory(arg));
            }

            // Check for a destination, which can be a dInventory, dEntity
            // or dLocation
            else if (!scriptEntry.hasObject("destination")
                     && arg.matchesPrefix("destination, dest, d, target, to, t")
                     && arg.matchesArgumentTypes(dInventory.class, dEntity.class, dLocation.class)) {
                scriptEntry.addObject("destination", Conversion.getInventory(arg));
            }

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("actions"))
            throw new InvalidArgumentsException("Must specify an Inventory action!");

        scriptEntry.defaultObject("destination",
                scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getDenizenEntity().getInventory() : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<String> actions = (List<String>) scriptEntry.getObject("actions");
        dInventory origin = (dInventory) scriptEntry.getObject("origin");
        dInventory destination = (dInventory) scriptEntry.getObject("destination");

        dB.report(getName(),
                aH.debugObj("actions", actions.toString()) +
                destination.debug()
                + (origin != null ? origin.debug() : ""));

        for (String action : actions) {
            switch (Action.valueOf(action.toUpperCase())) {

                // Make the attached player open the destination inventory
                case OPEN:
                    // Use special method to make opening workbenches work properly
                    if (destination.getIdHolder().equalsIgnoreCase("workbench")) {
                        scriptEntry.getPlayer().getPlayerEntity()
                            .openWorkbench(destination.getLocation(), true);
                    }
                    // Otherwise, open inventory as usual
                    else scriptEntry.getPlayer().getPlayerEntity().openInventory(destination.getInventory());
                    break;

                // Turn destination's contents into a copy of origin's
                case COPY:
                    origin.replace(destination);
                    break;

                // Copy origin's contents to destination, then empty origin
                case MOVE:
                    origin.replace(destination);
                    origin.clear();
                    break;

                // Swap the contents of the two inventories
                case SWAP:
                    dInventory temp = new dInventory(destination.getInventoryType())
                                              .add(destination.getContents());
                    origin.replace(destination);
                    temp.replace(origin);
                    break;

                // Add origin's contents to destination
                case ADD:
                    destination.add(origin.getContents());
                    break;

                // Remove origin's contents from destination
                case REMOVE:
                    destination.remove(origin.getContents());
                    break;

                // Keep only items from the origin's contents in the
                // destination
                case KEEP:
                    destination.keep(origin.getContents());
                    break;

                // Exclude all items from the origin's contents in the
                // destination
                case EXCLUDE:
                    destination.exclude(origin.getContents());
                    break;

                // Add origin's contents over and over to destination
                // until it is full
                case FILL:
                    destination.fill(origin.getContents());
                    break;

                // Clear the content of the destination inventory
                case CLEAR:
                    destination.clear();
                    break;
            }
        }
    }
}

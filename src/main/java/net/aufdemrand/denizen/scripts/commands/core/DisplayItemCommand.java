package net.aufdemrand.denizen.scripts.commands.core;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.arguments.Duration;
import net.aufdemrand.denizen.arguments.dItem;
import net.aufdemrand.denizen.arguments.dLocation;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Displays an item in the world. This item will not disappear (unless set to)
 * and cannot be picked up.
 *
 * @author aufdemrand
 */

public class DisplayItemCommand extends AbstractCommand {

    private enum Action { PLACE, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        dItem item = null;
        Action action = Action.PLACE;
        Duration duration = null;
        dLocation location = null;

        // Make sure NPC is available
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesDuration(arg))
                duration = aH.getDurationFrom(arg);

            else if (aH.matchesArg("REMOVE", arg))
                action = Action.REMOVE;

            else if (aH.matchesLocation(arg))
                location = aH.getLocationFrom(arg);

            else if (aH.getItemFrom(arg) != null)
                item = aH.getItemFrom(arg);

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Check required args
        if (item == null)
            throw new InvalidArgumentsException("Must specify an item to display.");

        if (location == null)
            throw new InvalidArgumentsException(Messages.DEBUG_SET_LOCATION);

        // Add objects to ScriptEntry for execution
        scriptEntry.addObject("item", item)
                .addObject("action", action)
                .addObject("duration", duration)
                .addObject("location", location);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects from ScriptEntry
        dItem item = (dItem) scriptEntry.getObject("item");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Action action = (Action) scriptEntry.getObject("action");

        // Report to dB
        dB.report(getName(), aH.debugObj("Action", action.toString())
                + item.debug()
                + (duration != null ? duration.debug() : "")
                + location.debug());

        if (action == Action.PLACE) {

            int ticks = Integer.MAX_VALUE;
            if (duration != null) ticks = duration.getTicksAsInt();

            // Display the item
            if (displayed.containsKey(location.dScriptArgValue())) {
                displayed.get(location.dScriptArgValue()).remove();
                displayed.remove(location.dScriptArgValue());
            }

            // Remember the item entity
            displayed.put(location.dScriptArgValue(), location.getBlock().getLocation().add(0, 1, 0).getWorld().dropItem(location, item.getItemStack()));
            displayed.get(location.dScriptArgValue()).setPickupDelay(Integer.MAX_VALUE);
            displayed.get(location.dScriptArgValue()).setTicksLived(ticks);
        }

        // Remove the item
        else if (action == Action.REMOVE) {
            if (displayed.containsKey(location.dScriptArgValue())) {
                displayed.get(location.dScriptArgValue()).remove();
                displayed.remove(location.dScriptArgValue());
            }

        }
    }

    public static Map<String, org.bukkit.entity.Item> displayed = new HashMap<String, org.bukkit.entity.Item>();

}
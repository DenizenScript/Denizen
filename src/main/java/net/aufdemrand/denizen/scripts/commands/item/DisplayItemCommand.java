package net.aufdemrand.denizen.scripts.commands.item;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Displays an item in the world. This item will not disappear (unless set to)
 * and cannot be picked up.
 *
 * @author aufdemrand, mcmonkey
 */

public class DisplayItemCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (arg.matchesArgumentType(dItem.class))
                scriptEntry.addObject("item", arg.asType(dItem.class));

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        // Check required args
        if (!scriptEntry.hasObject("item"))
            throw new InvalidArgumentsException("Must specify an item to display.");

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException(Messages.DEBUG_SET_LOCATION);

        if (!scriptEntry.hasObject("duration"))
            scriptEntry.addObject("duration", Duration.valueOf("1m"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects from ScriptEntry
        dItem item = (dItem) scriptEntry.getObject("item");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        dLocation location = (dLocation) scriptEntry.getObject("location");

        // Report to dB
        dB.report(getName(),
                item.debug()
                + duration.debug()
                + location.debug());

        // Drop the item
        final Item dropped = location.getBlock().getLocation().add(0, 1, 0).getWorld().dropItem(location, item.getItemStack());
        dropped.setPickupDelay(Integer.MAX_VALUE);
        dropped.setTicksLived(Integer.MAX_VALUE);

        // Remember the item entity
        scriptEntry.addObject("dropped", new dEntity(dropped));

        // Remove it later
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        if (dropped.isValid() && !dropped.isDead()) {
                            dropped.remove();
                        }
                    }
                }, duration.getTicks());
    }


}

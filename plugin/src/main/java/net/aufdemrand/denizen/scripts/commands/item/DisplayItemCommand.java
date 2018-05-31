package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;


public class DisplayItemCommand extends AbstractCommand {

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

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dItem item = (dItem) scriptEntry.getObject("item");
        Duration duration = (Duration) scriptEntry.getObject("duration");
        dLocation location = (dLocation) scriptEntry.getObject("location");

        dB.report(scriptEntry, getName(),
                item.debug()
                        + duration.debug()
                        + location.debug());

        // Drop the item
        final Item dropped = location.getWorld()
                .dropItem(location.getBlock().getLocation().clone().add(0.5, 1.5, 0.5), item.getItemStack());
        dropped.setVelocity(dropped.getVelocity().multiply(0));
        dropped.setPickupDelay(duration.getTicksAsInt() + 1000);
        dropped.setTicksLived(duration.getTicksAsInt() + 1000);

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

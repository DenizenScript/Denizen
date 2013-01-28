package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

/**
 * Drops items or experience in a location.
 *
 * @author Jeremy Schroeder
 */

public class DropCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        Item item = null;
        Integer qty = null;
        Location location = null;
        Boolean exp = false;

        // Set some defaults
        if (scriptEntry.getPlayer() != null)
            location = new Location(scriptEntry.getPlayer().getLocation());
        if (location == null && scriptEntry.getNPC() != null)
            location = new Location(scriptEntry.getNPC().getLocation());

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesItem(arg)) {
                item = aH.getItemFrom(arg);

            } else if (aH.matchesArg("XP, EXP", arg)) {
                exp = true;

            } else if (aH.matchesQuantity(arg)) {
                qty = aH.getIntegerFrom(arg);

            } else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (item == null && !exp) throw new InvalidArgumentsException(Messages.ERROR_INVALID_ITEM);
        if (location == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION);

        // Stash objects
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("item", item);
        scriptEntry.addObject("exp", exp);
        scriptEntry.addObject("qty", qty);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Location location = (Location) scriptEntry.getObject("location");
        Integer qty = (Integer) scriptEntry.getObject("qty");
        Boolean exp = (Boolean) scriptEntry.getObject("exp");
        Item item = (Item) scriptEntry.getObject("item");

        // Set quantity if not specified
        if (qty != null && item != null)
            item.setAmount(qty);
        else qty = 1;

        // Report to dB
        dB.report(getName(),
                location.debug()
                + (item != null ? item.debug()
                : aH.debugObj("Exp", String.valueOf(qty))));

        if (exp)
            ((ExperienceOrb) location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB))
                    .setExperience(qty);
        else
            location.getWorld().dropItemNaturally(location, item);

    }

}
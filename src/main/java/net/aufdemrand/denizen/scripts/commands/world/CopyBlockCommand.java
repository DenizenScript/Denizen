package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Copies a block to another location, keeping all special
 * data all about it.
 *
 * @author aufdemrand, David Cernat
 */

public class CopyBlockCommand extends AbstractCommand{

    @Override
    public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {

        boolean remove_original = false;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // CopyBlock can move a single 'location' ...
            if (arg.matchesArgumentType(dLocation.class)
                    && !scriptEntry.hasObject("location")
                    && !arg.matchesPrefix("t, to"))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

                // ... or and entire cuboid ...
            else if (arg.matchesArgumentType(dCuboid.class)
                    && !scriptEntry.hasObject("cuboid"))
                scriptEntry.addObject("cuboid", arg.asType(dCuboid.class));

                // ... to a location.
            else if (arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("t, to"))
                scriptEntry.addObject("destination", arg.asType(dLocation.class));

            else if (arg.matches("and_remove"))
                scriptEntry.addObject("remove", Element.TRUE);

            else arg.reportUnhandled();
        }

        // Check required arguments
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("cuboid"))
            throw new InvalidArgumentsException("Must specify a source loaction or cuboid.");

        if (!scriptEntry.hasObject("destination"))
            throw new InvalidArgumentsException("Must specify a destination location.");

        // Set defaults
        scriptEntry.defaultObject("remove", Element.FALSE);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation copy_location = (dLocation) scriptEntry.getObject("location");
        dLocation destination = (dLocation) scriptEntry.getObject("destination");
        dCuboid copy_cuboid = (dCuboid) scriptEntry.getObject("cuboid");
        Element remove_original = (Element) scriptEntry.getObject("remove");


        List<Location> locations = new ArrayList<Location>();

        if (copy_location != null) locations.add(copy_location);
        else if (copy_cuboid != null) locations.addAll(copy_cuboid.getBlockLocations());


        for (Location loc : locations) {

            Block source = copy_location.getBlock();
            BlockState sourceState = source.getState();
            Block update = destination.getBlock();

            update.setTypeIdAndData(source.getTypeId(), source.getData(), false);

            BlockState updateState = update.getState();

            // Note: only a BlockState, not a Block, is actually an instance
            // of InventoryHolder
            if (sourceState instanceof InventoryHolder) {

                ((InventoryHolder) updateState).getInventory()
                        .setContents(((InventoryHolder) sourceState).getInventory().getContents());
            }
            else if (sourceState instanceof Sign) {

                int n = 0;

                for (String line : ((Sign) sourceState).getLines()) {

                    ((Sign) updateState).setLine(n, line);
                    n++;
                }

                updateState.update();
            }


            // TODO: Account for Noteblock, Skull, Jukebox

        }
    }
}

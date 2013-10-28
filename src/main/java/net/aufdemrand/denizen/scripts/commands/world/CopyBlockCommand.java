package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;

/**
 * Copies a block to another location, keeping all special
 * data all about it.
 *
 * @author aufdemrand, David Cernat
 */

public class CopyBlockCommand extends AbstractCommand{

    @Override
    public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {

        dLocation copy_location = null;
        dLocation destination = null;
        boolean remove_original = false;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesLocation(arg))
                copy_location = aH.getLocationFrom(arg);

            else if (aH.matchesValueArg("to", arg, ArgumentType.Location))
                destination = aH.getLocationFrom(arg);

            else if (aH.matchesArg("and_remove", arg))
                remove_original = true;

            else
                dB.echoError("Unknown argument '" + arg + "'");
        }

        if (copy_location == null || destination == null)
            throw  new InvalidArgumentsException("Missing location argument!");

        scriptEntry.addObject("copy_location", copy_location)
                .addObject("destination", destination)
                .addObject("remove_original", remove_original);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation copy_location = (dLocation) scriptEntry.getObject("copy_location");
        dLocation destination = (dLocation) scriptEntry.getObject("destination");
        Boolean remove_original = (Boolean) scriptEntry.getObject("remove_original");

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

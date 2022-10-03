package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.blocks.FullBlockData;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class CopyBlockCommand extends AbstractCommand {

    public CopyBlockCommand() {
        setName("copyblock");
        setSyntax("copyblock [<location>] [to:<location>] (remove_original)");
        setRequiredArguments(2, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name CopyBlock
    // @Syntax copyblock [<location>] [to:<location>] (remove_original)
    // @Required 2
    // @Maximum 3
    // @Short Copies a block to another location, keeping metadata when possible.
    // @Group world
    //
    // @Description
    // Copies a block to another location.
    // You may also use the 'remove_original' argument to delete the original block.
    // This effectively moves the block to the target location.
    //
    // @Tags
    // <LocationTag.material>
    //
    // @Usage
    // Use to copy the block the player is looking at to their current location
    // - copyblock <player.cursor_on> to:<player.location>
    //
    // @Usage
    // Use to move the block the player is looking at to their current location (removing it from its original location)
    // - copyblock <player.cursor_on> to:<player.location> remove_original
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesArgumentType(LocationTag.class)
                    && !scriptEntry.hasObject("location")
                    && !arg.matchesPrefix("t", "to")) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (arg.matchesArgumentType(LocationTag.class)
                    && arg.matchesPrefix("t", "to")) {
                scriptEntry.addObject("destination", arg.asType(LocationTag.class));
            }
            else if (arg.matches("remove_original")) {
                scriptEntry.addObject("remove", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a source location.");
        }
        if (!scriptEntry.hasObject("destination")) {
            throw new InvalidArgumentsException("Must specify a destination location.");
        }
        // Set defaults
        scriptEntry.defaultObject("remove", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag copy_location = scriptEntry.getObjectTag("location");
        LocationTag destination = scriptEntry.getObjectTag("destination");
        ElementTag remove_original = scriptEntry.getElement("remove");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), copy_location, destination, remove_original);
        }
        List<Location> locations = new ArrayList<>();
        if (copy_location != null) {
            locations.add(copy_location);
        }
        for (Location loc : locations) {
            Block source = loc.getBlock();
            BlockState sourceState = source.getState();
            Block update = destination.getBlock();
            FullBlockData block = new FullBlockData(source);
            block.set(update, false);
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
                    PaperAPITools.instance.setSignLine(((Sign) updateState), n++, line);
                }
            }
            else if (sourceState instanceof Skull) {
                ((Skull) updateState).setSkullType(((Skull) sourceState).getSkullType());
                ((Skull) updateState).setOwner(((Skull) sourceState).getOwner());
                ((Skull) updateState).setRotation(((Skull) sourceState).getRotation());
            }
            else if (sourceState instanceof Jukebox) {
                ((Jukebox) updateState).setPlaying(((Jukebox) sourceState).getPlaying());
            }
            else if (sourceState instanceof Banner) {
                ((Banner) updateState).setBaseColor(((Banner) sourceState).getBaseColor());
                ((Banner) updateState).setPatterns(((Banner) sourceState).getPatterns());
            }
            else if (sourceState instanceof CommandBlock) {
                ((CommandBlock) updateState).setName(((CommandBlock) sourceState).getName());
                ((CommandBlock) updateState).setCommand(((CommandBlock) sourceState).getCommand());
            }
            else if (sourceState instanceof CreatureSpawner) {
                ((CreatureSpawner) updateState).setSpawnedType(((CreatureSpawner) sourceState).getSpawnedType());
                ((CreatureSpawner) updateState).setDelay(((CreatureSpawner) sourceState).getDelay());
            }
            updateState.update();
            if (remove_original.asBoolean()) {
                loc.getBlock().setType(Material.AIR);
            }
        }
    }
}

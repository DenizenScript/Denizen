package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class CopyBlockCommand extends AbstractCommand {

    // <--[command]
    // @Name CopyBlock
    // @Syntax copyblock [<location>/<cuboid>] [to:<location>] (remove_original)
    // @Required 1
    // @Short Copies a block or cuboid to another location, keeping metadata when possible.
    // @Group world
    //
    // @Description
    // Copies a block or cuboid to another location.
    // You may also use the 'remove_original' argument to delete the original block.
    // This effectively moves the block to the target location.
    //
    // @Tags
    // <LocationTag.material>
    //
    // @Usage
    // Use to copy the block the player is looking at to their current location
    // - copyblock <player.location.cursor_on> to:<player.location>
    //
    // @Usage
    // Use to move the block the player is looking at to their current location (removing it from its original location)
    // - copyblock <player.location.cursor_on> to:<player.location> remove_original
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            // CopyBlock can move a single 'location' ...
            if (arg.matchesArgumentType(LocationTag.class)
                    && !scriptEntry.hasObject("location")
                    && !arg.matchesPrefix("t", "to")) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }

            // ... or and entire cuboid ...
            else if (arg.matchesArgumentType(CuboidTag.class)
                    && !scriptEntry.hasObject("cuboid")) {
                scriptEntry.addObject("cuboid", arg.asType(CuboidTag.class));
            }

            // ... to a location.
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

        // Check required arguments
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("cuboid")) {
            throw new InvalidArgumentsException("Must specify a source location or cuboid.");
        }

        if (!scriptEntry.hasObject("destination")) {
            throw new InvalidArgumentsException("Must specify a destination location.");
        }

        // Set defaults
        scriptEntry.defaultObject("remove", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        LocationTag copy_location = (LocationTag) scriptEntry.getObject("location");
        LocationTag destination = (LocationTag) scriptEntry.getObject("destination");
        CuboidTag copy_cuboid = (CuboidTag) scriptEntry.getObject("cuboid");
        ElementTag remove_original = (ElementTag) scriptEntry.getObject("remove");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), (copy_location != null ? copy_location.debug() : "")
                    + (copy_cuboid != null ? copy_cuboid.debug() : "") + destination.debug() + remove_original.debug());

        }

        List<Location> locations = new ArrayList<>();

        if (copy_location != null) {
            locations.add(copy_location);
        }
        else if (copy_cuboid != null) {
            locations.addAll(copy_cuboid.getBlockLocations()); // TODO: make this work?...
        }

        for (Location loc : locations) {

            Block source = loc.getBlock();
            BlockState sourceState = LocationTag.getBlockStateFor(source);
            Block update = destination.getBlock();

            // TODO: 1.13 - confirm this works
            BlockData blockData = NMSHandler.getInstance().getBlockHelper().getBlockData(source);
            blockData.setBlock(update, false);

            BlockState updateState = LocationTag.getBlockStateFor(update);

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
            }
            else if (sourceState instanceof NoteBlock) {
                ((NoteBlock) updateState).setNote(((NoteBlock) sourceState).getNote());
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

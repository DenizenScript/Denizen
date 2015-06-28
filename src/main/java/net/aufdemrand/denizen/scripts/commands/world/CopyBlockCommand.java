package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class CopyBlockCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // CopyBlock can move a single 'location' ...
            if (arg.matchesArgumentType(dLocation.class)
                    && !scriptEntry.hasObject("location")
                    && !arg.matchesPrefix("t", "to"))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

                // ... or and entire cuboid ...
            else if (arg.matchesArgumentType(dCuboid.class)
                    && !scriptEntry.hasObject("cuboid"))
                scriptEntry.addObject("cuboid", arg.asType(dCuboid.class));

                // ... to a location.
            else if (arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("t", "to"))
                scriptEntry.addObject("destination", arg.asType(dLocation.class));

            else if (arg.matches("remove_original"))
                scriptEntry.addObject("remove", Element.TRUE);

            else arg.reportUnhandled();
        }

        // Check required arguments
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("cuboid"))
            throw new InvalidArgumentsException("Must specify a source location or cuboid.");

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

        dB.report(scriptEntry, getName(), (copy_location != null ? copy_location.debug() : "")
                + (copy_cuboid != null ? copy_cuboid.debug() : "") + destination.debug() + remove_original.debug());

        List<Location> locations = new ArrayList<Location>();

        if (copy_location != null) locations.add(copy_location);
        else if (copy_cuboid != null) locations.addAll(copy_cuboid.getBlockLocations()); // TODO: make this work?...

        for (Location loc : locations) {

            Block source = loc.getBlock();
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
                ((CreatureSpawner) updateState).setCreatureTypeByName(((CreatureSpawner) sourceState)
                        .getCreatureTypeName());
                ((CreatureSpawner) updateState).setDelay(((CreatureSpawner) sourceState).getDelay());
            }

            updateState.update();

            if (remove_original.asBoolean()) {
                loc.getBlock().setType(Material.AIR);
            }

        }
    }
}

package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Modifies blocks based based of single block location.
 * Possibility to do faux animations with blocks.
 *
 * @author Mason Adkins, aufdemrand
 */

public class ModifyBlockCommand extends AbstractCommand implements Listener {

    @Override
    public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {

        // Parse arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesArgumentList(dLocation.class)){
                scriptEntry.addObject("locations", arg.asType(dList.class).filter(dLocation.class));
            }

            else if (!scriptEntry.hasObject("material")
                    && arg.matchesArgumentType(dMaterial.class)) {
                scriptEntry.addObject("material", arg.asType(dMaterial.class));
            }

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius, r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("radius", new Element(arg.getValue()));
            }

            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height, h")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("height", new Element(arg.getValue()));
            }

            else if (!scriptEntry.hasObject("depth")
                    && arg.matchesPrefix("depth, d")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("depth", new Element(arg.getValue()));
            }

            else if (arg.matches("no_physics"))
                scriptEntry.addObject("physics", Element.FALSE);

            else
                arg.reportUnhandled();
        }

        // Must have material
        if (!scriptEntry.hasObject("material"))
            throw new InvalidArgumentsException("Missing material argument!");

        // ..and at least one location.
        if (!scriptEntry.hasObject("locations"))
            throw new InvalidArgumentsException("Missing location argument!");

        // Set some defaults
        scriptEntry.defaultObject("radius", new Element(0))
                .defaultObject("height", new Element(0))
                .defaultObject("depth", new Element(0))
                .defaultObject("physics", Element.TRUE);

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        final dMaterial material = (dMaterial) scriptEntry.getObject("material");
        final List<dObject> locations = (List<dObject>) scriptEntry.getObject("locations");
        final Element physics = scriptEntry.getElement("physics");

        final int radius = scriptEntry.getElement("radius").asInt();
        final int height = scriptEntry.getElement("height").asInt();
        final int depth = scriptEntry.getElement("depth").asInt();

        dB.report(scriptEntry, getName(), aH.debugObj("locations", locations)
                + material.debug() + scriptEntry.getdObject("radius").debug()
                + scriptEntry.getdObject("height").debug() + scriptEntry.getdObject("depth").debug());

        List<Location> blocks_for_removal = new ArrayList<Location>();

        for (dObject obj : locations) {

            dLocation location = (dLocation) obj;
            World world = location.getWorld();
            Block startBlock = location.getBlock();
            Block currentBlock;


            if (physics.equals(Element.FALSE)) {
                blocks_for_removal.add(startBlock.getLocation());
                block_physics.add(startBlock.getLocation());
            }


            startBlock.setType(material.getMaterial());
            if (material.hasData()) startBlock.setData(material.getData());

            if (radius != 0){
                for (int x = 0; x  < 2*radius+1;  x++){
                    for (int z = 0; z < 2*radius+1; z++){
                        currentBlock = world.getBlockAt(startBlock.getX() + x - radius, startBlock.getY(), startBlock.getZ() + z - radius);
                        if (currentBlock.getType() != material.getMaterial()){
                            currentBlock.setType(material.getMaterial());
                            if (material.hasData()) currentBlock.setData(material.getData());
                        }
                    }
                }
            }

            if (height != 0){
                for (int x = 0; x  < 2*radius+1;  x++){
                    for (int z = 0; z < 2*radius+1; z++){
                        for (int y = 1; y < height + 1; y++){
                            currentBlock = world.getBlockAt(startBlock.getX() + x - radius, startBlock.getY() + y, startBlock.getZ() + z - radius);
                            if (currentBlock.getType() != material.getMaterial()){
                                currentBlock.setType(material.getMaterial());
                                if (material.hasData()) currentBlock.setData(material.getData());
                            }
                        }
                    }
                }
            }

            if (depth != 0){
                for (int x = 0; x  < 2*radius+1;  x++){
                    for (int z = 0; z < 2*radius+1; z++){
                        for (int y = 1; y < depth + 1; y++){
                            currentBlock = world.getBlockAt(startBlock.getX() + x - radius, startBlock.getY() - y, startBlock.getZ() + z - radius);
                            if (currentBlock.getType() != material.getMaterial()){
                                currentBlock.setType(material.getMaterial());
                                if (material.hasData()) currentBlock.setData(material.getData());
                            }
                        }
                    }
                }
            }

        }

        for (Location loc : blocks_for_removal)
            block_physics.remove(loc);

    }


    // Keep track of blocks that physics should be blocked for
    public List<Location> block_physics = new CopyOnWriteArrayList<Location>();


    @Override
    public void onEnable() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }


    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {

        if (block_physics.isEmpty()) return;

        for (Location block_location : block_physics)
            if (block_location.equals(event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }

    }




}

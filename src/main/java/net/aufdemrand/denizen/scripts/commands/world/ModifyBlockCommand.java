package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Modifies blocks based based of single block location.
 * Possibility to do faux animations with blocks.
 *
 * @author Mason Adkins
 */

public class ModifyBlockCommand extends AbstractCommand{

    @Override
    public void onEnable() {
        //nothing to do here
    }

    /* MODIFYBLOCK [LOCATION:x,y,z,world] [MATERIAL:DATA VALUE] (RADIUS:##) (HEIGHT:##) (DEPTH:##) */

    /*
     * Arguments: [] - Required, () - Optional
     * [LOCATION:x,y,z,world] Block location
     * [MATERIAL|M] Material/ID to change block(s) to
     * (RADIUS|R) Radius of the selection, default is zero (only changes the one block)
     * (DEPTH|D) Depth of the selection, default is zero
     * (HEIGHT|H) Height of the selection, default is zero
     *
     * Example Usage:
     * MODIFYBLOCK LOCATION:??? MATERIAL:GRASS RADIUS:2 DEPTH:1 HEIGHT:1
     * MODIFYBLOCK LOCATION:??? M:STONE R:2 D:3 H:2
     *
     */

    @Override
    public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesArgumentType(dLocation.class)){
                scriptEntry.addObject("location", arg.asType(dLocation.class));
                dB.echoDebug("...location set to: " + scriptEntry.getObject("location"));
            }

            else if (!scriptEntry.hasObject("material")
                    && arg.matchesArgumentType(dMaterial.class)) {
                scriptEntry.addObject("material", arg.asType(dMaterial.class));
            }

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius, r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("radius", new Element(arg.getValue()));
                dB.echoDebug("...radius set to " + scriptEntry.getObject("radius"));
            }

            else if (!scriptEntry.hasObject("height")
                    && arg.matchesPrefix("height, h")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("height", new Element(arg.getValue()));
                dB.echoDebug("...height set to " + scriptEntry.getObject("height"));

            }

            else if (!scriptEntry.hasObject("depth")
                    && arg.matchesPrefix("depth, d")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("depth", new Element(arg.getValue()));
                dB.echoDebug("...depth set to " + scriptEntry.getObject("depth"));

            }
        }

        if (!scriptEntry.hasObject("material"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "MATERIAL");
        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION);
        scriptEntry.defaultObject("radius", new Element(0));
        scriptEntry.defaultObject("height", new Element(0));
        scriptEntry.defaultObject("depth", new Element(0));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        final dMaterial material = (dMaterial) scriptEntry.getObject("material");
        final dLocation location = (dLocation) scriptEntry.getObject("location");
        final int radius = scriptEntry.getElement("radius").asInt();
        final int height = scriptEntry.getElement("height").asInt();
        final int depth = scriptEntry.getElement("depth").asInt();

        if (location == null || material == null){
            dB.echoDebug("...can not exectue");
            return;
        }

        World world = location.getWorld();
        Block startBlock = location.getBlock();
        Block currentBlock;

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
}

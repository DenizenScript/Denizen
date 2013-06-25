package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.Material;
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
		
		Material material = null;
		int data = 0;
		dLocation location = null;
		int radius = 0;
		int height = 0;
		int depth = 0;
		
		for (String arg : scriptEntry.getArguments()) {			
		    if (aH.matchesLocation(arg)){
		    	location = aH.getLocationFrom(arg);
		    	dB.echoDebug("...location set to: " + location);

            }
			
			else if (aH.matchesValueArg("MATERIAL, M", arg, ArgumentType.Custom)) {
				
				String value = aH.getStringFrom(arg).toUpperCase();
				
				if (value.split(":", 2).length > 1) {
					data = aH.getIntegerFrom(value.split(":", 2)[1]);
				}
				
				value = value.split(":", 2)[0];
				
				if (aH.matchesInteger(value)) {
					material = Material.getMaterial(aH.getIntegerFrom(value));
				}
				else {
					material = Material.getMaterial(value);
				}
				
				if (material != null) dB.echoDebug("...material set to " + material);
				else dB.echoDebug("...material not valid.");

            }
			
			else if (aH.matchesValueArg("RADIUS, R", arg, ArgumentType.Integer)) {
				radius = aH.getIntegerFrom(arg);
				dB.echoDebug("...radius set to " + radius);

            }
			
			else if (aH.matchesValueArg("HEIGHT, H", arg, ArgumentType.Integer)) {
				height = aH.getIntegerFrom(arg);
				dB.echoDebug("...height set to " + height);

            }
			
			else if (aH.matchesValueArg("DEPTH, D", arg, ArgumentType.Integer)) {
				depth = aH.getIntegerFrom(arg);
				dB.echoDebug("...depth set to " + depth);

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
        // Store objects in ScriptEntry for use in execute()
        scriptEntry.addObject("material", material);
        scriptEntry.addObject("data", data);
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("radius", radius);
        scriptEntry.addObject("height", height);
        scriptEntry.addObject("depth", depth);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
		final Material material = (Material) scriptEntry.getObject("material");
		final int data = (Integer) scriptEntry.getObject("data");
        final dLocation location = (dLocation) scriptEntry.getObject("location");
        final int radius = (Integer) scriptEntry.getObject("radius");
        final int height = (Integer) scriptEntry.getObject("height");
        final int depth = (Integer) scriptEntry.getObject("depth");
        
		if (location == null || material == null){
			dB.echoDebug("...can not exectue");
			return;
		}
		
		World world = scriptEntry.getPlayer().getPlayerEntity().getWorld();
		Block startBlock = location.getBlock();
		Block currentBlock;
		
		startBlock.setType(material);
		startBlock.setData((byte) data);
		
		if (radius != 0){
			for (int x = 0; x  < 2*radius+1;  x++){
				for (int z = 0; z < 2*radius+1; z++){
					currentBlock = world.getBlockAt(startBlock.getX() + x - radius, startBlock.getY(), startBlock.getZ() + z - radius);
					if (currentBlock.getType() != material){
						currentBlock.setType(material);
						currentBlock.setData((byte) data);
					}
				}
			}
		}
		
		if (height != 0){
			for (int x = 0; x  < 2*radius+1;  x++){
				for (int z = 0; z < 2*radius+1; z++){
					for (int y = 1; y < height + 1; y++){
						currentBlock = world.getBlockAt(startBlock.getX() + x - radius, startBlock.getY() + y, startBlock.getZ() + z - radius);
						if (currentBlock.getType() != material){
							currentBlock.setType(material);
							currentBlock.setData((byte) data);
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
						if (currentBlock.getType() != material){
							currentBlock.setType(material);
							currentBlock.setData((byte) data);
						}
					}
				}
			}
		}
	}
}

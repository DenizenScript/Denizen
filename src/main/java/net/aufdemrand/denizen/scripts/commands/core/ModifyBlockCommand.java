package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
	
	private World theWorld;
	private Player thePlayer;
	private Material material;
	private Location location;
	
	private int radius;
	private int height;
	private int depth;

	@Override
	public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {
		
		thePlayer = scriptEntry.getPlayer();
		material = null;
		location = null;
		radius = 0;
		height = 0;
		depth = 0;
		
		for (String arg : scriptEntry.getArguments()) {			
		    if (aH.matchesLocation(arg)){
		    	location = aH.getLocationFrom(arg);
		    	dB.echoDebug("...location set to: " + location);

            }
			
			else if (aH.matchesValueArg("MATERIAL, M", arg, ArgumentType.Custom)) {
				if (aH.matchesValueArg("MATERIAL", arg, ArgumentType.Integer)) material = Material.getMaterial(aH.getIntegerFrom(arg));
				else material = Material.getMaterial(aH.getStringFrom(arg));
				
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
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
		if (location == null || material == null){
			dB.echoDebug("...can not exectue");
			return;
		}
		
		theWorld = thePlayer.getWorld();
		Block startBlock = location.getBlock();
		Block currentBlock;
		
		startBlock.setType(material);
		
		if (radius != 0){
			for (int x = 0; x  < 2*radius+1;  x++){
				for (int z = 0; z < 2*radius+1; z++){
					currentBlock = theWorld.getBlockAt(startBlock.getX() + x - radius, startBlock.getY(), startBlock.getZ() + z - radius);
					if (currentBlock.getType() != material){
						currentBlock.setType(material);
					}
				}
			}
		}
		
		if (height != 0){
			for (int x = 0; x  < 2*radius+1;  x++){
				for (int z = 0; z < 2*radius+1; z++){
					for (int y = 1; y < height + 1; y++){
						currentBlock = theWorld.getBlockAt(startBlock.getX() + x - radius, startBlock.getY() + y, startBlock.getZ() + z - radius);
						if (currentBlock.getType() != material){
							currentBlock.setType(material);
						}
					}
				}
			}
		}
		
		if (depth != 0){
			for (int x = 0; x  < 2*radius+1;  x++){
				for (int z = 0; z < 2*radius+1; z++){
					for (int y = 1; y < depth + 1; y++){
						currentBlock = theWorld.getBlockAt(startBlock.getX() + x - radius, startBlock.getY() - y, startBlock.getZ() + z - radius);
						if (currentBlock.getType() != material){
							currentBlock.setType(material);
						}
					}
				}
			}
		}
	}
}

package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;


public class ModifyBlockCommand extends AbstractCommand{

	@Override
	public void onEnable() {
		//nothing to do here
	}
    
    /* MODIFYBLOCK [LOCATION:<??>] [MATERIAL:DATA VALUE] [RADIUS:##] [HEIGHT:##] [DEPTH:##] */

    /* 
     * Arguments: [] - Required, () - Optional 
     * [BOOKMARK:???] Block location
     * [ID|MATERIAL:DATA VALUE|BUKKIT MATERIAL] Material/ID to change block to.. 
     *   
     * Example Usage:
     * MODIFYBLOCK LOCATION:??? ID:4
     * MODIFYBLOCK LOCATION:??? MATERIAL:STONE
     *
     */
	
	private World theWorld;
	private Player thePlayer;
	private Material material = null;
	private Location location = null;
	
	private int radius = 0;
	private int height = 0;
	private int depth = 0;
	
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)throws InvalidArgumentsException {
		
		thePlayer = scriptEntry.getPlayer();
		
		for (String arg : scriptEntry.getArguments()) {
		    if (aH.matchesLocation(arg)){
		    	location = aH.getLocationFrom(arg);
		    	dB.echoError("...location set.");
		    	continue;
		    	
		    }
			
			else if (aH.matchesValueArg("MATERIAL", arg, ArgumentType.Custom) || aH.matchesValueArg("M", arg, ArgumentType.Custom)) {
				material = Material.getMaterial(aH.getStringFrom(arg));
				dB.echoError("...material set to " + material);
				continue;
				
			}
			
			else if (aH.matchesValueArg("RADIUS", arg, ArgumentType.Custom) || aH.matchesValueArg("R", arg, ArgumentType.Custom)) {
				radius = aH.getIntegerFrom(arg);
				dB.echoError("...radius set to " + radius);
				continue;
				
			}
			
			else if (aH.matchesValueArg("HEIGHT", arg, ArgumentType.Custom) || aH.matchesValueArg("H", arg, ArgumentType.Custom)) {
				height = aH.getIntegerFrom(arg);
				dB.echoError("...height set to " + height);
				continue;
				
			}
			
			else if (aH.matchesValueArg("DEPTH", arg, ArgumentType.Custom) || aH.matchesValueArg("D", arg, ArgumentType.Custom)) {
				depth = aH.getIntegerFrom(arg);
				dB.echoError("...depth set to " + depth);
				continue;
				
			}
		}
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		if (location == null || material == null){
			return;
		}
		
		theWorld = thePlayer.getWorld();
		Block startBlock = location.getBlock();
		Block currentBlock;
		
		if (radius != 0){
			for (int x = 0; x < radius;  x++){
				for (int z = 0; z < radius; z++){
					currentBlock = theWorld.getBlockAt(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
					if (currentBlock.getType() != material){
						currentBlock.setType(material);
					}
				}
			}
		}
		
		if (height != 0){
			for (int x = 0; x < radius;  x++){
				for (int z = 0; z < radius; z++){
					for (int y = 0; y < height; y++){
						currentBlock = theWorld.getBlockAt(startBlock.getX() + x, startBlock.getY() + y, startBlock.getZ() + z);
						if (currentBlock.getType() != material){
							currentBlock.setType(material);
						}
					}
				}
			}
		}
		
		if (depth != 0){
			for (int x = 0; x < radius;  x++){
				for (int z = 0; z < radius; z++){
					for (int y = 0; y < depth; y++){
						currentBlock = theWorld.getBlockAt(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
						if (currentBlock.getType() != material){
							currentBlock.setType(material);
						}
					}
				}
			}
		}
		
	}
}

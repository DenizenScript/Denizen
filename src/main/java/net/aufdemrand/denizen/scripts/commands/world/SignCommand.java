package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

/**
 * Creates a sign with a certain text at a location.
 *
 * @author David Cernat
 */

public class SignCommand extends AbstractCommand {
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
    	
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));
        	
        	else if (!scriptEntry.hasObject("text")
        			&& arg.matchesArgumentType(dList.class))
                // add text
                scriptEntry.addObject("text", arg.asType(dList.class));
        }

        // Check to make sure required arguments have been filled
        
        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a Sign location!");
    }
    
	@SuppressWarnings("unchecked")
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

		// Get objects
        dList text = (dList) scriptEntry.getObject("text");
		dLocation location = (dLocation) scriptEntry.getObject("location");
		
		Block sign = location.getBlock();
		sign.setType(Material.valueOf("SIGN_POST"));
        BlockState signState = sign.getState();
        
        int n = 0;
        
        for (String line : text) {
            
            ((Sign) signState).setLine(n, line);
            n++;
        }
        
        signState.update();
    }
}
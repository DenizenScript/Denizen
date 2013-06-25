package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.Effect;
import org.bukkit.Location;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/* playeffect [location:<x,y,z,world>] [effect:<name>] (data:<#>) (radius:<#>)*/

/** 
 * Arguments: [] - Required, () - Optional 
 * [location:<x,y,z,world>] specifies location of the effect
 * [effect:<name>] sets the name of effect to be played
 * (data:<#>) sets the special data value of the effect
 * (radius:<#>) adjusts the radius within which players will observe the effect
 * 
 * Example Usage:
 * playeffect location:123,65,765,world effect:record_play data:2259 radius:7
 * playeffect location:<npc.location> e:smoke r:3
 * 
 * @author David Cernat
 */

public class PlayEffectCommand extends AbstractCommand {

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        Effect effect = null;
        int radius = 3;
        int data = 0;
        Location location = null;

        // Iterate through arguments
		for (String arg : scriptEntry.getArguments()){
			if (aH.matchesLocation(arg))
                location = aH.getLocationFrom(arg);

			else if (aH.matchesValueArg("effect, e", arg, ArgumentType.Custom)) {
        		try {
            		effect = Effect.valueOf(aH.getStringFrom(arg).toUpperCase());
            	} catch (Exception e) {
            		dB.echoError("Invalid effect!");
            	}
            }
			
			else if (aH.matchesValueArg("radius, r", arg, ArgumentType.Integer))
            	radius = aH.getIntegerFrom(arg);

			else if (aH.matchesValueArg("data, d", arg, ArgumentType.Integer))
                data = aH.getIntegerFrom(arg);
            
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        // Check required args
		if (effect == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "EFFECT");
		if (location == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");

        // Stash args in ScriptEntry for use in execute()
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("effect", effect);
        scriptEntry.addObject("radius", radius);
        scriptEntry.addObject("data", data);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        Location location = (Location) scriptEntry.getObject("location");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        int radius = (Integer) scriptEntry.getObject("radius");
        int data = (Integer) scriptEntry.getObject("data");

        // Debugger
        dB.echoApproval("Executing '" + getName() + "': "
                + "Location='" + location.getX() + "," + location.getY()
                + "," + location.getZ() + "," + location.getWorld().getName() + "', "
                + "Effect='" + effect.toString() + ", "
                + "Data='" + data + ", "
                + "Radius='" + radius + "'");

        // Play the sound
        location.getWorld().playEffect(location, effect, data, radius);
	}

}

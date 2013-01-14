package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Location;
import org.bukkit.Sound;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/* PLAYSOUND [LOCATION:x,y,z,world] [SOUND:NAME] (VOLUME:#) (PITCH:#)*/

/* 
 * Arguments: [] - Required, () - Optional 
 * [LOCATION:x,y,z,world] specifies location of the sound
 * [SOUND:NAME] name of sound to be played
 * (VOLUME:#) adjusts the volume of the sound
 * (PITCH:#) adjusts the pitch of the sound
 * 
 * Example Usage:
 * PLAYSOUND LOCATION:123,65,765,world SOUND:SPLASH VOLUME:1 PITCH:2
 * PLAYSOUND LOCATION:123,65,765,world S:SPLASH V:2 P:1
 * 
 */

public class PlaySoundCommand extends AbstractCommand {

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        Sound sound = null;
        float volume = 1;
        float pitch = 1;
        Location location = null;

        // Iterate through arguments
		for (String arg : scriptEntry.getArguments()){
			if (aH.matchesLocation(arg))
                location = aH.getLocationFrom(arg);

			else if (aH.matchesValueArg("SOUND", arg, ArgumentType.Custom) || aH.matchesValueArg("S", arg, ArgumentType.Custom)) {
        		try {
            		sound = Sound.valueOf(aH.getStringFrom(arg).toUpperCase());
            	} catch (Exception e) {
            		dB.echoError("Invalid SOUND!");
            	}
            }
			
			else if (aH.matchesValueArg("VOLUME, V", arg, ArgumentType.Float))
            	volume = aH.getFloatFrom(arg);

			else if (aH.matchesValueArg("PITCH, P", arg, ArgumentType.Float))
                pitch = aH.getFloatFrom(arg);
            
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        // Check required args
		if (sound == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "SOUND");
		if (location == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");

        // Stash args in ScriptEntry for use in execute()
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("sound", sound);
        scriptEntry.addObject("volume", volume);
        scriptEntry.addObject("pitch", pitch);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        Location location = (Location) scriptEntry.getObject("location");
        Sound sound = (Sound) scriptEntry.getObject("sound");
        Float volume = (Float) scriptEntry.getObject("volume");
        Float pitch = (Float) scriptEntry.getObject("pitch");

        // Debugger
        dB.echoApproval("Executing '" + getName() + "': "
                + "Location='" + location.getBlockX() + "," + location.getBlockY()
                + "," + location.getBlockZ() + "," + location.getWorld().getName() + "', "
                + "Sound='" + sound.toString() + ", "
                + "Volume/Pitch='" + volume + "/" + pitch + "'");

        // Play the sound
        location.getWorld().playSound(location, sound, volume, pitch);
	}

}

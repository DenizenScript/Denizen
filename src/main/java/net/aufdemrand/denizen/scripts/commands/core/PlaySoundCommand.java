package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;

public class PlaySoundCommand extends AbstractCommand {

	@Override
	public void onEnable() {
		// nothing to do here
	}

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

	
	Sound sound;
	Location location;
	
	private float volume;
	private float pitch;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		location = null;
		sound = null;
		volume = 1;
		pitch = 1;
		
		for (String arg : scriptEntry.getArguments()){
			if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                if (location != null) dB.echoDebug(Messages.DEBUG_SET_LOCATION, aH.getStringFrom(arg));
                continue;

            } 
			
			else if (aH.matchesValueArg("SOUND", arg, ArgumentType.Custom) || aH.matchesValueArg("S", arg, ArgumentType.Custom)) {
        		try {
            		sound = Sound.valueOf(aH.getStringFrom(arg));
                	dB.echoDebug("...SOUND set to: " + sound.name());
            	} catch (Exception e) {
            		dB.echoError("Invalid SOUND!");
            	}
            	continue;
            	
            }  
			
			else if (aH.matchesValueArg("VOLUME", arg, ArgumentType.Float) || aH.matchesValueArg("V", arg, ArgumentType.Float)) {
            	volume = aH.getFloatFrom(arg);
            	dB.echoDebug("...VOLUME set to: " + volume);
            	continue;
            	
            }  
			
			else if (aH.matchesValueArg("PITCH", arg, ArgumentType.Float) || aH.matchesValueArg("P", arg, ArgumentType.Float)) {
            	pitch = aH.getFloatFrom(arg);
            	dB.echoDebug("...PITCH set to: " + pitch);
            	continue;
            	
            }
            
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		if (sound == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "SOUND");
		if (location == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
    	dB.echoDebug("...playing sound.");
		location.getWorld().playSound(location, sound, volume, pitch);
	}

}

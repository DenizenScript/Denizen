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

	
	Sound theSound;
	Location location;
	
	private float volume;
	private float pitch;
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		location = null;
		theSound = null;
		volume = 1;
		pitch = 1;
		
		for (String arg : scriptEntry.getArguments()){
			if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                if (location != null) dB.echoDebug("...sound location now at bookmark '%s'", arg);
                continue;

            } 
			
			else if (aH.matchesValueArg("SOUND", arg, ArgumentType.Custom) || aH.matchesValueArg("S", arg, ArgumentType.Custom)) {
        		try {
            		theSound = Sound.valueOf(aH.getStringFrom(arg));
                	dB.echoDebug("...sound set to: " + theSound);
            	} catch (IllegalArgumentException e) {
            		dB.echoError(e.getMessage());
            	}
            	continue;
            	
            }  
			
			else if (aH.matchesValueArg("VOLUME", arg, ArgumentType.Float) || aH.matchesValueArg("V", arg, ArgumentType.Integer)) {
            	volume = aH.getIntegerFrom(arg);
            	dB.echoDebug("...volume set to: " + volume);
            	continue;
            	
            }  
			
			else if (aH.matchesValueArg("PITCH", arg, ArgumentType.Float) || aH.matchesValueArg("P", arg, ArgumentType.Integer)) {
            	pitch = aH.getIntegerFrom(arg);
            	dB.echoDebug("...pitch set to: " + pitch);
            	continue;
            	
            }
            
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
    	dB.echoDebug("...playing sound");
		if (theSound != null && location != null) location.getWorld().playSound(location, theSound, volume, pitch);
	}

}

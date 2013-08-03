package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.Location;
import org.bukkit.Sound;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
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

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
        	
			if (!scriptEntry.hasObject("location")
					&& arg.matchesArgumentType(dLocation.class))
				scriptEntry.addObject("location", arg.asType(dLocation.class));
			
			else if (!scriptEntry.hasObject("volume")
					&& arg.matchesPrimitive(aH.PrimitiveType.Double)
					&& arg.matchesPrefix("volume, v"))
				scriptEntry.addObject("volume", arg.asElement());

			else if (!scriptEntry.hasObject("pitch")
					&& arg.matchesPrimitive(aH.PrimitiveType.Double)
					&& arg.matchesPrefix("pitch, p"))
                scriptEntry.addObject("pitch", arg.asElement());

			else if (!scriptEntry.hasObject("sound")
					&& arg.matchesPrimitive(aH.PrimitiveType.String)) {
        		try {
            		scriptEntry.addObject("sound", Sound.valueOf(arg.asElement().asString().toUpperCase()));
            	} catch (Exception e) {
            		dB.echoError("Invalid sound!");
            	}
            }
			
		}

		if (!scriptEntry.hasObject("sound"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "SOUND");
		if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");
		if (!scriptEntry.hasObject("volume"))
			scriptEntry.addObject("volume", new Element(1));
		if (!scriptEntry.hasObject("pitch"))
			scriptEntry.addObject("pitch", new Element(1));
		
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        Location location = (Location) scriptEntry.getObject("location");
        Sound sound = (Sound) scriptEntry.getObject("sound");
        Float volume = ((Element) scriptEntry.getObject("volume")).asFloat();
        Float pitch = ((Element) scriptEntry.getObject("pitch")).asFloat();

        // Debugger
        dB.echoApproval("Executing '" + getName() + "': "
                + "Location='" + location.getX() + "," + location.getY()
                + "," + location.getZ() + "," + location.getWorld().getName() + "', "
                + "Sound='" + sound.toString() + ", "
                + "Volume/Pitch='" + volume + "/" + pitch + "'");

        // Play the sound
        location.getWorld().playSound(location, sound, volume, pitch);
	}

}

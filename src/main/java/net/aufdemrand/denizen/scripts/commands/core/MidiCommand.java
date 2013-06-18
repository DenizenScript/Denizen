package net.aufdemrand.denizen.scripts.commands.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.midi.MidiUtil;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.dEntity;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/* midi [file:<name>] (listener(s):[p@<name>|...])|(location:<x,y,z,world>) (tempo:<#.#>) */

/** 
 * Arguments: [] - Required, () - Optional
 * [file:<name>] specifies the name of the file under plugins/Denizen/midi/
 * (listener(s):[p@<name>|...]) specifies the players who will listen to the midi
 * (location:<x,y,z,world>) specifies the location where the midi will be played
 * [tempo:<#.#>] sets the tempo of the midi
 * 
 * The listeners and location arguments cannot be used at the same time, but
 * the location has a higher priority if both are included.
 * 
 * Example Usage:
 * midi "file:stillalive" "tempo:1.0"
 * midi "file:mariotheme" listeners:p@aufdemrand|p@Jeebiss
 * midi "file:clairdelune" location:200,63,200,world
 * 
 * @author authorblues, David Cernat
 */

public class MidiCommand extends AbstractCommand {

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        File file = null;
        float tempo = 1.0F;
        Location location = null;
        Set<Player> listeners = new HashSet<Player>();

        // Iterate through arguments
		for (String arg : scriptEntry.getArguments()){
			if (aH.matchesLocation(arg))
                location = aH.getLocationFrom(arg);

			else if (aH.matchesValueArg("file, f", arg, ArgumentType.Custom)) {
        		try {
        			String path = denizen.getDataFolder() + 
        					File.separator + "midi" +
        					File.separator + aH.getStringFrom(arg);
        			
        			if (!path.endsWith(".mid")) {
        				
        				path = path + ".mid";
        			}
        			
            		file = new File(path);
            	} catch (Exception e) {
            		dB.echoError("Invalid file!");
            	}
            }
			
			else if (aH.matchesValueArg("listeners, l", arg, ArgumentType.Custom)) {
            	
                Entity entity = null;

                for (String listener : aH.getListFrom(arg)) {
                	
                	entity = dEntity.valueOf(listener).getBukkitEntity();
                	
                	if (entity != null && entity instanceof Player) {
                		
                		listeners.add((Player) entity);
                	}
            		else {
            			dB.echoError("Invalid listener '%s'!", listener);
            		}
                }
			}
			
			else if (aH.matchesValueArg("tempo, t", arg, ArgumentType.Float))
            	tempo = aH.getFloatFrom(arg);
            
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        // Check required args
		if (file == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "FILE");
		
		// If there are no listeners, and the location is null,
		// add this player to the listeners
        if (location == null && listeners.size() == 0) {
        	
        	listeners.add(scriptEntry.getPlayer());
        }

        // Stash args in ScriptEntry for use in execute()
        scriptEntry.addObject("file", file);
        scriptEntry.addObject("listeners", listeners);
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("tempo", tempo);
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        File file = (File) scriptEntry.getObject("file");
        @SuppressWarnings("unchecked")
		Set<Player> listeners = (Set<Player>) scriptEntry.getObject("listeners");
        Location location = (Location) scriptEntry.getObject("location");
        Float tempo = (Float) scriptEntry.getObject("tempo");
                
        // Report to dB
        dB.report(getName(),
               aH.debugObj("Playing midi file", file.getPath()
                        + (listeners != null ? aH.debugObj("Listeners", listeners) : "")
                        + (location != null ? aH.debugObj("Location", location) : ""))
                        + aH.debugObj("Tempo", tempo));

        // Play the sound
        if (location != null) {
        	MidiUtil.playMidiQuietly(file, tempo, location);
        }
        else {
        	MidiUtil.playMidiQuietly(file, tempo, listeners);
        }
	}
}

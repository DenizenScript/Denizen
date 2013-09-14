package net.aufdemrand.denizen.scripts.commands.world;

import java.io.File;
import java.util.HashSet;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.midi.MidiUtil;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;


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
 * @author David Cernat
 */

public class MidiCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("location") &&
                    arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("listeners") &&
                    arg.matchesArgumentList(dPlayer.class))
                scriptEntry.addObject("listeners", arg.asType(dList.class));

            else if (!scriptEntry.hasObject("tempo") &&
                    arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("tempo", arg.asElement());

            else if (!scriptEntry.hasObject("file")) {
                String path = denizen.getDataFolder() +
                        File.separator + "midi" +
                        File.separator + arg.getValue();
                if (!path.endsWith(".mid"))
                    path = path + ".mid";

                scriptEntry.addObject("file", new Element(path));
            }

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        // Check required args
        if (!scriptEntry.hasObject("file"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "FILE");

        if (!scriptEntry.hasObject("location") &&
            !scriptEntry.hasObject("listeners"))
            scriptEntry.addObject("listeners", new dList(scriptEntry.getPlayer().identify()));

        if (!scriptEntry.hasObject("tempo"))
            scriptEntry.addObject("tempo", new Element(1));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        File file;
        try {
            file = new File(scriptEntry.getElement("file").asString());
        }
        catch (Exception ex) {
            dB.echoError("Invalid file " + scriptEntry.getElement("file").asString());
            return;
        }
        if (!file.exists()) {
            dB.echoError("Invalid file " + scriptEntry.getElement("file").asString());
            return;
        }
        dList listeners = (dList) scriptEntry.getObject("listeners");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        float tempo = (float) scriptEntry.getElement("tempo").asDouble();

        // Report to dB
        dB.report(getName(),
               aH.debugObj("file", file.getPath()) +
                       (listeners != null ? listeners.debug() : "") +
                       (location != null ? location.debug() : "") +
                        aH.debugObj("Tempo", tempo));

        // Play the sound
        if (location != null) {
            MidiUtil.playMidiQuietly(file, tempo, location);
        }
        else {
            HashSet<Player> listenerSet = new HashSet<Player>();
            for (String player: listeners.toArray()) {
                listenerSet.add(dPlayer.valueOf(player).getPlayerEntity());
            }
            MidiUtil.playMidiQuietly(file, tempo, listenerSet);
        }
    }
}

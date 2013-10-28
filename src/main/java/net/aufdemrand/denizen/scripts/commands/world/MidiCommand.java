package net.aufdemrand.denizen.scripts.commands.world;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.midi.MidiUtil;
import net.aufdemrand.denizen.utilities.debugging.dB;


/**
 * Arguments: [] - Required, () - Optional
 * [<file>] specifies the name of the file under plugins/Denizen/midi/
 * (<entity>|...) specifies the entities the midi will be played at
 * (<location>) specifies the location where the midi will be played
 * (tempo:<#.#>) sets the tempo of the midi
 *
 * The listeners and location arguments cannot be used at the same time, but
 * the location has a higher priority if both are included.
 *
 * Example Usage:
 * midi stillalive tempo:1.0
 * midi p@aufdemrand|p@Jeebiss mariotheme
 * midi l@200,63,200,world clairdelune
 *
 * @author David Cernat
 */

public class MidiCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cancel")
                 && (arg.matches("cancel") || arg.matches("stop")))

                scriptEntry.addObject("cancel", "");

            else if (!scriptEntry.hasObject("location") &&
                arg.matchesArgumentType(dLocation.class))

                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("entities") &&
                     arg.matchesArgumentList(dEntity.class))

                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));

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

            else
                arg.reportUnhandled();
        }

        // Produce error if there is no file and the "cancel" argument was
        // not used
        if (!scriptEntry.hasObject("file")
            && !scriptEntry.hasObject("cancel"))
            throw new InvalidArgumentsException("Missing file (Midi name) argument!");

        if (!scriptEntry.hasObject("location")) {
            scriptEntry.defaultObject("entities", (scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null),
                                                  (scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null));
        }

        scriptEntry.defaultObject("tempo", new Element(1));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        boolean cancel = scriptEntry.hasObject("cancel");
        File file = !cancel ? new File(scriptEntry.getElement("file").asString()) : null;

        if (!cancel && !file.exists()) {
            dB.echoError("Invalid file " + scriptEntry.getElement("file").asString());
            return;
        }

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        float tempo = (float) scriptEntry.getElement("tempo").asDouble();

        // Report to dB
        dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                             (file != null ? aH.debugObj("file", file.getPath()) : "") +
                             (entities != null ? aH.debugObj("entities", entities.toString()) : "") +
                             (location != null ? location.debug() : "") +
                             aH.debugObj("tempo", tempo));

        // Play the midi
        if (!cancel) {
            if (location != null) {
                MidiUtil.playMidi(file, tempo, location);
            }
            else {
                MidiUtil.playMidi(file, tempo, entities);
            }
        }
        else {
            if (location != null) {
                MidiUtil.stopMidi(location.identify());
            }
            else {
                MidiUtil.stopMidi(entities);
            }
        }
    }
}

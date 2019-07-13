package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.utilities.midi.MidiUtil;
import com.denizenscript.denizen.utilities.midi.NoteBlockReceiver;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MidiCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name Midi
    // @Syntax midi (cancel) [<file>] (<location>/<entity>|...) (tempo:<#.#>) (volume:<#.#>)
    // @Required 1
    // @Short Plays a midi file at a given location or to a list of players using note block sounds.
    // @Group world
    //
    // @Description
    // This will fully load a midi song file stored in the '../plugins/Denizen/midi/' folder. The file
    // must be a valid midi file with the extension '.mid'. It will continuously play the song as
    // noteblock songs at the given location or group of players until the song ends. If no location or
    // entity is specified, by default this will play for the attached player.
    //
    // Also, an example Midi song file has been included: "Denizen" by Black Coyote. He made it just for us!
    // Check out more of his amazing work at: http://www.youtube.com/user/BlaCoyProductions
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to play a midi song file on the current player.
    // - midi file:Denizen
    //
    // @Usage
    // Use to play a midi song file at a given location.
    // - midi file:Denizen <player.location>
    //
    // @Usage
    // Use to play a midi song file at a given location to the specified player(s), and wait for it to finish.
    // - ~midi file:Denizen <server.list_online_players>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("cancel")
                    && (arg.matches("cancel") || arg.matches("stop"))) {
                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("location") &&
                    arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("entities") &&
                    arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("volume") &&
                    arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double) &&
                    arg.matchesPrefix("volume", "vol", "v")) {
                scriptEntry.addObject("volume", arg.asElement());
            }
            else if (!scriptEntry.hasObject("tempo") &&
                    arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)) {
                scriptEntry.addObject("tempo", arg.asElement());
            }
            else if (!scriptEntry.hasObject("file")) {

                String path = DenizenAPI.getCurrentInstance().getDataFolder() +
                        File.separator + "midi" +
                        File.separator + arg.getValue();
                if (!path.endsWith(".mid")) {
                    path = path + ".mid";
                }

                scriptEntry.addObject("file", new Element(path));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Produce error if there is no file and the "cancel" argument was
        // not used
        if (!scriptEntry.hasObject("file")
                && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Missing file (Midi name) argument!");
        }

        if (!scriptEntry.hasObject("location")) {
            scriptEntry.defaultObject("entities", (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null),
                    (Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null));
        }

        scriptEntry.defaultObject("tempo", new Element(1)).defaultObject("volume", new Element(10));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        boolean cancel = scriptEntry.hasObject("cancel");
        File file = !cancel ? new File(scriptEntry.getElement("file").asString()) : null;

        if (!cancel && !Utilities.canReadFile(file)) {
            dB.echoError("Server config denies reading files in that location.");
            return;
        }

        if (!cancel && !file.exists()) {
            dB.echoError(scriptEntry.getResidingQueue(), "Invalid file " + scriptEntry.getElement("file").asString());
            return;
        }

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        float tempo = scriptEntry.getElement("tempo").asFloat();
        float volume = scriptEntry.getElement("volume").asFloat();

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (cancel ? ArgumentHelper.debugObj("cancel", cancel) : "") +
                    (file != null ? ArgumentHelper.debugObj("file", file.getPath()) : "") +
                    (entities != null ? ArgumentHelper.debugObj("entities", entities.toString()) : "") +
                    (location != null ? location.debug() : "") +
                    ArgumentHelper.debugObj("tempo", tempo) +
                    ArgumentHelper.debugObj("volume", volume));
        }

        // Play the midi
        if (!cancel) {
            NoteBlockReceiver rec;
            if (location != null) {
                rec = MidiUtil.playMidi(file, tempo, volume, location);
            }
            else {
                rec = MidiUtil.playMidi(file, tempo, volume, entities);
            }
            if (rec == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Something went wrong playing a midi!");
                scriptEntry.setFinished(true);
            }
            else {
                rec.onFinish = new Runnable() {
                    @Override
                    public void run() {
                        scriptEntry.setFinished(true);
                    }
                };
            }
        }
        else {
            if (location != null) {
                MidiUtil.stopMidi(location.identify());
            }
            else {
                MidiUtil.stopMidi(entities);
            }
            scriptEntry.setFinished(true);
        }
    }
}

package com.denizenscript.denizen.scripts.commands.world;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.midi.MidiUtil;
import com.denizenscript.denizen.utilities.midi.NoteBlockReceiver;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;

import java.io.File;
import java.util.List;

public class MidiCommand extends AbstractCommand implements Holdable {

    public MidiCommand() {
        setName("midi");
        setSyntax("midi [cancel/<file> (tempo:<#.#>) (volume:<#.#>)] (<location>/<entity>|...)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Midi
    // @Syntax midi [cancel/<file> (tempo:<#.#>) (volume:<#.#>)] (<location>/<entity>|...)
    // @Required 1
    // @Maximum 4
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
    // The midi command is ~waitable. Refer to <@link language ~waitable>.
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
    // - ~midi file:Denizen <server.online_players>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("cancel")
                    && (arg.matches("cancel") || arg.matches("stop"))) {
                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("location") &&
                    arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("entities") &&
                    arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("volume") &&
                    arg.matchesFloat() &&
                    arg.matchesPrefix("volume", "vol", "v")) {
                scriptEntry.addObject("volume", arg.asElement());
            }
            else if (!scriptEntry.hasObject("tempo") &&
                    arg.matchesFloat()) {
                scriptEntry.addObject("tempo", arg.asElement());
            }
            else if (!scriptEntry.hasObject("file")) {

                String path = Denizen.getInstance().getDataFolder() +
                        File.separator + "midi" +
                        File.separator + arg.getValue();
                if (!path.endsWith(".mid")) {
                    path = path + ".mid";
                }

                scriptEntry.addObject("file", new ElementTag(path));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("file")
                && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Missing file (Midi name) argument!");
        }
        if (!scriptEntry.hasObject("location")) {
            scriptEntry.defaultObject("entities", Utilities.entryDefaultEntityList(scriptEntry, true));
        }
        scriptEntry.defaultObject("tempo", new ElementTag(1)).defaultObject("volume", new ElementTag(10));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {
        boolean cancel = scriptEntry.hasObject("cancel");
        File file = !cancel ? new File(scriptEntry.getElement("file").asString()) : null;
        if (!cancel && !Utilities.canReadFile(file)) {
            Debug.echoError("Server config denies reading files in that location.");
            return;
        }
        if (!cancel && !file.exists()) {
            Debug.echoError(scriptEntry.getResidingQueue(), "Invalid file " + scriptEntry.getElement("file").asString());
            return;
        }
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        LocationTag location = scriptEntry.getObjectTag("location");
        float tempo = scriptEntry.getElement("tempo").asFloat();
        float volume = scriptEntry.getElement("volume").asFloat();
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? ArgumentHelper.debugObj("cancel", cancel) : "") +
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
                Debug.echoError(scriptEntry.getResidingQueue(), "Something went wrong playing a midi!");
                scriptEntry.setFinished(true);
            }
            else {
                rec.onFinish = () -> scriptEntry.setFinished(true);
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

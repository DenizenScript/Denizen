package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.midi.MidiUtil;
import com.denizenscript.denizen.utilities.midi.NoteBlockReceiver;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    // @Synonyms Music
    // @Group world
    //
    // @Description
    // This will fully load a midi song file stored in the '../plugins/Denizen/midi/' folder.
    // The file must be a valid midi file with the extension '.mid'.
    // It will continuously play the song as noteblock songs at the given location or group of players until the song ends.
    // If no location or entity is specified, by default this will play for the attached player.
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
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("cancel")
                    && (arg.matches("cancel") || arg.matches("stop"))) {
                scriptEntry.addObject("cancel", "true");
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
                scriptEntry.addObject("file", arg.asElement());
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

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        boolean cancel = scriptEntry.hasObject("cancel");
        ElementTag filePath = scriptEntry.getElement("file");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        LocationTag location = scriptEntry.getObjectTag("location");
        float tempo = scriptEntry.getElement("tempo").asFloat();
        float volume = scriptEntry.getElement("volume").asFloat();
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? db("cancel", true) : ""), filePath, db("entities", entities), location, db("tempo", tempo), db("volume", volume));
        }
        // Play the midi
        if (!cancel) {
            String fName = scriptEntry.getElement("file").asString();
            if (!fName.endsWith(".mid")) {
                fName += ".mid";
            }
            File file = new File(Denizen.getInstance().getDataFolder(), "/midi/" + fName);
            if (!Utilities.canReadFile(file)) {
                Debug.echoError("Cannot read from that file path due to security settings in Denizen/config.yml.");
                return;
            }
            if (!file.exists()) {
                Debug.echoError(scriptEntry, "Invalid file " + filePath.asString());
                return;
            }
            NoteBlockReceiver rec;
            if (location != null) {
                rec = MidiUtil.playMidi(file, tempo, volume, location);
            }
            else {
                rec = MidiUtil.playMidi(file, tempo, volume, entities);
            }
            if (rec == null) {
                Debug.echoError(scriptEntry, "Something went wrong playing a midi!");
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

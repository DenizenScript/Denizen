package com.denizenscript.denizen.utilities.midi;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for playing midi files for players to hear.
 *
 * @author authorblues, patched by mcmonkey
 */
public class MidiUtil {
    public static Map<String, Receiver> receivers = new HashMap<>();

    public static void startSequencer(File file, float tempo, NoteBlockReceiver receiver)
            throws InvalidMidiDataException, IOException, MidiUnavailableException {

        Sequencer sequencer = MidiSystem.getSequencer(false);
        sequencer.setSequence(MidiSystem.getSequence(file));
        sequencer.addMetaEventListener(receiver);
        sequencer.open();

        receiver.setSequencer(sequencer);

        // Set desired tempo
        sequencer.setTempoFactor(tempo);

        sequencer.getTransmitter().setReceiver(receiver);
        sequencer.start();
    }

    public static NoteBlockReceiver playMidi(File file, float tempo, float volume, List<EntityTag> entities) {
        try {
            NoteBlockReceiver receiver = new NoteBlockReceiver(entities, entities.get(0).getUUID().toString());
            receiver.VOLUME_RANGE = volume;
            // If there is already a midi file being played for one of the entities,
            // stop playing it
            for (EntityTag entity : entities) {
                stopMidi(entity.getUUID().toString());
            }
            receivers.put(entities.get(0).getUUID().toString(), receiver);
            startSequencer(file, tempo, receiver);
            return receiver;
        }
        catch (Exception e) {
            Debug.echoError(e);
            return null;
        }
    }

    public static NoteBlockReceiver playMidi(File file, float tempo, float volume, LocationTag location) {
        try {
            NoteBlockReceiver receiver = new NoteBlockReceiver(location, location.identify());
            receiver.VOLUME_RANGE = volume;
            // If there is already a midi file being played for this location,
            // stop playing it
            stopMidi(location.identify());
            receivers.put(location.identify(), receiver);
            startSequencer(file, tempo, receiver);
            return receiver;
        }
        catch (Exception e) {
            Debug.echoError(e);
            return null;
        }
    }

    public static void stopMidi(String object) {
        if (receivers.containsKey(object)) {
            receivers.get(object).close();
        }
    }

    public static void stopMidi(List<EntityTag> entities) {
        for (EntityTag entity : entities) {
            stopMidi(entity.getUUID().toString());
        }
    }
}

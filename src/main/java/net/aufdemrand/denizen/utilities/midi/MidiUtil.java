package net.aufdemrand.denizen.utilities.midi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;

import org.bukkit.Sound;

/**
 * Utility for playing midi files for players to hear.
 *
 * @author authorblues
 */
public class MidiUtil
{
    public static Map<String, Receiver> receivers = new HashMap<String, Receiver>();

    public static void startSequencer(File file, float tempo, Receiver receiver)
            throws InvalidMidiDataException, IOException, MidiUnavailableException
    {

        Sequencer sequencer = MidiSystem.getSequencer(false);
        sequencer.setSequence(MidiSystem.getSequence(file));
        sequencer.open();

        // Set desired tempo
        sequencer.setTempoFactor(tempo);

        sequencer.getTransmitter().setReceiver(receiver);
        sequencer.start();
    }

    public static void playMidi(File file, float tempo, List<dEntity> entities)
    {
        try {
            NoteBlockReceiver receiver = new NoteBlockReceiver(entities);
            // If there is already a midi file being played for one of the entities,
            // stop playing it
            for (dEntity entity : entities) {
                stopMidi(entity.identify());
                receivers.put(entity.identify(), receiver);
            }

            startSequencer(file, tempo, receiver);
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static void playMidi(File file, float tempo, dLocation location)
    {
        try {
            NoteBlockReceiver receiver = new NoteBlockReceiver(location);
            // If there is already a midi file being played for this location,
            // stop playing it
            stopMidi(location.identify());
            receivers.put(location.identify(), receiver);

            startSequencer(file, tempo, receiver);
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static void stopMidi(String object) {
        if (receivers.containsKey(object)) {
            receivers.get(object).close();
            receivers.remove(object);
        }
    }

    // provided by github.com/sk89q/craftbook
    private static final int[] instruments = {
        0, 0, 0, 0, 0, 0, 0, 5, //   8
        6, 0, 0, 0, 0, 0, 0, 0, //  16
        0, 0, 0, 0, 0, 0, 0, 5, //  24
        5, 5, 5, 5, 5, 5, 5, 5, //  32
        6, 6, 6, 6, 6, 6, 6, 6, //  40
        5, 5, 5, 5, 5, 5, 5, 2, //  48
        5, 5, 5, 5, 0, 0, 0, 0, //  56
        0, 0, 0, 0, 0, 0, 0, 0, //  64
        0, 0, 0, 0, 0, 0, 0, 0, //  72
        0, 0, 0, 0, 0, 0, 0, 0, //  80
        0, 0, 0, 0, 0, 0, 0, 0, //  88
        0, 0, 0, 0, 0, 0, 0, 0, //  96
        0, 0, 0, 0, 0, 0, 0, 0, // 104
        0, 0, 0, 0, 0, 0, 0, 0, // 112
        1, 1, 1, 3, 1, 1, 1, 5, // 120
        1, 1, 1, 1, 1, 2, 4, 3, // 128
    };

    public static Sound patchToInstrument(int patch)
    {
        // look up the instrument matching the patch
        switch (instruments[patch])
        {
            case 1: return Sound.NOTE_BASS_GUITAR;
            case 2: return Sound.NOTE_SNARE_DRUM;
            case 3: return Sound.NOTE_STICKS;
            case 4: return Sound.NOTE_BASS_DRUM;
            case 5: return Sound.NOTE_PLING;
            case 6: return Sound.NOTE_BASS;
        }

        // if no instrument match is found, use piano
        return Sound.NOTE_PIANO;
    }
}


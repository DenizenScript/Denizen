package net.aufdemrand.denizen.utilities.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

/**
 * Midi Receiver for processing note events.
 *
 * @author authorblues
 */
public class NoteBlockReceiver implements Receiver
{
    private static final float VOLUME_RANGE = 10.0f;

    private final Set<Player> listeners;
    private final Location location;
    private final Map<Integer, Integer> channelPatches;

    public NoteBlockReceiver(Set<Player> listeners) throws InvalidMidiDataException, IOException
    {
        this.listeners = listeners;
        this.location = null;
        this.channelPatches = Maps.newHashMap();
    }
    
    public NoteBlockReceiver(Location location) throws InvalidMidiDataException, IOException
    {
        this.listeners = null;
        this.location = location;
        this.channelPatches = Maps.newHashMap();
    }

    @Override
    public void send(MidiMessage m, long time)
    {
        if (m instanceof ShortMessage)
        {
            ShortMessage smessage = (ShortMessage) m;
            int chan = smessage.getChannel();

            switch (smessage.getCommand())
            {
                case ShortMessage.PROGRAM_CHANGE:
                    int patch = smessage.getData1();
                    channelPatches.put(chan, patch);
                    break;

                case ShortMessage.NOTE_ON:
                    this.playNote(smessage);
                    break;

                case ShortMessage.NOTE_OFF:
                    break;
            }
        }
    }

    public void playNote(ShortMessage message)
    {
        // if this isn't a NOTE_ON message, we can't play it
        if (ShortMessage.NOTE_ON != message.getCommand()) return;

        int channel = message.getChannel();
        
        // If this is a percussion channel, return
        if (channel == 9) return;
        
        // get the correct instrument
        Integer patch = channelPatches.get(channel);

        // get pitch and volume from the midi message
        float pitch = (float) ToneUtil.midiToPitch(message);
        float volume = VOLUME_RANGE * (message.getData2() / 127.0f);

        Sound instrument = Sound.NOTE_PIANO;
        if (patch != null) instrument = MidiUtil.patchToInstrument(patch);

        if (location != null) {
            
            location.getWorld().playSound(location, instrument, volume, pitch);
        }
        else {
            for (Player player : listeners)
                player.playSound(player.getLocation(), instrument, volume, pitch);
        }
    }

    @Override
    public void close()
    {
        listeners.clear();
        channelPatches.clear();
    }
}

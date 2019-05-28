package net.aufdemrand.denizen.utilities.midi;

import com.google.common.collect.Maps;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.SoundHelper;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import javax.sound.midi.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Midi Receiver for processing note events.
 *
 * @author authorblues, patched by mcmonkey
 */
public class NoteBlockReceiver implements Receiver, MetaEventListener {
    public float VOLUME_RANGE = 10.0f;

    private List<dEntity> entities;
    private dLocation location;
    private Map<Integer, Integer> channelPatches;
    public String key = null;
    public Sequencer sequencer;
    public boolean closing = false;

    public NoteBlockReceiver(List<dEntity> entities, String _Key) throws InvalidMidiDataException, IOException {
        this.entities = entities;
        this.location = null;
        this.channelPatches = Maps.newHashMap();
        this.key = _Key;
    }

    public NoteBlockReceiver(dLocation location, String _Key) throws InvalidMidiDataException, IOException {
        this.entities = null;
        this.location = location;
        this.channelPatches = Maps.newHashMap();
        this.key = _Key;
    }

    public void setSequencer(Sequencer sequencer) {
        this.sequencer = sequencer;
    }

    @Override
    public void meta(MetaMessage meta) {
        if (meta.getType() == 47) { // Track completion
            close();
        }
    }

    @Override
    public void send(MidiMessage m, long time) {
        if (closing) {
            return;
        }
        if (m instanceof ShortMessage) {
            ShortMessage smessage = (ShortMessage) m;
            int chan = smessage.getChannel();

            switch (smessage.getCommand()) {
                case ShortMessage.PROGRAM_CHANGE:
                    int patch = smessage.getData1();
                    channelPatches.put(chan, patch);
                    break;

                case ShortMessage.NOTE_ON:
                    this.playNote(smessage);
                    break;

                case ShortMessage.NOTE_OFF:
                    break;

                case ShortMessage.STOP:
                    close();
                    break;
            }
        }
    }

    // Note that this may run async
    public void playNote(ShortMessage message) {
        // if this isn't a NOTE_ON message, we can't play it
        if (ShortMessage.NOTE_ON != message.getCommand()) {
            return;
        }

        int channel = message.getChannel();

        // If this is a percussion channel, return
        if (channel == 9) {
            return;
        }

        if (channelPatches == null) {
            dB.echoError("Trying to play notes on closed midi NoteBlockReceiver!");
            return;
        }

        // get the correct instrument
        Integer patch = channelPatches.get(channel);

        // get pitch and volume from the midi message
        float pitch = (float) ToneUtil.midiToPitch(message);
        float volume = VOLUME_RANGE * (message.getData2() / 127.0f);

        SoundHelper soundHelper = NMSHandler.getInstance().getSoundHelper();
        Sound instrument = soundHelper.getDefaultMidiInstrument();
        if (patch != null) {
            instrument = soundHelper.getMidiInstrumentFromPatch(patch);
        }

        if (location != null) {
            location.getWorld().playSound(location, instrument, volume, pitch);
        }
        else if (entities != null && !entities.isEmpty()) {
            for (int i = 0; i < entities.size(); i++) {
                dEntity entity = entities.get(i);
                if (entity.isSpawned()) {
                    if (entity.isPlayer()) {
                        NMSHandler.getInstance().getSoundHelper().playSound(entity.getPlayer(), entity.getLocation(), instrument, volume, pitch, "RECORDS");
                    }
                    else {
                        NMSHandler.getInstance().getSoundHelper().playSound(null, entity.getLocation(), instrument, volume, pitch, "RECORDS");
                    }
                }
                else {
                    entities.remove(i);
                    i--;
                }
            }
        }
        else {
            this.close();
        }
    }

    public Runnable onFinish = null;

    @Override
    public void close() {
        if (closing) {
            return;
        }
        closing = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), () -> {
            if (MidiUtil.receivers.containsKey(key)) {
                MidiUtil.receivers.remove(key);
            }
            if (sequencer != null) {
                sequencer.close();
                sequencer = null;
            }
            channelPatches.clear();
            channelPatches = null;
            entities = null;
            location = null;
            if (onFinish != null) {
                onFinish.run();
            }
        }, 1);
    }
}

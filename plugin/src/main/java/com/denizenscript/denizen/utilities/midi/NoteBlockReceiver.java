package com.denizenscript.denizen.utilities.midi;

import com.denizenscript.denizen.Denizen;
import com.google.common.collect.Maps;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.SoundHelper;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import javax.sound.midi.*;
import java.util.List;
import java.util.Map;

/**
 * Midi Receiver for processing note events.
 *
 * @author authorblues, patched by mcmonkey
 */
public class NoteBlockReceiver implements Receiver, MetaEventListener {
    public float VOLUME_RANGE = 10.0f;

    private List<EntityTag> entities;
    private LocationTag location;
    private Map<Integer, Integer> channelPatches;
    public String key;
    public Sequencer sequencer;
    public boolean closing = false;

    public NoteBlockReceiver(List<EntityTag> entities, String _Key) {
        this.entities = entities;
        this.location = null;
        this.channelPatches = Maps.newHashMap();
        this.key = _Key;
    }

    public NoteBlockReceiver(LocationTag location, String _Key) {
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

    public void playNote(ShortMessage message) {
        int channel = message.getChannel();
        // If this is a percussion channel, return
        if (channel == 9) {
            return;
        }
        if (channelPatches == null) {
            Debug.echoError("Trying to play notes on closed midi NoteBlockReceiver!");
            return;
        }
        // get the correct instrument
        Integer patch = channelPatches.get(channel);
        // get pitch and volume from the midi message
        float pitch = (float) ToneUtil.midiToPitch(message);
        float volume = VOLUME_RANGE * (message.getData2() / 127.0f);
        Sound instrument = patch == null ? NMSHandler.soundHelper.getDefaultMidiInstrument() : NMSHandler.soundHelper.getMidiInstrumentFromPatch(patch);
        Runnable actualPlay = () -> {
            if (location != null) {
                location.getWorld().playSound(location, instrument, volume, pitch);
            }
            else if (entities != null && !entities.isEmpty()) {
                for (int i = 0; i < entities.size(); i++) {
                    EntityTag entity = entities.get(i);
                    if (entity.isSpawned()) {
                        if (entity.isPlayer()) {
                            NMSHandler.soundHelper.playSound(entity.getPlayer(), entity.getLocation(), instrument, volume, pitch, "RECORDS");
                        }
                        else {
                            NMSHandler.soundHelper.playSound(null, entity.getLocation(), instrument, volume, pitch, "RECORDS");
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
        };
        if (Bukkit.isPrimaryThread()) {
            actualPlay.run();
        }
        else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), actualPlay);
        }
    }

    public Runnable onFinish = null;

    @Override
    public void close() {
        if (closing) {
            return;
        }
        closing = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> {
            try {
                MidiUtil.receivers.remove(key);
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
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }, 1);
    }
}

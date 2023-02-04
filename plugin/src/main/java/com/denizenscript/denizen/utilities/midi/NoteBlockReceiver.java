package com.denizenscript.denizen.utilities.midi;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

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
        Sound instrument = patch == null ? defaultMidiInstrument : getMidiInstrumentFromPatch(patch);
        Runnable actualPlay = () -> {
            if (location != null) {
                location.getWorld().playSound(location, instrument, volume, pitch);
            }
            else if (entities != null && !entities.isEmpty()) {
                for (int i = 0; i < entities.size(); i++) {
                    EntityTag entity = entities.get(i);
                    if (entity.isSpawned()) {
                        if (entity.isPlayer()) {
                            entity.getPlayer().playSound(entity.getPlayer(), instrument, SoundCategory.RECORDS, volume, pitch);
                        }
                        else {
                            entity.getLocation().getWorld().playSound(entity.getLocation(), instrument, SoundCategory.RECORDS, volume, pitch);
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

    private static final int[] instruments = { // Last revised 2023/02/24 for MC 1.19.3 instrument list (previously revised for MC 1.12)
            0, 0, 0, 0, 0, 0, 0, 5,         // 8
            9, 9, 9, 9, 9, 6, 0, 9,         // 16
            9, 0, 0, 0, 0, 0, 0, 5,         // 24
            5, 5, 5, 5, 5, 5, 5, 1,         // 32
            1, 1, 1, 1, 1, 1, 1, 5,         // 40
            1, 5, 5, 5, 5, 5, 5, 5,         // 48
            5, 5, 5, 8, 8, 8, 8, 8,         // 56
            8, 8, 8, 8, 8, 8, 8, 8,         // 64
            8, 8, 8, 8, 8, 8, 8, 8,         // 72
            8, 8, 8, 8, 8, 8, 8, 8,         // 80
            0, 1, 2, 3, 4, 5, 6, 7,         // 88
            8, 9, 10, 11, 12, 13, 14, 15,   // 96
            0, 0, 0, 0, 0, 0, 0, 5,         // 104
            5, 5, 5, 9, 8, 5, 8, 6,         // 112
            6, 3, 3, 2, 2, 2, 6, 5,         // 120
            1, 1, 1, 6, 1, 2, 4, 7,         // 128
    };

    public static Sound getMidiInstrumentFromPatch(int patch) {
        switch (instruments[patch]) {
            case 0: return Sound.BLOCK_NOTE_BLOCK_HARP;
            case 1: return Sound.BLOCK_NOTE_BLOCK_BASS;
            case 2: return Sound.BLOCK_NOTE_BLOCK_SNARE;
            case 3: return Sound.BLOCK_NOTE_BLOCK_HAT;
            case 4: return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case 5: return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case 6: return Sound.BLOCK_NOTE_BLOCK_BELL;
            case 7: return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case 8: return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case 9: return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case 10: return Sound.BLOCK_NOTE_BLOCK_PLING;
            case 11: return Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case 12: return Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case 13: return Sound.BLOCK_NOTE_BLOCK_BANJO;
            case 14: return Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case 15: return Sound.BLOCK_NOTE_BLOCK_BIT;
            default: return defaultMidiInstrument;
        }
    }

    public static final Sound defaultMidiInstrument = Sound.BLOCK_NOTE_BLOCK_HARP;
}

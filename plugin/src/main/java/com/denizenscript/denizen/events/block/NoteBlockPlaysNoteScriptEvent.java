package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.NotePlayEvent;

public class NoteBlockPlaysNoteScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // noteblock plays note
    //
    // @Regex ^on noteblock plays note$
    //
    // @Group Block
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    // @Switch instrument:<instrument> to only process the event if a specific instrument was played.
    //
    // @Cancellable true
    //
    // @Triggers when a NoteBlock plays a note.
    //
    // @Context
    // <context.location> returns the LocationTag of the note block.
    // <context.instrument> returns the name of the instrument played, see list at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Instrument.html>.
    // <context.sound> returns the name of the sound (that fits into <@link command playsound>) represented by the instrument.
    // <context.tone> returns the note tone played (A to G).
    // <context.octave> returns the octave the note is played at (as a number).
    // <context.sharp> returns a boolean indicating whether the note is sharp.
    // <context.pitch> returns the computed pitch value (that fits into <@link command playsound>). Note that volume is always 3.
    //
    // -->

    public NoteBlockPlaysNoteScriptEvent() {
        instance = this;
    }

    public static NoteBlockPlaysNoteScriptEvent instance;
    public NotePlayEvent event;
    public LocationTag location;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("noteblock plays note")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "instrument", event.getInstrument().name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "NoteBlockPlaysNote";
    }

    public Sound getSound() {
        switch (event.getInstrument()) {
            case PIANO:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
            case BASS_DRUM:
                return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case SNARE_DRUM:
                return Sound.BLOCK_NOTE_BLOCK_SNARE;
            case STICKS:
                return Sound.BLOCK_NOTE_BLOCK_HAT;
            case BASS_GUITAR:
                return Sound.BLOCK_NOTE_BLOCK_BASS;
            case FLUTE:
                return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case BELL:
                return Sound.BLOCK_NOTE_BLOCK_BELL;
            case GUITAR:
                return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case CHIME:
                return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case XYLOPHONE:
                return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case IRON_XYLOPHONE:
                return Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case COW_BELL:
                return Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case DIDGERIDOO:
                return Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case BIT:
                return Sound.BLOCK_NOTE_BLOCK_BIT;
            case BANJO:
                return Sound.BLOCK_NOTE_BLOCK_BANJO;
            case PLING:
                return Sound.BLOCK_NOTE_BLOCK_PLING;
        }
        return null;
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("instrument")) {
            return new ElementTag(event.getInstrument().name());
        }
        else if (name.equals("sound")) {
            return new ElementTag(getSound().name());
        }
        else if (name.equals("tone")) {
            return new ElementTag(event.getNote().getTone().name());
        }
        else if (name.equals("octave")) {
            return new ElementTag(event.getNote().getOctave());
        }
        else if (name.equals("sharp")) {
            return new ElementTag(event.getNote().isSharped());
        }
        else if (name.equals("pitch")) {
            double pitch = Math.pow(2.0, (double)(event.getNote().getId() - 12) / 12.0); // based on minecraft source
            return new ElementTag(pitch);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onNotePlay(NotePlayEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

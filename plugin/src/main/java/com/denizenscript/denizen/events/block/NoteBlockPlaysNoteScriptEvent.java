package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.Utilities;
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
    // @Group Block
    //
    // @Location true
    //
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
        registerCouldMatcher("noteblock plays note");
        registerSwitches("instrument");
    }

    public NotePlayEvent event;
    public LocationTag location;

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

    public Sound getSound() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            return event.getInstrument().getSound();
        }
        return switch (event.getInstrument()) {
            case PIANO -> Sound.BLOCK_NOTE_BLOCK_HARP;
            case BASS_DRUM -> Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
            case SNARE_DRUM -> Sound.BLOCK_NOTE_BLOCK_SNARE;
            case STICKS -> Sound.BLOCK_NOTE_BLOCK_HAT;
            case BASS_GUITAR -> Sound.BLOCK_NOTE_BLOCK_BASS;
            case FLUTE -> Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case BELL -> Sound.BLOCK_NOTE_BLOCK_BELL;
            case GUITAR -> Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case CHIME -> Sound.BLOCK_NOTE_BLOCK_CHIME;
            case XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case IRON_XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case COW_BELL -> Sound.BLOCK_NOTE_BLOCK_COW_BELL;
            case DIDGERIDOO -> Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO;
            case BIT -> Sound.BLOCK_NOTE_BLOCK_BIT;
            case BANJO -> Sound.BLOCK_NOTE_BLOCK_BANJO;
            case PLING -> Sound.BLOCK_NOTE_BLOCK_PLING;
            default -> null;
        };
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "instrument" -> new ElementTag(event.getInstrument());
            case "sound" -> Utilities.enumLikeToLegacyElement(getSound());
            case "tone" -> new ElementTag(event.getNote().getTone());
            case "octave" -> new ElementTag(event.getNote().getOctave());
            case "sharp" -> new ElementTag(event.getNote().isSharped());
            case "pitch" -> new ElementTag(Math.pow(2.0, (double) (event.getNote().getId() - 12) / 12.0)); // based on minecraft source
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onNotePlay(NotePlayEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

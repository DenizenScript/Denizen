package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
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
    // <context.tone> returns the the note tone played (A to G).
    // <context.octave> returns the octave the note is played at (as a number).
    // <context.sharp> returns a boolean indicating whether the note is sharp.
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
        return path.eventLower.startsWith("noteblock plays note");
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

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("instrument")) {
            return new ElementTag(event.getInstrument().name());
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
        return super.getContext(name);
    }

    @EventHandler
    public void onNotePlay(NotePlayEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

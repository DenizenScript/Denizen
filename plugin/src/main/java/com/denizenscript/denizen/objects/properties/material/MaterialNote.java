package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Note;
import org.bukkit.block.data.type.NoteBlock;

public class MaterialNote implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof NoteBlock;
    }

    public static MaterialNote getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialNote((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "note"
    };

    public MaterialNote(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.note_octave>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.note
        // @group properties
        // @description
        // Returns the octave of note played from this note block, as 0, 1, or 2.
        // -->
        PropertyParser.registerStaticTag(MaterialNote.class, ElementTag.class, "note_octave", (attribute, material) -> {
            return new ElementTag(material.getNoteBlock().getNote().getOctave());
        });

        // <--[tag]
        // @attribute <MaterialTag.note_tone>
        // @returns ElementTag
        // @mechanism MaterialTag.note
        // @group properties
        // @description
        // Returns the tone of note played from this note block, as a letter from A to F, sometimes with a # to indicate sharp.
        // Like A or A#.
        // -->
        PropertyParser.registerStaticTag(MaterialNote.class, ElementTag.class, "note_tone", (attribute, material) -> {
            Note note = material.getNoteBlock().getNote();
            return new ElementTag(note.getTone().name() + (note.isSharped() ? "#" : ""));
        });

        // <--[tag]
        // @attribute <MaterialTag.note>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.note
        // @group properties
        // @description
        // Returns the note played from this note block, as an ID number from 0 to 24.
        // -->
        PropertyParser.registerStaticTag(MaterialNote.class, ElementTag.class, "note", (attribute, material) -> {
            return new ElementTag(material.getNoteBlock().getNote().getId());
        });
    }

    public NoteBlock getNoteBlock() {
        return (NoteBlock) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getNoteBlock().getNote().getId());
    }

    @Override
    public String getPropertyId() {
        return "note";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name note
        // @input ElementTag(Number)
        // @description
        // Sets the note played from this note block, as an ID number from 0 to 24.
        // @tags
        // <MaterialTag.note>
        // <MaterialTag.note_tone>
        // <MaterialTag.note_octave>
        // -->
        if (mechanism.matches("note") && mechanism.requireInteger()) {
            getNoteBlock().setNote(new Note(mechanism.getValue().asInt()));
        }
    }
}

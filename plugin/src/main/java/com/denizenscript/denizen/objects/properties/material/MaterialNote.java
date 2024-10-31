package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Note;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;

public class MaterialNote extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name note
    // @input ElementTag(Number)
    // @description
    // Controls the note played from this note block, as an ID number from 0 to 24.
    // See also <@link tag MaterialTag.note_octave> and <@link tag MaterialTag.note_tone>
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof NoteBlock;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "note";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getNoteBlock().getNote().getId());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            getNoteBlock().setNote(new Note(value.asInt()));
        }
    }

    public static void register() {
        autoRegister("note", MaterialNote.class, ElementTag.class, true);

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
    }

    public NoteBlock getNoteBlock() {
        return (NoteBlock) material.getModernData();
    }
}

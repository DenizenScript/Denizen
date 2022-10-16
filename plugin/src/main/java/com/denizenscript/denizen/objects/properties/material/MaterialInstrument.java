package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Instrument;
import org.bukkit.block.data.type.NoteBlock;

public class MaterialInstrument implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof NoteBlock;
    }

    public static MaterialInstrument getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialInstrument((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "instrument"
    };

    private MaterialInstrument(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.instrument>
        // @returns ElementTag
        // @mechanism MaterialTag.instrument
        // @group properties
        // @description
        // Returns the name of the instrument played from this note block,
        // see list at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Instrument.html>.
        // For the instrument that a material *would* produce if below a noteblock <@link tag MaterialTag.produced_instrument>.
        // -->
        PropertyParser.registerStaticTag(MaterialInstrument.class, ElementTag.class, "instrument", (attribute, material) -> {
            return new ElementTag(material.getNoteBlock().getInstrument());
        });
    }

    public NoteBlock getNoteBlock() {
        return (NoteBlock) material.getModernData();
    }

    public void setInstrument(String instrument) {
        getNoteBlock().setInstrument(Instrument.valueOf(instrument));
    }

    @Override
    public String getPropertyString() {
        return getNoteBlock().getInstrument().name();
    }

    @Override
    public String getPropertyId() {
        return "instrument";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name instrument
        // @input ElementTag
        // @description
        // Sets the instrument played from this note block,
        // for valid instruments see list at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Instrument.html>.
        // @tags
        // <MaterialTag.instrument>
        // -->
        if (mechanism.matches("instrument") && mechanism.requireEnum(Instrument.class)) {
            setInstrument(mechanism.getValue().asString().toUpperCase());
        }
    }
}

package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Instrument;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;

public class MaterialInstrument extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name instrument
    // @input ElementTag
    // @description
    // Controls the name of the instrument played from this note block,
    // see list at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Instrument.html>.
    // For the instrument that a material *would* produce if below a noteblock <@link tag MaterialTag.produced_instrument>.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof NoteBlock;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "instrument";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getNoteBlock().getInstrument());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireEnum(Instrument.class)) {
            getNoteBlock().setInstrument(value.asEnum(Instrument.class));
        }
    }

    public static void register() {
        autoRegister("instrument", MaterialInstrument.class, ElementTag.class, true);
    }

    public NoteBlock getNoteBlock() {
        return (NoteBlock) material.getModernData();
    }
}

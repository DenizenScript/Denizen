package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

public class ItemInstrument extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name instrument
    // @input ElementTag
    // @description
    // A goat horn's instrument, if any.
    // Goat horns will default to playing "ponder_goat_horn" when the instrument is unset, although this is effectively random and shouldn't be relied on.
    // Valid instruments are: admire_goat_horn, call_goat_horn, dream_goat_horn, feel_goat_horn, ponder_goat_horn, seek_goat_horn, sing_goat_horn, yearn_goat_horn.
    // For the mechanism: provide no input to unset the instrument.
    // @example
    // # This can narrate: "This horn has the ponder_goat_horn instrument!"
    // - narrate "This horn has the <player.item_in_hand.instrument> instrument!"
    // @example
    // # Forces the player's held item to play seek_goat_horn instead of whatever it played before.
    // # Would break if the player isn't holding a goat horn.
    // - inventory adjust slot:hand instrument:seek_goat_horn
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof MusicInstrumentMeta;
    }

    @Override
    public ElementTag getPropertyValue() {
        MusicInstrument instrument = ((MusicInstrumentMeta) getItemMeta()).getInstrument();
        if (instrument != null) {
            return new ElementTag(Utilities.namespacedKeyToString(instrument.getKey()));
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        MusicInstrument instrument = value != null ? MusicInstrument.getByKey(Utilities.parseNamespacedKey(value.asString())) : null;
        if (value != null && instrument == null) {
            mechanism.echoError("Invalid instrument: " + value);
            return;
        }
        editMeta(MusicInstrumentMeta.class, meta -> meta.setInstrument(instrument));
    }

    @Override
    public String getPropertyId() {
        return "instrument";
    }

    public static void register() {
        autoRegisterNullable("instrument", ItemInstrument.class, ElementTag.class, false);
    }
}

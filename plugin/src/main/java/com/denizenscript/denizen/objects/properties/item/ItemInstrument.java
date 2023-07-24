package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

public class ItemInstrument extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name instrument
    // @input ElementTag
    // @description
    // Sets the instrument of a goat horn.
    // Here is a list of valid instruments: admire_goat_horn, call_goat_horn, dream_goat_horn, feel_goat_horn, ponder_goat_horn, seek_goat_horn, sing_goat_horn, yearn_goat_horn.
    // @example
    // # This can narrate: "This horn has the ponder_goat_horn instrument!"
    // - narrate "This horn has the <player.item_in_hand.instrument> instrument!"
    // @example
    // # Forces the player's held item to play seek_goat_horn instead of whatever it played before.
    // # Would break if the player isn't holding a goat horn.
    // - inventory adjust slot:hand instrument:seek_goat_horn
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() == Material.GOAT_HORN;
    }

    @Override
    public ElementTag getPropertyValue() {
        MusicInstrument instrument = getMusicInstrument();
        if (instrument != null) {
            return new ElementTag(Utilities.namespacedKeyToString(instrument.getKey()));
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        MusicInstrument instrument = MusicInstrument.getByKey(Utilities.parseNamespacedKey(param.asString()));
        if (instrument == null) {
            mechanism.echoError("Invalid horn instrument: '" + param + "'!");
            return;
        }
        setMusicInstrument(instrument);
    }

    @Override
    public String getPropertyId() {
        return "instrument";
    }

    public MusicInstrument getMusicInstrument() {
        MusicInstrumentMeta itemMeta = (MusicInstrumentMeta) getItemMeta();
        return itemMeta.getInstrument();
    }

    public void setMusicInstrument(MusicInstrument instrument) {
        MusicInstrumentMeta itemMeta = (MusicInstrumentMeta) getItemMeta();
        itemMeta.setInstrument(instrument);
        setItemMeta(itemMeta);
    }

    public static void register() {
        autoRegister("instrument", ItemInstrument.class, ElementTag.class, false);
    }
}

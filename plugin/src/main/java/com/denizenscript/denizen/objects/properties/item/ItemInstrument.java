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
    // For a list of possible instruments, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/MusicInstrument.html>.
    // @example
    // # This can narrate: "This horn has the ponder instrument!"
    // - narrate "This horn has the <player.item_in_hand.instrument> instrument!"
    // @example
    // # Forces the player's held item to play seek instead of whatever it played before.
    // # Would break if the player isn't holding a goat horn.
    // - inventory adjust slot:hand instrument:seek
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() == Material.GOAT_HORN;
    }

    @Override
    public ElementTag getPropertyValue() {
        MusicInstrument instrument = getMusicInstrument();
        if (instrument != null) {
            String key = Utilities.namespacedKeyToString(instrument.getKey()).replace("_goat_horn", "");
            return new ElementTag(key);
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        String key = param.asString().endsWith("_goat_horn") ? param.asString() : param.asString().concat("_goat_horn");
        MusicInstrument instrument = MusicInstrument.getByKey(Utilities.parseNamespacedKey(key));
        if (instrument == null) {
            mechanism.echoError("Invalid horn instrument: '" + param.asString() + "'!");
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

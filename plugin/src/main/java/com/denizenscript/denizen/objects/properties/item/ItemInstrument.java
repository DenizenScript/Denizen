package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

public class ItemInstrument extends ItemProperty {

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

        // <--[tag]
        // @attribute <ItemTag.instrument>
        // @returns ElementTag
        // @group properties
        // @mechanism ItemTag.instrument
        // @description
        // Returns the instrument of a goat horn.
        // For a list of possible instruments, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/MusicInstrument.html>.
        // @example
        // # This can narrate: "This horn has the ponder_goat_horn instrument!"
        // - narrate "This horn has the <player.item_in_hand.instrument> instrument!"
        // -->
        PropertyParser.registerTag(ItemInstrument.class, ElementTag.class, "instrument", (attribute, prop) -> {
            MusicInstrument musicInstrument = prop.getMusicInstrument();
            if (musicInstrument == null) {
                return null;
            }
            return new ElementTag(Utilities.namespacedKeyToString(musicInstrument.getKey()));
        });

        // <--[mechanism]
        // @object ItemTag
        // @name instrument
        // @input ElementTag
        // @description
        // Sets the instrument of a goat horn.
        // For a list of possible instruments, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/MusicInstrument.html>.
        // @tags
        // <ItemTag.instrument>
        // @example
        // - inventory adjust slot:hand instrument:seek_goat_horn
        // -->
        PropertyParser.registerMechanism(ItemInstrument.class, ElementTag.class, "instrument", (prop, mechanism, param) -> {
            MusicInstrument instrument = MusicInstrument.getByKey(Utilities.parseNamespacedKey(param.asString()));
            if (instrument == null) {
                mechanism.echoError("Invalid horn instrument: '" + param.asString() + "'! Instrument names should be like this: 'seek_goat_horn'.");
                return;
            }
            prop.setMusicInstrument(MusicInstrument.getByKey(Utilities.parseNamespacedKey(param.asString())));
        });
    }
}

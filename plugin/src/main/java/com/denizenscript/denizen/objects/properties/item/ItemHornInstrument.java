package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

public class ItemHornInstrument extends ItemProperty {

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() == Material.GOAT_HORN;
    }

    @Override
    public String getPropertyString() {
        MusicInstrument instrument = getMusicInstrument();
        if (instrument != null) {
            return Utilities.namespacedKeyToString(instrument.getKey());
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "horn_instrument";
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
        // @attribute <ItemTag.horn_instrument>
        // @returns ElementTag
        // @group properties
        // @mechanism ItemTag.horn_instrument
        // @description
        // Returns the instrument of a goat horn.
        // For a list of possible instruments, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/MusicInstrument.html>.
        // @example
        // # This can narrate: "This horn has the ponder_goat_horn instrument!"
        // - narrate "This horn has the <player.item_in_hand.horn_instrument> instrument!"
        // -->
        PropertyParser.registerTag(ItemHornInstrument.class, ElementTag.class, "horn_instrument", (attribute, prop) -> {
            MusicInstrument musicInstrument = prop.getMusicInstrument();
            if (musicInstrument == null) {
                return null;
            }
            return new ElementTag(Utilities.namespacedKeyToString(musicInstrument.getKey()));
        });

        // <--[mechanism]
        // @object EntityTag
        // @name horn_instrument
        // @input ElementTag
        // @description
        // Sets the instrument of a goat horn.
        // For a list of possible instruments, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/MusicInstrument.html>.
        // @tags
        // <EntityTag.horn_instrument>
        // @example
        // - adjust <player.item_in_hand> horn_instrument:seek_goat_horn
        // -->
        PropertyParser.registerMechanism(ItemHornInstrument.class, ElementTag.class, "horn_instrument", (prop, mechanism, param) -> {
            prop.setMusicInstrument(MusicInstrument.getByKey(Utilities.parseNamespacedKey(param.asString())));
        });
    }
}

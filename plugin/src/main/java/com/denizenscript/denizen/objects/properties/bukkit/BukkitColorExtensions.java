package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public class BukkitColorExtensions {

    public static ColorTag fromColor(Color color) {
        return new ColorTag(color.getRed(), color.getGreen(), color.getBlue(), NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? color.getAlpha() : 255);
    }

    public ColorTag fromDyeColor(DyeColor dyeColor) {
        return fromColor(dyeColor.getColor());
    }

    public static Color getColor(ColorTag tag) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            return Color.fromRGB(tag.red, tag.green, tag.blue);
        }
        return Color.fromARGB(tag.alpha, tag.red, tag.green, tag.blue);
    }

    public static void register() {

        // <--[tag]
        // @attribute <ColorTag.to_particle_offset>
        // @returns LocationTag
        // @description
        // Returns the color as a particle offset, for use with <@link command playeffect>.
        // @example
        // # Plays the "SPELL_MOB" effect above the player's head with the particle offset applied to color the particle.
        // - playeffect at:<player.location.add[0,3,0]> effect:SPELL_MOB data:1 quantity:0 offset:<color[fuchsia].to_particle_offset>
        // -->
        ColorTag.tagProcessor.registerStaticTag(LocationTag.class, "to_particle_offset", (attribute, object) -> {
            if (object.red + object.green + object.blue == 0) {
                return new LocationTag(null, 1 / 255f, 0, 0);
            }
            return new LocationTag(null, object.red / 255F, object.green / 255F, object.blue / 255F);
        });
    }
}

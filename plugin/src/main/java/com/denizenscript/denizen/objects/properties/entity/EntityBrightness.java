package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.entity.Display;

public class EntityBrightness extends EntityProperty<MapTag> {

    // <--[property]
    // @object EntityTag
    // @name brightness
    // @input MapTag
    // @description
    // A map of the display entity's brightness override (if any), containing "block" and "sky" keys, each with a brightness level between 0 and 15.
    // For the mechanism: provide no input to remove the brightness override.
    // @example
    // # Sets the brightness of the display entity to be as if the block was well lit by a torch in a cave (no sky light).
    // - adjust <[some_entity]> brightness:[sky=0;block=15]
    // @example
    // # Spawns a block_display entity of a stone that is middle brightness from both the sky and block sources.
    // - spawn block_display[material=stone;brightness=[sky=7;block=7]] <player.location>
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public MapTag getPropertyValue() {
        Display.Brightness brightness = as(Display.class).getBrightness();
        if (brightness == null) {
            return null;
        }
        MapTag brightnessMap = new MapTag();
        brightnessMap.putObject("block", new ElementTag(brightness.getBlockLight()));
        brightnessMap.putObject("sky", new ElementTag(brightness.getSkyLight()));
        return brightnessMap;
    }

    @Override
    public void setPropertyValue(MapTag value, Mechanism mechanism) {
        if (value == null) {
            as(Display.class).setBrightness(null);
            return;
        }
        Display.Brightness brightness = as(Display.class).getBrightness();
        ElementTag blockLightInput = value.getElement("block");
        int blockLight = blockLightInput != null ?
                blockLightInput.isInt() ? blockLightInput.asInt() : -1
                : brightness != null ? brightness.getBlockLight() : 0;
        if (blockLight < 0 || blockLight > 15) {
            mechanism.echoError("Invalid 'block' brightness, must be a number between 0 and 15.");
            return;
        }
        ElementTag skyLightInput = value.getElement("sky");
        int skyLight = skyLightInput != null ?
                skyLightInput.isInt() ? skyLightInput.asInt() : -1
                : brightness != null ? brightness.getSkyLight() : 0;
        if (skyLight < 0 || skyLight > 15) {
            mechanism.echoError("Invalid 'sky' brightness, must be a number between 0 and 15.");
            return;
        }
        as(Display.class).setBrightness(new Display.Brightness(blockLight, skyLight));
    }

    @Override
    public String getPropertyId() {
        return "brightness";
    }

    public static void register() {
        autoRegisterNullable("brightness", EntityBrightness.class, MapTag.class, false);
    }
}

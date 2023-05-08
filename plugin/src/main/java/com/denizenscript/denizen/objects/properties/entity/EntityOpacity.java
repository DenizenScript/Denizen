package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.TextDisplay;

public class EntityOpacity extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name opacity
    // @input ElementTag(Number)
    // @description
    // A text display entity's text opacity, from 0 to 255.
    // Can be interpolated, see <@link language Display entity interpolation>.
    // Note that there's currently an edge-case/bug where 0-3 are completely opaque, and it only becomes transparent at 4.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(Byte.toUnsignedInt(as(TextDisplay.class).getTextOpacity()));
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asInt() == 255;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            int opacity = value.asInt();
            if (opacity < 0 || opacity > 255) {
                mechanism.echoError("Invalid opacity specified, must be between 0 and 255.");
                return;
            }
            as(TextDisplay.class).setTextOpacity((byte) opacity);
        }
    }

    @Override
    public String getPropertyId() {
        return "opacity";
    }

    public static void register() {
        autoRegister("opacity", EntityOpacity.class, ElementTag.class, false);
    }
}

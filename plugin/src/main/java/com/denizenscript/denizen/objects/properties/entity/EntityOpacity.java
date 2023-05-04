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
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(TextDisplay.class).getTextOpacity() + 128);
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asInt() == 127; // Default value is -1, + 128
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            int opacity = value.asInt();
            if (opacity > 255 || opacity < 0) {
                mechanism.echoError("Invalid opacity specified, must be between 0 and 255.");
                return;
            }
            as(TextDisplay.class).setTextOpacity((byte) (opacity - 128));
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

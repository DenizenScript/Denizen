package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.TextDisplay;

public class EntityLineWidth extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name line_width
    // @input ElementTag(Number)
    // @description
    // A text display entity's line width, used to split lines (note that newlines can still be added as normal).
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(TextDisplay.class).getLineWidth());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asInt() == 200;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            as(TextDisplay.class).setLineWidth(value.asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "line_width";
    }

    public static void register() {
        autoRegister("line_width", EntityLineWidth.class, ElementTag.class, false);
    }
}

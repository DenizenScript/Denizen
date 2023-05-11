package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.TextDisplay;

public class EntityTextShadowed extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name text_shadowed
    // @input ElementTag(Boolean)
    // @description
    // Whether a text display entity's text has a shadow.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(TextDisplay.class).isShadowed());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return !value.asBoolean();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(TextDisplay.class).setShadowed(value.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "text_shadowed";
    }

    public static void register() {
        autoRegister("text_shadowed", EntityTextShadowed.class, ElementTag.class, false);
    }
}

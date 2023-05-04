package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.TextDisplay;

public class EntityDefaultBackground extends EntityProperty<ElementTag> {

    // TODO: reference the background color property here, once that's created.
    // <--[property]
    // @object EntityTag
    // @name default_background
    // @input ElementTag(Boolean)
    // @description
    // Whether a text display entity's background is the default (same as the chat window), or custom set.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(TextDisplay.class).isDefaultBackground());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return !value.asBoolean();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(TextDisplay.class).setDefaultBackground(value.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "default_background";
    }

    public static void register() {
        autoRegister("default_background", EntityDefaultBackground.class, ElementTag.class, false);
    }
}

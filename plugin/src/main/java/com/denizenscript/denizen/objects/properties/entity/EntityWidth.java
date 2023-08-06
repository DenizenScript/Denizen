package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;

public class EntityWidth extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name width
    // @input ElementTag(Decimal)
    // @description
    // For a display entity, this is the width of it's culling box. The box will span half the width in every direction from the entity's position.
    // The default value for these is 0, which disables culling entirely.
    // For an interaction entity, this is the width of it's bounding box (the area that can be interacted with).
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display || entity.getBukkitEntity() instanceof Interaction;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Display display) {
            return new ElementTag(display.getDisplayWidth());
        }
        return new ElementTag(as(Interaction.class).getInteractionWidth());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asFloat() == (getEntity() instanceof Display ? 0f : 1f);
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireFloat()) {
            return;
        }
        if (getEntity() instanceof Display display) {
            display.setDisplayWidth(value.asFloat());
            return;
        }
        as(Interaction.class).setInteractionWidth(value.asFloat());
    }

    @Override
    public String getPropertyId() {
        return "width";
    }

    public static void register() {
        autoRegister("width", EntityWidth.class, ElementTag.class, false);
    }
}

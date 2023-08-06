package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;

public class EntityHeight extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name height
    // @input ElementTag(Decimal)
    // @description
    // For a display entity, this is the height of it's culling box. The box will span from the entity's y to the entity's y + the height.
    // The default value for these is 0, which disables culling entirely.
    // For an interaction entity, this is the height of it's bounding box (the area that can be interacted with).
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display || entity.getBukkitEntity() instanceof Interaction;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Display display) {
            return new ElementTag(display.getDisplayHeight());
        }
        return new ElementTag(as(Interaction.class).getInteractionHeight());
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
            display.setDisplayHeight(value.asFloat());
            return;
        }
        as(Interaction.class).setInteractionHeight(value.asFloat());
    }

    @Override
    public String getPropertyId() {
        return "height";
    }

    public static void register() {
        autoRegister("height", EntityHeight.class, ElementTag.class, false);
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.TextDisplay;

public class EntitySeeThrough extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name see_through
    // @input ElementTag(Boolean)
    // @description
    // Whether a text display entity can be seen through blocks.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(TextDisplay.class).isSeeThrough());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return !value.asBoolean();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(TextDisplay.class).setSeeThrough(value.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "see_through";
    }

    public static void register() {
        autoRegister("see_through", EntitySeeThrough.class, ElementTag.class, false);
    }
}

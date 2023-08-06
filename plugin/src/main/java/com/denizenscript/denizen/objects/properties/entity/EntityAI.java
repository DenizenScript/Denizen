package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class EntityAI extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name has_ai
    // @input ElementTag(Boolean)
    // @description
    // Controls whether this entity will use the default Minecraft AI to roam and look around.
    // This tends to have implications for other vanilla functionality, including gravity.
    // This generally shouldn't be used with NPCs. NPCs do not have vanilla AI, regardless of what this tag returns.
    // Other programmatic methods of blocking AI might also not be accounted for by this tag.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.isLivingEntityType();
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getLivingEntity().hasAI());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            getLivingEntity().setAI(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "has_ai";
    }

    public static void register() {
        autoRegister("has_ai", EntityAI.class, ElementTag.class, false, "toggle_ai");
    }
}

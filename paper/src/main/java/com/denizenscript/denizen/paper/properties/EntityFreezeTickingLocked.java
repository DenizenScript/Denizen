package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class EntityFreezeTickingLocked extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name freeze_locked
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // Controls whether an entity’s freeze ticks are locked to a fixed value, preventing vanilla freeze tick updates.
    // NOTE: Clients may still show frost effects when in powder snow.
    // -->

    public static boolean describes(EntityTag entity) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getEntity().isFreezeTickingLocked());
    }

    @Override
    public String getPropertyId() {
        return "freeze_locked";
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            getEntity().lockFreezeTicks(param.asBoolean());
        }
    }

    public static void register() {
        autoRegister("freeze_locked", EntityFreezeTickingLocked.class, ElementTag.class, false);
    }
}

package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class EntityBodyStingers extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name body_stingers
    // @input ElementTag(Number)
    // @plugin Paper
    // @description
    // Controls the number of bee stingers stuck in an entity's body.
    // Note: Bee stingers will only be visible for players or player-type npcs.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.isLivingEntity();
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asInt() == 0;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getLivingEntity().getBeeStingersInBody());
    }

    @Override
    public String getPropertyId() {
        return "body_stingers";
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            getLivingEntity().setBeeStingersInBody(param.asInt());
        }
    }

    public static void register() {
        autoRegister("body_stingers", EntityBodyStingers.class, ElementTag.class, false);
    }
}

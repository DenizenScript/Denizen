package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class EntityStepHeight extends EntityProperty<ElementTag> {

    // TODO: 1.20.6: this can be controlled by an attribute now, can deprecate in favor of that & backsupport
    // <--[property]
    // @object EntityTag
    // @name step_height
    // @input ElementTag(Decimal)
    // @description
    // Controls the entity's step height, which controls how many blocks can it walk over.
    // As this is based on an internal value, it has some edge-cases, for example:
    // - most (but not all) living entities can still step over 1 block tall things as usual, even if this is set to 0.
    // - this doesn't apply to vehicles when the player is controlling them.
    // Note that this also applies to things like getting pushed.
    // -->

    public static boolean describes(EntityTag entity) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(NMSHandler.entityHelper.getStepHeight(getEntity()));
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireFloat()) {
            NMSHandler.entityHelper.setStepHeight(getEntity(), param.asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "step_height";
    }

    public static void register() {
        autoRegister("step_height", EntityStepHeight.class, ElementTag.class, false);
    }
}

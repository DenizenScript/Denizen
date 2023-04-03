package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class EntityStepHeight extends EntityProperty {

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(NMSHandler.entityHelper.getStepHeight(getEntity()));
    }

    @Override
    public String getPropertyId() {
        return "step_height";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.step_height>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.step_height
        // @group properties
        // @description
        // Returns the entity's step height, which controls how many blocks can it walk over.
        // As this is based on an internal value, it has some edge-cases, for example:
        // - most (but not all) living entities can still step over 1 block tall things as usual, even if this is set to 0.
        // - this doesn't apply to vehicles when the player is controlling them.
        // Note that this also applies to things like getting pushed.
        // -->
        PropertyParser.registerTag(EntityStepHeight.class, ElementTag.class, "step_height", (attribute, prop) -> {
            return prop.getPropertyValue();
        });

        // <--[mechanism]
        // @object EntityTag
        // @name step_height
        // @input ElementTag(Decimal)
        // @description
        // Sets the entity's step height, which controls how many blocks can it walk over.
        // As this is based on an internal value, it has some edge-cases, for example:
        // - most (but not all) living entities can still step over 1 block tall things as usual, even if this is set to 0.
        // - this doesn't apply to vehicles when the player is controlling them.
        // Note that this also applies to things like getting pushed.
        // @tags
        // <EntityTag.step_height>
        // -->
        PropertyParser.registerMechanism(EntityStepHeight.class, ElementTag.class, "step_height", (prop, mechanism, input) -> {
            if (mechanism.requireFloat()) {
                NMSHandler.entityHelper.setStepHeight(prop.getEntity(), input.asFloat());
            }
        });
    }
}

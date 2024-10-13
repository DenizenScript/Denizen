package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.Attribute;

@Deprecated(forRemoval = true)
public class EntityStepHeight extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name step_height
    // @input ElementTag(Decimal)
    // @deprecated Use the step height attribute on MC 1.20+.
    // @description
    // Deprecated in favor of the step height attribute on MC 1.20+, see <@link language Attribute Modifiers>.
    // -->

    public static boolean describes(EntityTag entity) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(NMSHandler.entityHelper.getStepHeight(getEntity()));
    }

    @Override
    public ElementTag getTagValue(Attribute attribute) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            BukkitImplDeprecations.entityStepHeight.warn(attribute.context);
        }
        return super.getTagValue(attribute);
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
            BukkitImplDeprecations.entityStepHeight.warn(mechanism.context);
        }
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

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class EntityVisualFire extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name visual_fire
    // @input ElementTag(Boolean)
    // @description
    // Whether an entity has a fake fire effect. For actual fire, see <@link command burn> and <@link tag EntityTag.on_fire>.
    // -->

    public static boolean describes(EntityTag entity) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getEntity().isVisualFire());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return !value.asBoolean();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            getEntity().setVisualFire(value.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "visual_fire";
    }

    public static void register() {
        autoRegister("visual_fire", EntityVisualFire.class, ElementTag.class, false);
    }
}

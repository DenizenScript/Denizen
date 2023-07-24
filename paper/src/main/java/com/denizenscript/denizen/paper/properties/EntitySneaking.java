package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class EntitySneaking extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name sneaking
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // Whether an entity is sneaking.
    // For most entities this just makes the name tag less visible, and doesn't actually update the pose.
    // Note that <@link command sneak> is also available.
    // -->

    public static boolean describes(EntityTag entity) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getEntity().isSneaking());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return !value.asBoolean();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            getEntity().setSneaking(value.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "sneaking";
    }

    public static void register() {
        autoRegister("sneaking", EntitySneaking.class, ElementTag.class, false);
    }
}

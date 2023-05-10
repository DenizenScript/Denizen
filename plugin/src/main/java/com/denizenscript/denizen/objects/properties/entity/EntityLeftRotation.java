package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.QuaternionTag;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

public class EntityLeftRotation extends EntityProperty<QuaternionTag> {

    // <--[property]
    // @object EntityTag
    // @name left_rotation
    // @input QuaternionTag
    // @description
    // A display entity's "left" rotation.
    // Note that <@link mechanism EntityTag.right_rotation> is also available, but should normally use this instead.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public QuaternionTag getPropertyValue() {
        Quaternionf leftRotation = as(Display.class).getTransformation().getLeftRotation();
        return new QuaternionTag(leftRotation.x(), leftRotation.y(), leftRotation.z(), leftRotation.w());
    }

    @Override
    public boolean isDefaultValue(QuaternionTag value) {
        return value.x == 0d && value.y == 0d && value.z == 0d && value.w == 1d;
    }

    @Override
    public void setPropertyValue(QuaternionTag value, Mechanism mechanism) {
        Transformation transformation = as(Display.class).getTransformation();
        transformation.getLeftRotation().set((float) value.x, (float) value.y, (float) value.z, (float) value.w);
        as(Display.class).setTransformation(transformation);
    }

    @Override
    public String getPropertyId() {
        return "left_rotation";
    }

    public static void register() {
        autoRegister("left_rotation", EntityLeftRotation.class, QuaternionTag.class, false);
    }
}

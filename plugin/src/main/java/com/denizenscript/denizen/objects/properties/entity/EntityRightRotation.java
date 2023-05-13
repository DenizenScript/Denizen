package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.QuaternionTag;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

public class EntityRightRotation extends EntityProperty<QuaternionTag> {

    // <--[property]
    // @object EntityTag
    // @name right_rotation
    // @input QuaternionTag
    // @description
    // A display entity's "right" rotation.
    // Should usually use <@link mechanism EntityTag.left_rotation> instead.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public QuaternionTag getPropertyValue() {
        Quaternionf rightRotation = as(Display.class).getTransformation().getRightRotation();
        return new QuaternionTag(rightRotation.x(), rightRotation.y(), rightRotation.z(), rightRotation.w());
    }

    @Override
    public boolean isDefaultValue(QuaternionTag value) {
        return value.x == 0d && value.y == 0d && value.z == 0d && value.w == 1d;
    }

    @Override
    public void setPropertyValue(QuaternionTag value, Mechanism mechanism) {
        Transformation transformation = as(Display.class).getTransformation();
        transformation.getRightRotation().set((float) value.x, (float) value.y, (float) value.z, (float) value.w);
        as(Display.class).setTransformation(transformation);
    }

    @Override
    public String getPropertyId() {
        return "right_rotation";
    }

    public static void register() {
        autoRegister("right_rotation", EntityRightRotation.class, QuaternionTag.class, false);
    }
}

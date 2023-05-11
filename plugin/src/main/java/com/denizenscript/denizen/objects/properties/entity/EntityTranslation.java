package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class EntityTranslation extends EntityProperty<LocationTag> {

    // <--[property]
    // @object EntityTag
    // @name translation
    // @input LocationTag
    // @description
    // A display entity's translation, represented as a <@link objecttype LocationTag> vector.
    // Can be interpolated, see <@link language Display entity interpolation>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public LocationTag getPropertyValue() {
        Vector3f translation = as(Display.class).getTransformation().getTranslation();
        return new LocationTag(null, translation.x(), translation.y(), translation.z());
    }

    @Override
    public boolean isDefaultValue(LocationTag value) {
        return value.getX() == 0d && value.getY() == 0d && value.getZ() == 0d;
    }

    @Override
    public void setPropertyValue(LocationTag value, Mechanism mechanism) {
        Transformation transformation = as(Display.class).getTransformation();
        transformation.getTranslation().set(value.getX(), value.getY(), value.getZ());
        as(Display.class).setTransformation(transformation);
    }

    @Override
    public String getPropertyId() {
        return "translation";
    }

    public static void register() {
        autoRegister("translation", EntityTranslation.class, LocationTag.class, false);
    }
}

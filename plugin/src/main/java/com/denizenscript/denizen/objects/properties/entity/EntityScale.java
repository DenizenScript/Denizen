package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class EntityScale extends EntityProperty<LocationTag> {

    // <--[property]
    // @object EntityTag
    // @name scale
    // @input LocationTag
    // @description
    // A display entity's scale, represented as a <@link objecttype LocationTag> vector.
    // Can be interpolated, see <@link language Display entity interpolation>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public LocationTag getPropertyValue() {
        Vector3f scale = as(Display.class).getTransformation().getScale();
        return new LocationTag(null, scale.x(), scale.y(), scale.z());
    }

    @Override
    public boolean isDefaultValue(LocationTag value) {
        return value.getX() == 1d && value.getY() == 1d && value.getZ() == 1d;
    }

    @Override
    public void setPropertyValue(LocationTag value, Mechanism mechanism) {
        Transformation transformation = as(Display.class).getTransformation();
        transformation.getScale().set(value.getX(), value.getY(), value.getZ());
        as(Display.class).setTransformation(transformation);
    }

    @Override
    public String getPropertyId() {
        return "scale";
    }

    public static void register() {
        autoRegister("scale", EntityScale.class, LocationTag.class, false);
    }
}

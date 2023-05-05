package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

import java.util.Arrays;

public class EntityDisplayEntityData extends EntityProperty<MapTag> {

    // <--[property]
    // @object EntityTag
    // @name display_entity_data
    // @input MapTag
    // @description
    // A map of Display Entity data. This is a placeholder until more-proper tools are developed.
    // This placeholder exists to enable you to play with the new entity type straight away. Details are subject to change. Be prepared to update your scripts soon if you use this.
    // Keys: transformation_left_rotation, transformation_right_rotation
    // (Note: rotations use a temporary ListTag format, subject to replacement).
    // For text displays: text_background_color
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    public static ListTag convertQuaternion(Quaternionf quat) {
        return new ListTag(Arrays.asList(new ElementTag(quat.x), new ElementTag(quat.y), new ElementTag(quat.z), new ElementTag(quat.w)));
    }

    public static void setToQuaternion(Quaternionf quaternion, ListTag list) {
        if (list == null) {
            return;
        }
        quaternion.set(list.getObject(0).asElement().asFloat(), list.getObject(1).asElement().asFloat(), list.getObject(2).asElement().asFloat(), list.getObject(3).asElement().asFloat());
    }

    @Override
    public MapTag getPropertyValue() {
        MapTag map = new MapTag();
        Transformation trans = as(Display.class).getTransformation();
        map.putObject("transformation_left_rotation", convertQuaternion(trans.getLeftRotation()));
        map.putObject("transformation_right_rotation", convertQuaternion(trans.getRightRotation()));
        if (getEntity() instanceof TextDisplay text && text.getBackgroundColor() != null) {
            map.putObject("text_background_color", BukkitColorExtensions.fromColor(text.getBackgroundColor()));
        }
        return map;
    }

    @Override
    public void setPropertyValue(MapTag value, Mechanism mechanism) {
        Display display = as(Display.class);
        ListTag leftRotation = value.getObjectAs("transformation_left_rotation", ListTag.class, mechanism.context);
        ListTag rightRotation = value.getObjectAs("transformation_right_rotation", ListTag.class, mechanism.context);
        if (leftRotation != null || rightRotation != null) {
            Transformation transformation = display.getTransformation();
            setToQuaternion(transformation.getLeftRotation(), leftRotation);
            setToQuaternion(transformation.getRightRotation(), rightRotation);
            display.setTransformation(transformation);
        }
        if (display instanceof TextDisplay text) {
            ColorTag backgroundColor = value.getObjectAs("text_background_color", ColorTag.class, mechanism.context);
            text.setBackgroundColor(backgroundColor == null ? null : BukkitColorExtensions.getColor(backgroundColor));
        }
    }

    @Override
    public String getPropertyId() {
        return "display_entity_data";
    }

    public static void register() {
        autoRegister("display_entity_data", EntityDisplayEntityData.class, MapTag.class, false);
    }
}

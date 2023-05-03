package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

import java.util.Arrays;

public class EntityDisplayEntityData implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag ent
                && ent.getBukkitEntity() instanceof Display;
    }

    public static EntityDisplayEntityData getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDisplayEntityData((EntityTag) entity);
        }
    }

    public EntityDisplayEntityData(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    public Display getDisplay() {
        return (Display) entity.getBukkitEntity();
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

    public MapTag getData() {
        Display display = getDisplay();
        MapTag map = new MapTag();
        Transformation trans = display.getTransformation();
        map.putObject("transformation_left_rotation", convertQuaternion(trans.getLeftRotation()));
        map.putObject("transformation_right_rotation", convertQuaternion(trans.getRightRotation()));
        if (display instanceof BlockDisplay block) {
            // handled by EntityTag.material
            //map.putObject("block_display", new MaterialTag(block.getBlock()));
        }
        else if (display instanceof TextDisplay text) {
            // TODO: Broken in current spigot build
            //if (text.getBackgroundColor() != null) {
                 // map.putObject("text_background_color", new ColorTag(text.getBackgroundColor()));
            //}
            map.putObject("text", new ElementTag(text.getText()));
            map.putObject("text_opacity", new ElementTag(text.getTextOpacity()));
            map.putObject("text_is_default_background", new ElementTag(text.isDefaultBackground()));
            map.putObject("text_is_see_through", new ElementTag(text.isSeeThrough()));
            map.putObject("text_is_shadowed", new ElementTag(text.isShadowed()));
        }
        return map;
    }

    @Override
    public String getPropertyString() {
        return getData().savable();
    }

    @Override
    public String getPropertyId() {
        return "display_entity_data";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.display_entity_data>
        // @returns MapTag
        // @mechanism EntityTag.display_entity_data
        // @group attributes
        // @description
        // Returns a map tag of Display Entity data. This is a placeholder until more-proper tools are developed.
        // This placeholder exists to enable you to play with the new entity type straight away. Details are subject to change. Be prepared to update your scripts soon if you use this.
        // Keys: transformation_left_rotation, transformation_right_rotation
        // (Note: rotations use a temporary ListTag format, subject to replacement).
        // For block displays: (N/A)
        // For text displays: text_background_color,  text, text_opacity, text_is_default_background, text_is_see_through, text_is_shadowed
        // -->
        PropertyParser.registerTag(EntityDisplayEntityData.class, MapTag.class, "display_entity_data", (attribute, object) -> {
            return object.getData();
        });

        // <--[mechanism]
        // @object EntityTag
        // @name display_entity_data
        // @input MapTag
        // @description
        // Returns a map tag of Display Entity data. This is a placeholder until more-proper tools are developed.
        // This placeholder exists to enable you to play with the new entity type straight away. Details are subject to change. Be prepared to update your scripts soon if you use this.
        // See <@link tag EntityTag.display_entity_data> for key details.
        // @tags
        // <EntityTag.display_entity_data>
        // -->
        PropertyParser.registerMechanism(EntityDisplayEntityData.class, MapTag.class, "display_entity_data", (object, mechanism, map) -> {
            Display display = object.getDisplay();
            ListTag leftRotation = map.getObjectAs("transformation_left_rotation", ListTag.class, mechanism.context);
            ListTag rightRotation = map.getObjectAs("transformation_right_rotation", ListTag.class, mechanism.context);
            if (leftRotation != null || rightRotation != null) {
                Transformation transformation = display.getTransformation();
                setToQuaternion(transformation.getLeftRotation(), leftRotation);
                setToQuaternion(transformation.getRightRotation(), rightRotation);
                display.setTransformation(transformation);
            }
            if (display instanceof BlockDisplay block) {
                //block.setBlock(map.getObjectAs("block_display", MaterialTag.class, mechanism.context).getModernData());
            }
            else if (display instanceof TextDisplay text) {
                if (map.getObject("text_background_color") != null) {
                    //text.setBackgroundColor(map.getObjectAs("text_background_color", ColorTag.class, mechanism.context).getColor());
                }
                else {
                    //text.setBackgroundColor(null);
                }
                text.setText(map.getElement("text", text.getText()).asString());
                text.setTextOpacity((byte) map.getElement("text_opacity", String.valueOf(text.getTextOpacity())).asInt());
                text.setDefaultBackground(map.getElement("text_is_default_background", String.valueOf(text.isDefaultBackground())).asBoolean());
                text.setSeeThrough(map.getElement("text_is_see_through", String.valueOf(text.isSeeThrough())).asBoolean());
                text.setShadowed(map.getElement("text_is_shadowed", String.valueOf(text.isShadowed())).asBoolean());
            }
        });
    }
}

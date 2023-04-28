package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.*;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    public static LocationTag convertVector(Vector3f vector) {
        return new LocationTag(null, vector.x, vector.y, vector.z);
    }

    public static ListTag convertQuaternion(Quaternionf quat) {
        return new ListTag(Arrays.asList(new ElementTag(quat.x), new ElementTag(quat.y), new ElementTag(quat.z), new ElementTag(quat.w)));
    }

    public static Vector3f toVector(LocationTag loc) {
        return new Vector3f((float) loc.getX(), (float) loc.getY(), (float) loc.getZ());
    }

    public static Quaternionf toQuaternion(ListTag list) {
        return new Quaternionf(list.getObject(0).asElement().asFloat(), list.getObject(1).asElement().asFloat(), list.getObject(2).asElement().asFloat(), list.getObject(3).asElement().asFloat());
    }

    public MapTag getData() {
        Display display = getDisplay();
        MapTag map = new MapTag();
        map.putObject("interpolation_delay", new DurationTag((long) display.getInterpolationDelay()));
        map.putObject("interpolation_duration", new DurationTag((long) display.getInterpolationDuration()));
        map.putObject("shadow_radius", new ElementTag(display.getShadowRadius()));
        map.putObject("shadow_strength", new ElementTag(display.getShadowStrength()));
        Transformation trans = display.getTransformation();
        map.putObject("transformation_left_rotation", convertQuaternion(trans.getLeftRotation()));
        map.putObject("transformation_right_rotation", convertQuaternion(trans.getRightRotation()));
        map.putObject("transformation_scale", convertVector(trans.getScale()));
        map.putObject("transformation_translation", convertVector(trans.getTranslation()));
        map.putObject("view_range", new ElementTag(display.getViewRange()));
        if (display instanceof BlockDisplay block) {
            // handled by EntityTag.material
            //map.putObject("block_display", new MaterialTag(block.getBlock()));
        }
        else if (display instanceof ItemDisplay item) {
            // "itemStack" handled by EntityTag.item
            map.putObject("item_transform", new ElementTag(item.getItemDisplayTransform()));
        }
        else if (display instanceof TextDisplay text) {
            map.putObject("text_alignment", new ElementTag(text.getAlignment()));
            // TODO: Broken in current spigot build
            //if (text.getBackgroundColor() != null) {
                 // map.putObject("text_background_color", new ColorTag(text.getBackgroundColor()));
            //}
            map.putObject("text_line_width", new ElementTag(text.getLineWidth()));
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
        // Keys: interpolation_delay, interpolation_duration, shadow_radius, shadow_strength, view_range,
        // transformation_left_rotation, transformation_right_rotation, transformation_scale, transformation_translation
        // (Note: rotations use a temporary ListTag format, subject to replacement).
        // For block displays: (N/A)
        // For item displays: item_transform
        // For text displays: text_alignment, text_background_color, text_line_width, text, text_opacity, text_is_default_background, text_is_see_through, text_is_shadowed
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
            if (map.getObject("interpolation_delay") != null) {
                display.setInterpolationDelay(map.getObjectAs("interpolation_delay", DurationTag.class, mechanism.context).getTicksAsInt());
                display.setInterpolationDuration(map.getObjectAs("interpolation_duration", DurationTag.class, mechanism.context).getTicksAsInt());
            }
            display.setShadowRadius(map.getElement("shadow_radius", String.valueOf(display.getShadowRadius())).asFloat());
            display.setShadowStrength(map.getElement("shadow_strength", String.valueOf(display.getShadowStrength())).asFloat());
            if (map.getObject("transformation_translation") != null) {
                Vector3f translation = toVector(map.getObjectAs("transformation_translation", LocationTag.class, mechanism.context));
                Vector3f scale = toVector(map.getObjectAs("transformation_scale", LocationTag.class, mechanism.context));
                Quaternionf leftRot = toQuaternion(map.getObjectAs("transformation_left_rotation", ListTag.class, mechanism.context));
                Quaternionf rightRot = toQuaternion(map.getObjectAs("transformation_right_rotation", ListTag.class, mechanism.context));
                display.setTransformation(new Transformation(translation, leftRot, scale, rightRot));
            }
            display.setViewRange(map.getElement("view_range", String.valueOf(display.getViewRange())).asFloat());
            if (display instanceof BlockDisplay block) {
                //block.setBlock(map.getObjectAs("block_display", MaterialTag.class, mechanism.context).getModernData());
            }
            else if (display instanceof ItemDisplay item) {
                item.setItemDisplayTransform(map.getElement("item_transform").asEnum(ItemDisplay.ItemDisplayTransform.class));
            }
            else if (display instanceof TextDisplay text) {
                text.setAlignment(map.getElement("text_alignment", text.getAlignment().name()).asEnum(TextDisplay.TextAlignment.class));
                if (map.getObject("text_background_color") != null) {
                    //text.setBackgroundColor(map.getObjectAs("text_background_color", ColorTag.class, mechanism.context).getColor());
                }
                else {
                    //text.setBackgroundColor(null);
                }
                text.setLineWidth(map.getElement("text_line_width", String.valueOf(text.getLineWidth())).asInt());
                text.setText(map.getElement("text", text.getText()).asString());
                text.setTextOpacity((byte) map.getElement("text_opacity", String.valueOf(text.getTextOpacity())).asInt());
                text.setDefaultBackground(map.getElement("text_is_default_background", String.valueOf(text.isDefaultBackground())).asBoolean());
                text.setSeeThrough(map.getElement("text_is_see_through", String.valueOf(text.isSeeThrough())).asBoolean());
                text.setShadowed(map.getElement("text_is_shadowed", String.valueOf(text.isShadowed())).asBoolean());
            }
        });
    }
}

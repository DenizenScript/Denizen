package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class EntityCustomNameVisible implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityCustomNameVisible getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCustomNameVisible((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "custom_name_visibility", "custom_name_visible"
    };

    public EntityCustomNameVisible(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return entity.getBukkitEntity().isCustomNameVisible() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "custom_name_visible";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.custom_name_visible>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.custom_name_visible
        // @group attributes
        // @description
        // Returns whether the entity's custom name is visible.
        // -->
        PropertyParser.registerTag(EntityCustomNameVisible.class, ElementTag.class, "custom_name_visible", (attribute, object) -> {
            return new ElementTag(object.entity.getBukkitEntity().isCustomNameVisible());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name custom_name_visible
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the entity's custom name is visible.
        // @tags
        // <EntityTag.custom_name_visible>
        // -->
        if ((mechanism.matches("custom_name_visibility") || mechanism.matches("custom_name_visible"))
                && mechanism.requireBoolean()) {
            entity.getBukkitEntity().setCustomNameVisible(mechanism.getValue().asBoolean());
        }
    }
}

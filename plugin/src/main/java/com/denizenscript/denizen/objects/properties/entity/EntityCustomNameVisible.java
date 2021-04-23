package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityCustomNameVisible implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityCustomNameVisible getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityCustomNameVisible((EntityTag) entity);
    }

    public static final String[] handledTags = new String[] {
            "custom_name_visible"
    };

    public static final String[] handledMechs = new String[] {
            "custom_name_visibility", "custom_name_visible"
    };

    private EntityCustomNameVisible(EntityTag ent) {
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

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.custom_name_visible>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.custom_name_visible
        // @group attributes
        // @description
        // Returns true if the entity's custom name is visible.
        // -->
        if (attribute.startsWith("custom_name_visible")) {
            return new ElementTag(entity.getBukkitEntity().isCustomNameVisible()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name custom_name_visible
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the custom name is visible.
        // @tags
        // <EntityTag.custom_name_visible>
        // -->
        if ((mechanism.matches("custom_name_visibility") || mechanism.matches("custom_name_visible"))
                && mechanism.requireBoolean()) {
            entity.getBukkitEntity().setCustomNameVisible(mechanism.getValue().asBoolean());
        }
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityCustomName implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity;
    }

    public static EntityCustomName getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityCustomName((dEntity) entity);
    }

    public static final String[] handledTags = new String[] {
            "custom_name_visible", "custom_name"
    };

    public static final String[] handledMechs = new String[] {
            "custom_name_visibility", "custom_name_visible", "custom_name"
    };

    private EntityCustomName(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    @Override
    public String getPropertyString() {
        String name = entity.getBukkitEntity().getCustomName();
        if (name == null) {
            return null;
        }
        else {
            return name;
        }
    }

    @Override
    public String getPropertyId() {
        return "custom_name";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.custom_name_visible>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns true if the entity's custom name is visible.
        // -->
        if (attribute.startsWith("custom_name_visible") || attribute.startsWith("custom_name.visible")) {
            int fulfilled = 1;
            if (attribute.startsWith("custom_name.visible")) {
                fulfilled = 2;
            }
            return new ElementTag(entity.getBukkitEntity().isCustomNameVisible()).getAttribute(attribute.fulfill(fulfilled));
        }

        // <--[tag]
        // @attribute <e@entity.custom_name>
        // @returns ElementTag
        // @group attributes
        // @description
        // Returns the entity's custom name, if any.
        // -->
        else if (attribute.startsWith("custom_name")) {
            String name = entity.getBukkitEntity().getCustomName();
            if (name == null) {
                return null;
            }
            else {
                return new ElementTag(name).getAttribute(attribute.fulfill(1));
            }
        }
        else {
            return null;
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name custom_name_visible
        // @input Element(Boolean)
        // @description
        // Sets whether the custom name is visible.
        // @tags
        // <e@entity.custom_name_visible>
        // -->
        if ((mechanism.matches("custom_name_visibility") || mechanism.matches("custom_name_visible"))
                && mechanism.requireBoolean()) {
            entity.getBukkitEntity().setCustomNameVisible(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object dEntity
        // @name custom_name
        // @input Element
        // @description
        // Sets the custom name of the entity.
        // @tags
        // <e@entity.custom_name>
        // -->
        else if (mechanism.matches("custom_name")) {
            entity.getBukkitEntity().setCustomName(mechanism.getValue().asString());
        }

    }
}

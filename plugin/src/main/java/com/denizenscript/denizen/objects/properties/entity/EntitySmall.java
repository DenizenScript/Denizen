package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;

public class EntitySmall implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntitySmall getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySmall((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "is_small"
    };

    public static final String[] handledMechs = new String[] {
            "is_small"
    };

    public EntitySmall(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (((ArmorStand) entity.getBukkitEntity()).isSmall()) {
            return "true";
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "is_small";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.is_small>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.is_small
        // @group properties
        // @description
        // Returns whether the armor stand is small.
        // -->
        if (attribute.startsWith("is_small")) {
            return new ElementTag(((ArmorStand) entity.getBukkitEntity()).isSmall())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name is_small
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the armor stand is small.
        // @tags
        // <EntityTag.is_small>
        // -->
        if (mechanism.matches("is_small") && mechanism.requireBoolean()) {
            ((ArmorStand) entity.getBukkitEntity()).setSmall(mechanism.getValue().asBoolean());
        }
    }
}

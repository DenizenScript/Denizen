package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowman;

public class EntityPumpkinHead implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag entityTag && entityTag.getBukkitEntity() instanceof Snowman;
    }

    public static EntityPumpkinHead getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPumpkinHead((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_pumpkin_head"
    };

    public static final String[] handledMechs = new String[] {
            "has_pumpkin_head"
    };

    public EntityPumpkinHead(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        return String.valueOf(!((Snowman) dentity.getBukkitEntity()).isDerp());
    }

    @Override
    public String getPropertyId() {
        return "has_pumpkin_head";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.has_pumpkin_head>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.has_pumpkin_head
        // @group properties
        // @description
        // If the entity is a snowman, returns whether the snowman has a pumpkin on its head.
        // -->
        if (attribute.startsWith("has_pumpkin_head")) {
            return new ElementTag(!((Snowman) dentity.getBukkitEntity()).isDerp())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name has_pumpkin_head
        // @input ElementTag(Boolean)
        // @description
        // Changes whether a Snowman entity has a pumpkin on its head.
        // @tags
        // <EntityTag.has_pumpkin_head>
        // -->
        if (mechanism.matches("has_pumpkin_head") && mechanism.requireBoolean()) {
            ((Snowman) dentity.getBukkitEntity()).setDerp(!mechanism.getValue().asBoolean());
        }
    }
}

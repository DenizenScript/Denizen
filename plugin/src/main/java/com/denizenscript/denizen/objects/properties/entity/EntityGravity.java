package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.trait.Gravity;

public class EntityGravity implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityGravity getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityGravity((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "gravity"
    };

    public static final String[] handledMechs = new String[] {
            "gravity"
    };

    public EntityGravity(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        if (dentity.getBukkitEntity().hasGravity()) {
            return null;
        }
        else {
            return "false";
        }
    }

    @Override
    public String getPropertyId() {
        return "gravity";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.gravity>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.gravity
        // @group properties
        // @description
        // Returns whether the entity has gravity.
        // -->
        if (attribute.startsWith("gravity")) {
            if (dentity.isCitizensNPC()) {
                boolean gravityBlocked = dentity.getDenizenNPC().getCitizen().hasTrait(Gravity.class)
                        && !dentity.getDenizenNPC().getCitizen().getOrAddTrait(Gravity.class).hasGravity();
                return new ElementTag(!gravityBlocked);
            }
            return new ElementTag(dentity.getBukkitEntity().hasGravity())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name gravity
        // @input ElementTag(Boolean)
        // @description
        // Changes the gravity state of an entity.
        // When set false (no gravity), side effects may also occur, eg all movement entirely being blocked.
        // @tags
        // <EntityTag.gravity>
        // -->
        if (mechanism.matches("gravity") && mechanism.requireBoolean()) {
            if (dentity.isCitizensNPC()) {
                dentity.getDenizenNPC().getCitizen().getOrAddTrait(Gravity.class).gravitate(!mechanism.getValue().asBoolean());
            }
            else {
                dentity.getBukkitEntity().setGravity(mechanism.getValue().asBoolean());
            }
        }
    }
}

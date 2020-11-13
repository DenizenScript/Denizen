package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.PiglinAbstract;

public class EntityImmune implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof PiglinAbstract;
    }

    public static EntityImmune getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityImmune((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "immune"
    };

    public static final String[] handledMechs = new String[] {
            "immune"
    };

    private EntityImmune(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    public PiglinAbstract getPiglin() {
        return (PiglinAbstract) dentity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getPiglin().isImmuneToZombification() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "immune";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.immune>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.immune
        // @group properties
        // @description
        // Returns whether this piglin entity is immune to zombification.
        // -->
        if (attribute.startsWith("immune")) {
            return new ElementTag(getPiglin().isImmuneToZombification())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name immune
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this piglin entity is immune to zombification.
        // @tags
        // <EntityTag.immune>
        // -->
        if (mechanism.matches("immune") && mechanism.requireBoolean()) {
            getPiglin().setImmuneToZombification(mechanism.getValue().asBoolean());
        }
    }
}

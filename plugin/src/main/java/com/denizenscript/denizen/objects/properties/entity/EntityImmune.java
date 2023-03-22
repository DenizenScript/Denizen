package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.PiglinAbstract;

public class EntityImmune implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && (((EntityTag) entity).getBukkitEntity() instanceof PiglinAbstract
                || ((EntityTag) entity).getBukkitEntity() instanceof Hoglin);
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

    public EntityImmune(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    public boolean isHoglin() {
        return dentity.getBukkitEntity() instanceof Hoglin;
    }

    public Hoglin getHoglin() {
        return (Hoglin) dentity.getBukkitEntity();
    }

    public PiglinAbstract getPiglin() {
        return (PiglinAbstract) dentity.getBukkitEntity();
    }

    public boolean getIsImmune() {
        if (isHoglin()) {
            return getHoglin().isImmuneToZombification();
        }
        return getPiglin().isImmuneToZombification();
    }

    @Override
    public String getPropertyString() {
        return getIsImmune() ? "true" : "false";
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
        // Returns whether this piglin or hoglin entity is immune to zombification.
        // -->
        if (attribute.startsWith("immune")) {
            return new ElementTag(getIsImmune())
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
        // Sets whether this piglin or hoglin entity is immune to zombification.
        // @tags
        // <EntityTag.immune>
        // -->
        if (mechanism.matches("immune") && mechanism.requireBoolean()) {
            if (isHoglin()) {
                getHoglin().setImmuneToZombification(mechanism.getValue().asBoolean());
            }
            else {
                getPiglin().setImmuneToZombification(mechanism.getValue().asBoolean());
            }
        }
    }
}

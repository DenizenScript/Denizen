package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;

public class EntityPowered implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.CREEPER;
    }

    public static EntityPowered getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPowered((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "powered"
    };

    public static final String[] handledMechs = new String[] {
            "powered"
    };

    public EntityPowered(EntityTag entity) {
        powered = entity;
    }

    EntityTag powered;

    public boolean getPowered() {
        return ((Creeper) (powered.getBukkitEntity())).isPowered();
    }

    public void setPowered(boolean power) {
        if (powered == null) {
            return;
        }

        ((Creeper) (powered.getBukkitEntity())).setPowered(power);
    }

    @Override
    public String getPropertyString() {
        if (!getPowered()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "powered";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.powered>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.powered
        // @group properties
        // @description
        // If the entity is a creeper, returns whether the creeper is powered.
        // -->
        if (attribute.startsWith("powered")) {
            return new ElementTag(getPowered())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name powered
        // @input ElementTag(Boolean)
        // @description
        // Changes the powered state of a Creeper.
        // @tags
        // <EntityTag.powered>
        // -->
        if (mechanism.matches("powered") && mechanism.requireBoolean()) {
            setPowered(mechanism.getValue().asBoolean());
        }
    }
}

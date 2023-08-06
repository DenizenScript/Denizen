package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Villager;

public class EntityVillagerLevel implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                (((EntityTag) entity).getBukkitEntity() instanceof Villager);
    }

    public static EntityVillagerLevel getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityVillagerLevel((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "villager_level"
    };

    public static final String[] handledMechs = new String[] {
            "villager_level"
    };

    public EntityVillagerLevel(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(((Villager) entity.getBukkitEntity()).getVillagerLevel());
    }

    @Override
    public String getPropertyId() {
        return "villager_level";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.villager_level>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.villager_level
        // @group properties
        // @description
        // Returns the level of a villager.
        // -->
        if (attribute.startsWith("villager_level")) {
            return new ElementTag(((Villager) entity.getBukkitEntity()).getVillagerLevel())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name villager_level
        // @input ElementTag(Number)
        // @description
        // Sets the level of a villager.
        // @tags
        // <EntityTag.villager_level>
        // -->
        if (mechanism.matches("villager_level") && mechanism.requireInteger()) {
            ((Villager) entity.getBukkitEntity()).setVillagerLevel(mechanism.getValue().asInt());
        }
    }
}

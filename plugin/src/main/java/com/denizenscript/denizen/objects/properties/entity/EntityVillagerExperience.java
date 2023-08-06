package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Villager;

public class EntityVillagerExperience implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                (((EntityTag) entity).getBukkitEntity() instanceof Villager);
    }

    public static EntityVillagerExperience getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityVillagerExperience((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "villager_experience"
    };

    public static final String[] handledMechs = new String[] {
            "villager_experience"
    };

    public EntityVillagerExperience(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(((Villager) entity.getBukkitEntity()).getVillagerExperience());
    }

    @Override
    public String getPropertyId() {
        return "villager_experience";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.villager_experience>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.villager_experience
        // @group properties
        // @description
        // Returns the experience amount of a villager.
        // -->
        if (attribute.startsWith("villager_experience")) {
            return new ElementTag(((Villager) entity.getBukkitEntity()).getVillagerExperience())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name villager_experience
        // @input ElementTag(Number)
        // @description
        // Sets the experience amount of a villager.
        // @tags
        // <EntityTag.villager_experience>
        // -->
        if (mechanism.matches("villager_experience") && mechanism.requireInteger()) {
            ((Villager) entity.getBukkitEntity()).setVillagerExperience(mechanism.getValue().asInt());
        }
    }
}

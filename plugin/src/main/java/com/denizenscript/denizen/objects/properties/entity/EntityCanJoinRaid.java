package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Raider;

public class EntityCanJoinRaid implements Property {

    // <--[property]
    // @object EntityTag
    // @name can_join_raid
    // @input ElementTag(Boolean)
    // @description
    // If the entity is raider mob (like a pillager), controls whether the entity is allowed to join active raids.
    // -->

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Raider;
    }

    public static EntityCanJoinRaid getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCanJoinRaid((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "can_join_raid"
    };

    public EntityCanJoinRaid(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        getRaider().setCanJoinRaid(mechanism.getValue().asBoolean());
    }

    @Override
    public String getPropertyString() {
        return getRaider().isCanJoinRaid() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "can_join_raid";
    }

    public Raider getRaider() {
        return (Raider) entity.getBukkitEntity();
    }

    public static void register() {
        PropertyParser.registerTag(EntityCanJoinRaid.class, ElementTag.class, "can_join_raid", (attribute, object) -> {
            return new ElementTag(object.getRaider().isCanJoinRaid());
        });
    }
}

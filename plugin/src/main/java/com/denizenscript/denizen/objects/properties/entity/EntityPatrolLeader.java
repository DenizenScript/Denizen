package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Raider;

public class EntityPatrolLeader implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Raider;
    }

    public static EntityPatrolLeader getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPatrolLeader((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "is_patrol_leader"
    };

    public EntityPatrolLeader(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return ((Raider) entity.getBukkitEntity()).isPatrolLeader() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "is_patrol_leader";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.is_patrol_leader>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.is_patrol_leader
        // @group properties
        // @description
        // If the entity is raider mob (like a pillager), returns whether the entity is a patrol leader.
        // -->
        PropertyParser.registerTag(EntityPatrolLeader.class, ElementTag.class, "is_patrol_leader", (attribute, object) -> {
            return new ElementTag(((Raider) object.entity.getBukkitEntity()).isPatrolLeader());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name is_patrol_leader
        // @input ElementTag(Boolean)
        // @description
        // If the entity is raider mob (like a pillager), changes whether the entity is a patrol leader.
        // @tags
        // <EntityTag.is_patrol_leader>
        // -->
        if (mechanism.matches("is_patrol_leader") && mechanism.requireBoolean()) {
            ((Raider) entity.getBukkitEntity()).setPatrolLeader(mechanism.getValue().asBoolean());
        }
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.Block;
import org.bukkit.entity.Raider;

public class EntityPatrolTarget implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Raider;
    }

    public static EntityPatrolTarget getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPatrolTarget((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "patrol_target"
    };

    public EntityPatrolTarget(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        Block target = ((Raider) entity.getBukkitEntity()).getPatrolTarget();
        if (target == null) {
            return null;
        }
        return new LocationTag(target.getLocation()).identify();
    }

    @Override
    public String getPropertyId() {
        return "patrol_target";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.patrol_target>
        // @returns LocationTag
        // @mechanism EntityTag.patrol_target
        // @group properties
        // @description
        // If the entity is raider mob (like a pillager), returns whether the entity is allowed to join active raids.
        // -->
        PropertyParser.registerTag(EntityPatrolTarget.class, LocationTag.class, "patrol_target", (attribute, object) -> {
            Block target = ((Raider) object.entity.getBukkitEntity()).getPatrolTarget();
            if (target == null) {
                return null;
            }
            return new LocationTag(target.getLocation());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name patrol_target
        // @input LocationTag
        // @description
        // If the entity is raider mob (like a pillager), changes whether the entity is allowed to join active raids.
        // @tags
        // <EntityTag.patrol_target>
        // -->
        if (mechanism.matches("patrol_target")) {
            if (mechanism.hasValue() && mechanism.requireObject(LocationTag.class)) {
                ((Raider) entity.getBukkitEntity()).setPatrolTarget(mechanism.valueAsType(LocationTag.class).getBlock());
            }
            else {
                ((Raider) entity.getBukkitEntity()).setPatrolTarget(null);
            }
        }
    }
}

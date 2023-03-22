package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Raider;

public class EntityCanJoinRaid implements Property {

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

        // <--[tag]
        // @attribute <EntityTag.can_join_raid>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.can_join_raid
        // @group properties
        // @description
        // If the entity is raider mob (like a pillager), returns whether the entity is allowed to join active raids.
        // -->
        PropertyParser.registerTag(EntityCanJoinRaid.class, ElementTag.class, "can_join_raid", (attribute, object) -> {
            return new ElementTag(object.getRaider().isCanJoinRaid());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name can_join_raid
        // @input ElementTag(Boolean)
        // @description
        // If the entity is raider mob (like a pillager), changes whether the entity is allowed to join active raids.
        // @tags
        // <EntityTag.can_join_raid>
        // -->
        if (mechanism.matches("can_join_raid") && mechanism.requireBoolean()) {
            getRaider().setCanJoinRaid(mechanism.getValue().asBoolean());
        }
    }
}

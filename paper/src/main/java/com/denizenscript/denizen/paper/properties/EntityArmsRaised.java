package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.destroystokyo.paper.entity.RangedEntity;

public class EntityArmsRaised implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof RangedEntity;
    }

    public static EntityArmsRaised getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityArmsRaised((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[]{
            "arms_raised"
    };

    private EntityArmsRaised(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.arms_raised>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.arms_raised
        // @group properties
        // @Plugin Paper
        // @description
        // Returns whether a ranged mob (skeleton, stray, wither skeleton, drowned, illusioner, or piglin) is "charging" up an attack (its arms are raised).
        // -->
        PropertyParser.registerTag(EntityArmsRaised.class, ElementTag.class, "arms_raised", (attribute, object) -> {
            return new ElementTag(object.getRanged().isChargingAttack());
        });
    }

    public RangedEntity getRanged() {
        return (RangedEntity) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getRanged().isChargingAttack());
    }

    @Override
    public String getPropertyId() {
        return "arms_raised";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name arms_raised
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @description
        // Sets whether a ranged mob (skeleton, stray, wither skeleton, drowned, illusioner, or piglin) is "charging" up an attack (its arms are raised).
        // Some entities may require <@link mechanism EntityTag.is_aware> to be set to false.
        // @tags
        // <EntityTag.arms_raised>
        // -->
        if (mechanism.matches("arms_raised") && mechanism.requireBoolean()) {
            getRanged().setChargingAttack(mechanism.getValue().asBoolean());
        }
    }
}

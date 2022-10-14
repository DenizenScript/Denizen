package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.destroystokyo.paper.entity.RangedEntity;

@Deprecated
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

    private EntityArmsRaised(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "arms_raised";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.arms_raised>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.arms_raised
        // @group properties
        // @Plugin Paper
        // @deprecated use 'aggressive'
        // @description
        // Deprecated in favor of <@link tag EntityTag.aggressive>.
        // -->
        PropertyParser.registerTag(EntityArmsRaised.class, ElementTag.class, "arms_raised", (attribute, object) -> {
            BukkitImplDeprecations.entityArmsRaised.warn(attribute.context);
            return new ElementTag(object.getRanged().isChargingAttack());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name arms_raised
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @deprecated use 'aggressive'
        // @description
        // Deprecated in favor of <@link mechanism EntityTag.aggressive>.
        // @tags
        // <EntityTag.arms_raised>
        // -->
        PropertyParser.registerMechanism(EntityArmsRaised.class, ElementTag.class, "arms_raised", (object, mechanism, input) -> {
            BukkitImplDeprecations.entityArmsRaised.warn(mechanism.context);
            if (mechanism.requireBoolean()) {
                object.getRanged().setChargingAttack(input.asBoolean());
            }
        });
    }

    public RangedEntity getRanged() {
        return (RangedEntity) entity.getBukkitEntity();
    }
}

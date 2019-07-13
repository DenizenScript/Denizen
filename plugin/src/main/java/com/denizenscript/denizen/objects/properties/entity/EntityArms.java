package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityArms implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityArms getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityArms((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "arms"
    };

    public static final String[] handledMechs = new String[] {
            "arms"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityArms(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!((ArmorStand) dentity.getBukkitEntity()).hasArms()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "arms";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <EntityTag.arms>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.arms
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand has arms.
        // -->
        if (attribute.startsWith("arms")) {
            return new ElementTag(((ArmorStand) dentity.getBukkitEntity()).hasArms())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name arms
        // @input Element(Boolean)
        // @description
        // Changes the arms state of an armor stand.
        // @tags
        // <EntityTag.arms>
        // -->

        if (mechanism.matches("arms") && mechanism.requireBoolean()) {
            ((ArmorStand) dentity.getBukkitEntity()).setArms(mechanism.getValue().asBoolean());
        }
    }
}

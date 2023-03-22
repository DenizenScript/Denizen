package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ArmorStand;

public class EntityArms implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntityArms getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityArms((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "arms"
    };

    public EntityArms(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        return getStand().hasArms() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "arms";
    }

    public ArmorStand getStand() {
        return (ArmorStand) dentity.getBukkitEntity();
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.arms>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.arms
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand has arms.
        // -->
        PropertyParser.registerTag(EntityArms.class, ElementTag.class, "arms", (attribute, object) -> {
            return new ElementTag(object.getStand().hasArms());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name arms
        // @input ElementTag(Boolean)
        // @description
        // Changes the arms state of an armor stand.
        // @tags
        // <EntityTag.arms>
        // -->
        if (mechanism.matches("arms") && mechanism.requireBoolean()) {
            getStand().setArms(mechanism.getValue().asBoolean());
        }
    }
}

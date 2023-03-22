package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Firework;

public class EntityShotAtAngle implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Firework;
    }

    public static EntityShotAtAngle getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityShotAtAngle((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "shot_at_angle"
    };

    public EntityShotAtAngle(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public Firework getFirework() {
        return (Firework) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getFirework().isShotAtAngle() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "shot_at_angle";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.shot_at_angle>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.shot_at_angle
        // @group properties
        // @description
        // Returns true if the Firework entity is 'shot at angle', meaning it should render facing the direction it's moving. If false, will be angled straight up.
        // -->
        PropertyParser.registerTag(EntityShotAtAngle.class, ElementTag.class, "shot_at_angle", (attribute, object) -> {
            return new ElementTag(object.getFirework().isShotAtAngle());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name shot_at_angle
        // @input ElementTag(Boolean)
        // @description
        // Set to true if the Firework entity should be 'shot at angle', meaning it should render facing the direction it's moving. If false, will be angled straight up.
        // @tags
        // <EntityTag.shot_at_angle>
        // -->
        if (mechanism.matches("shot_at_angle") && mechanism.requireBoolean()) {
            getFirework().setShotAtAngle(mechanism.getValue().asBoolean());
        }
    }
}

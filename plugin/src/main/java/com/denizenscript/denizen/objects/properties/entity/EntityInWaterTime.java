package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Zombie;

public class EntityInWaterTime implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Zombie;
    }

    public static EntityInWaterTime getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityInWaterTime((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "in_water_duration"
    };

    public EntityInWaterTime(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new DurationTag((long) NMSHandler.entityHelper.getInWaterTime((Zombie) entity.getBukkitEntity())).identify();
    }

    @Override
    public String getPropertyId() {
        return "in_water_duration";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.in_water_duration>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.in_water_duration
        // @group properties
        // @description
        // If the entity is a zombie mob, returns the duration of time the zombie has been in water for.
        // If this value exceeds 600 ticks, the zombie will begin converted to a Drowned mob.
        // See also <@link tag EntityTag.conversion_duration>
        // -->
        PropertyParser.registerTag(EntityInWaterTime.class, DurationTag.class, "in_water_duration", (attribute, object) -> {
            return new DurationTag((long) NMSHandler.entityHelper.getInWaterTime((Zombie) object.entity.getBukkitEntity()));
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name in_water_duration
        // @input ElementTag(Boolean)
        // @description
        // If the entity is a zombie mob, sets the duration of time the zombie has been in water for.
        // If this value exceeds 600 ticks, the zombie will begin converted to a Drowned mob.
        // See also <@link mechanism EntityTag.conversion_duration>
        // @tags
        // <EntityTag.in_water_duration>
        // -->
        if (mechanism.matches("in_water_duration") && mechanism.requireObject(DurationTag.class)) {
            NMSHandler.entityHelper.setInWaterTime((Zombie) entity.getBukkitEntity(), mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}

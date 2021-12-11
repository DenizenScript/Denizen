package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Zombie;

public class EntityDrownedConversionTime implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Zombie;
    }

    public static EntityDrownedConversionTime getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDrownedConversionTime((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "drowned_conversion_duration"
    };

    private EntityDrownedConversionTime(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new DurationTag((long) NMSHandler.getEntityHelper().getDrownedConversionTicks((Zombie) entity.getBukkitEntity())).identify();
    }

    @Override
    public String getPropertyId() {
        return "drowned_conversion_duration";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.drowned_conversion_duration>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.drowned_conversion_duration
        // @group properties
        // @description
        // If the entity is a zombie mob, returns the duration of time the zombie will be converting to a Drowned for.
        // If this value hits 0, the Zombie will become a Drowned.
        // See also <@link tag EntityTag.in_water_duration>
        // -->
        PropertyParser.<EntityDrownedConversionTime, DurationTag>registerTag(DurationTag.class, "drowned_conversion_duration", (attribute, object) -> {
            return new DurationTag((long) NMSHandler.getEntityHelper().getDrownedConversionTicks((Zombie) object.entity.getBukkitEntity()));
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name drowned_conversion_duration
        // @input ElementTag(Boolean)
        // @description
        // If the entity is a zombie mob, sets the duration of time the zombie will be converting to a Drowned for.
        // If this value hits 0, the Zombie will become a Drowned.
        // See also <@link mechanism EntityTag.in_water_duration>
        // @tags
        // <EntityTag.in_water_duration>
        // -->
        if (mechanism.matches("drowned_conversion_duration") && mechanism.requireObject(DurationTag.class)) {
            NMSHandler.getEntityHelper().setDrownedConversionTicks((Zombie) entity.getBukkitEntity(), mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}

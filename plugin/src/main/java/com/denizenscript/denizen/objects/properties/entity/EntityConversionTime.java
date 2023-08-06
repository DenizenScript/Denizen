package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.*;

public class EntityConversionTime implements Property {

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) object).getBukkitEntity();
        return (entity instanceof Zombie && !(entity instanceof PigZombie))
                || entity instanceof Skeleton;
    }

    public static EntityConversionTime getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityConversionTime((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "conversion_duration", "drowned_conversion_duration"
    };

    public EntityConversionTime(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (isConverting()) {
            return getConversionTime().identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "conversion_duration";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.conversion_duration>
        // @returns DurationTag
        // @mechanism EntityTag.conversion_duration
        // @group properties
        // @description
        // Returns the duration of time until an entity completes a conversion. See <@link tag EntityTag.is_converting> for examples of conversions.
        // When this value hits 0, the conversion completes.
        // See also <@link tag EntityTag.in_water_duration>
        // -->
        PropertyParser.registerTag(EntityConversionTime.class, DurationTag.class, "conversion_duration", (attribute, object) -> {
            if (!object.isConverting()) {
                attribute.echoError("This entity is not converting!");
                return null;
            }
            return object.getConversionTime();
        }, "drowned_conversion_duration");

        // <--[tag]
        // @attribute <EntityTag.is_converting>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether an entity is currently undergoing a conversion process. This can be:
        // A zombie villager being cured,
        // A zombie becoming a drowned (See also <@link tag EntityTag.in_water_duration>),
        // A husk becoming a zombie, or
        // A skeleton becoming a stray.
        // -->
        PropertyParser.registerTag(EntityConversionTime.class, ElementTag.class, "is_converting", (attribute, object) -> {
            return new ElementTag(object.isConverting());
        });
    }

    public boolean isZombie() {
        return entity.getBukkitEntity() instanceof Zombie;
    }

    public boolean isSkeleton() {
        return entity.getBukkitEntity() instanceof Skeleton;
    }

    public Zombie getZombie() {
        return (Zombie) entity.getBukkitEntity();
    }

    public Skeleton getSkeleton() {
        return (Skeleton) entity.getBukkitEntity();
    }

    public boolean isConverting() {
        if (isZombie()) {
            return getZombie().isConverting();
        }
        else if (isSkeleton()) {
            return getSkeleton().isConverting();
        }
        return false;
    }

    public DurationTag getConversionTime() {
        if (isZombie()) {
            return new DurationTag((long) getZombie().getConversionTime());
        }
        else if (isSkeleton()) {
            return new DurationTag((long) getSkeleton().getConversionTime());
        }
        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name conversion_duration
        // @input DurationTag
        // @description
        // Sets the duration of time until an entity completes a conversion. See <@link tag EntityTag.is_converting> for examples of conversions.
        // When this value hits 0, the conversion completes.
        // @tags
        // <EntityTag.conversion_duration>
        // -->
        if ((mechanism.matches("conversion_duration") || mechanism.matches("drowned_conversion_duration")) && mechanism.requireObject(DurationTag.class)) {
            if (isZombie()) {
                getZombie().setConversionTime(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
            }
            else if (isSkeleton()) {
                getSkeleton().setConversionTime(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
            }
        }
    }
}

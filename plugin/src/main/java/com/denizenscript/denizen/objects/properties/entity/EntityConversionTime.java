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

    private EntityConversionTime(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (getZombie().isConverting()) {
            return getConversionTime().identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "conversion_duration";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.conversion_duration>
        // @returns DurationTag
        // @mechanism EntityTag.conversion_duration
        // @group properties
        // @description
        // If the entity is a zombie villager, returns the duration of time until it will be cured.
        // If the entity is a zombie, husk, or skeleton, returns the duration of time the entity will be converting into a drowned, zombie, or stray for, respectively.
        // If this value hits 0, the conversion completes.
        // See also <@link tag EntityTag.in_water_duration>
        // -->
        PropertyParser.<EntityConversionTime, DurationTag>registerTag(DurationTag.class, "conversion_duration", (attribute, object) -> {
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
        // If the entity is a zombie villager, returns whether it is being cured.
        // If the entity is a zombie, husk, or skeleton, returns whether it is converting into a drowned, zombie, or stray, respectively.
        // See also <@link tag EntityTag.in_water_duration>
        // -->
        PropertyParser.<EntityConversionTime, ElementTag>registerTag(ElementTag.class, "is_converting", (attribute, object) -> {
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
        // If the entity is a zombie villager, sets the duration of time until it will be cured.
        // If the entity is a zombie, husk, or skeleton, sets the duration of time the entity will be converting into a drowned, zombie, or stray for, respectively.
        // If this value hits 0, the conversion completes.
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


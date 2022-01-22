package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;

public class EntityConversionTime implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Zombie;
    }

    public static EntityConversionTime getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityConversionTime((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "conversion_duration", "is_converting"
    };

    public static final String[] handledMechs = new String[] {
            "conversion_duration"
    };

    private EntityConversionTime(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return getConversionTime().identify();
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
        // If the entity is a zombie mob, returns the duration of time the zombie will be converting to a Drowned for.
        // If this value hits 0, the zombie villager will be cured, or if the entity is a zombie, it will become a Drowned.
        // See also <@link tag EntityTag.in_water_duration>
        // -->
        PropertyParser.<EntityConversionTime, DurationTag>registerStaticTag(DurationTag.class, "conversion_duration", (attribute, object) -> {
            return object.getConversionTime();
        });

        // <--[tag]
        // @attribute <EntityTag.is_converting>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // If the entity is a zombie villager, returns whether it is being cured.
        // If the entity is a zombie mob, returns whether it is converting into a Drowned.
        // See also <@link tag EntityTag.in_water_duration>
        // -->
        PropertyParser.<EntityConversionTime, ElementTag>registerStaticTag(ElementTag.class, "is_converting", (attribute, object) -> {
            return new ElementTag(object.getEntity().isConverting());
        });
    }

    public Zombie getEntity() {
        if (entity.getBukkitEntity() instanceof ZombieVillager) {
            return (ZombieVillager) entity.getBukkitEntity();
        }
        else {
            return (Zombie) entity.getBukkitEntity();
        }
    }

    public DurationTag getConversionTime() {
        return new DurationTag(getEntity().getConversionTime());
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name conversion_duration
        // @input DurationTag
        // @description
        // If the entity is a zombie villager, sets the amount of time until it will be cured.
        // If the entity is a zombie mob, sets the duration of time the zombie will be converting to a Drowned for.
        // If this value hits 0, the zombie villager will be cured, or if the entity is a zombie, it will become a Drowned.
        // @tags
        // <EntityTag.conversion_duration>
        // -->
        if (mechanism.matches("conversion_duration") && mechanism.requireObject(DurationTag.class)) {
            getEntity().setConversionTime(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}


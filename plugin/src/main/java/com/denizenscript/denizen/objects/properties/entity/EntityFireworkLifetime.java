package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Firework;

public class EntityFireworkLifetime implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Firework;
    }

    public static EntityFireworkLifetime getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFireworkLifetime((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "firework_lifetime"
    };

    private EntityFireworkLifetime(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public DurationTag getDuration() {
        return new DurationTag((long) NMSHandler.getEntityHelper().getFireworkLifetime((Firework) entity.getBukkitEntity()));
    }

    @Override
    public String getPropertyString() {
        return getDuration().identify();
    }

    @Override
    public String getPropertyId() {
        return "firework_lifetime";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.firework_lifetime>
        // @returns DurationTag
        // @mechanism EntityTag.firework_lifetime
        // @group properties
        // @description
        // Returns the duration that a firework will live for (before detonating).
        // -->
        PropertyParser.<EntityFireworkLifetime, DurationTag>registerTag(DurationTag.class, "firework_lifetime", (attribute, object) -> {
            return object.getDuration();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name firework_lifetime
        // @input ElementTag(Boolean)
        // @description
        // Sets the duration that a firework will live for (before detonating).
        // @tags
        // <EntityTag.firework_lifetime>
        // -->
        if (mechanism.matches("firework_lifetime") && mechanism.requireObject(DurationTag.class)) {
            NMSHandler.getEntityHelper().setFireworkLifetime((Firework) entity.getBukkitEntity(), mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}

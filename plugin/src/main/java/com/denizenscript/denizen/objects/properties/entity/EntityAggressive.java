package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Mob;

public class EntityAggressive implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof EntityTag
                && ((EntityTag) object).getBukkitEntity() instanceof Mob;
    }

    public static EntityAggressive getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAggressive((EntityTag) entity);
        }
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(NMSHandler.entityHelper.isAggressive(getMob()));
    }

    @Override
    public String getPropertyId() {
        return "aggressive";
    }

    public EntityAggressive(EntityTag entity) {
        this.entity = entity;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.aggressive>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.aggressive
        // @group properties
        // @description
        // Returns whether the entity is currently aggressive.
        // -->
        PropertyParser.registerTag(EntityAggressive.class, ElementTag.class, "aggressive", (attribute, object) -> {
            return new ElementTag(NMSHandler.entityHelper.isAggressive(object.getMob()));
        });

        // <--[mechanism]
        // @object EntityTag
        // @name aggressive
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the entity is currently aggressive.
        // @tags
        // <EntityTag.aggressive>
        // -->
        PropertyParser.registerMechanism(EntityAggressive.class, ElementTag.class, "aggressive", (object, mechanism, input) -> {
            if (mechanism.requireBoolean()) {
                NMSHandler.entityHelper.setAggressive(object.getMob(), input.asBoolean());
            }
        });
    }

    public Mob getMob() {
        return (Mob) entity.getBukkitEntity();
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Mob;

public class EntityAggressive extends EntityProperty {

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Mob;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(NMSHandler.entityHelper.isAggressive(getMob()));
    }

    @Override
    public String getPropertyId() {
        return "aggressive";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.aggressive>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.aggressive
        // @group properties
        // @description
        // Returns whether the entity is currently aggressive.
        // -->
        PropertyParser.registerTag(EntityAggressive.class, ElementTag.class, "aggressive", (attribute, prop) -> {
            return new ElementTag(NMSHandler.entityHelper.isAggressive(prop.getMob()));
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
        PropertyParser.registerMechanism(EntityAggressive.class, ElementTag.class, "aggressive", (prop, mechanism, input) -> {
            if (mechanism.requireBoolean()) {
                NMSHandler.entityHelper.setAggressive(prop.getMob(), input.asBoolean());
            }
        });
    }

    public Mob getMob() {
        return (Mob) getEntity();
    }
}

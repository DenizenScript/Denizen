package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Mob;

public class EntityAggressive extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name aggressive
    // @input ElementTag(Boolean)
    // @description
    // Controls whether the entity is currently aggressive.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Mob;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(NMSHandler.entityHelper.isAggressive(as(Mob.class)));
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            NMSHandler.entityHelper.setAggressive(as(Mob.class), param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "aggressive";
    }

    public static void register() {
        autoRegister("aggressive", EntityAggressive.class, ElementTag.class, false);
    }
}

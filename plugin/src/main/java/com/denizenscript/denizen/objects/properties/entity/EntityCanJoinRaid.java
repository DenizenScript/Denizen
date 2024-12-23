package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Raider;

public class EntityCanJoinRaid extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name can_join_raid
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a raider mob (like a pillager), is allowed to join active raids.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Raider;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Raider.class).isCanJoinRaid());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Raider.class).setCanJoinRaid(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "can_join_raid";
    }

    public static void register() {
        autoRegister("can_join_raid", EntityCanJoinRaid.class, ElementTag.class, false);
    }
}

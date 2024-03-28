package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.EnderSignal;

public class EntityDropItem extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name drop_item
    // @input ElementTag(Boolean)
    // @description
    // Whether an eye of ender drops an item when breaking or shatters.
    // See <@link property EntityTag.item> for controlling an eye's item.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof EnderSignal;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(EnderSignal.class).getDropItem());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(EnderSignal.class).setDropItem(value.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "drop_item";
    }

    public static void register() {
        autoRegister("drop_item", EntityDropItem.class, ElementTag.class, false);
    }
}

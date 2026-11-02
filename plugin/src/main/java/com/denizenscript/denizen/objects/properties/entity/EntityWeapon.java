package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.AbstractArrow;

public class EntityWeapon extends EntityProperty<ItemTag> {

    // <--[property]
    // @object EntityTag
    // @name weapon
    // @input ItemTag
    // @description
    // Controls the weapon that fired an arrow.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof AbstractArrow;
    }

    @Override
    public ItemTag getPropertyValue() {
        return new ItemTag(as(AbstractArrow.class).getWeapon());
    }

    @Override
    public void setPropertyValue(ItemTag item, Mechanism mechanism) {
        as(AbstractArrow.class).setWeapon(item.getItemStack());
    }

    @Override
    public String getPropertyId() {
        return "weapon";
    }

    public static void register() {
        autoRegister("weapon", EntityWeapon.class, ItemTag.class, false);
    }
}

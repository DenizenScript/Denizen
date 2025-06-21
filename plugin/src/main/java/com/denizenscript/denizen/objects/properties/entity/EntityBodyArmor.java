package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;

public class EntityBodyArmor extends EntityProperty<ItemTag> {

    // <--[property]
    // @object EntityTag
    // @name body_armor
    // @input ItemTag
    // @description
    // Controls the item that is on an entity's body.
    // This property is only valid on wolves, happy ghasts, and horse-type entities.
    // Note: Any item can be used, but only specific items will be visible on the entity.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof AbstractHorse
            || entity.getBukkitEntity() instanceof Wolf
            || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity.getBukkitEntity() instanceof HappyGhast);
    }

    @Override
    public ItemTag getPropertyValue() {
        return new ItemTag(getLivingEntity().getEquipment().getItem(EquipmentSlot.BODY));
    }

    @Override
    public void setPropertyValue(ItemTag item, Mechanism mechanism) {
        getLivingEntity().getEquipment().setItem(EquipmentSlot.BODY, item.getItemStack());
    }

    @Override
    public String getPropertyId() {
        return "body_armor";
    }

    public static void register() {
        autoRegister("body_armor", EntityBodyArmor.class, ItemTag.class, false);
    }
}

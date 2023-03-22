package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;

public class EntityItemInHand implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        if (!((EntityTag) entity).isLivingEntity()) {
            return false;
        }
        return true;
    }

    public static EntityItemInHand getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityItemInHand((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "item_in_hand"
    };

    public static final String[] handledMechs = new String[] {
            "item_in_hand"
    };

    public EntityItemInHand(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        ItemTag item = new ItemTag(entity.getLivingEntity().getEquipment().getItemInMainHand());
        if (item.getBukkitMaterial() != Material.AIR) {
            return item.identify();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "item_in_hand";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.item_in_hand>
        // @returns ItemTag
        // @mechanism EntityTag.item_in_hand
        // @group inventory
        // @description
        // Returns the item the entity is holding, or air if none.
        // -->
        if (attribute.startsWith("item_in_hand")) {
            if (!entity.isSpawnedOrValidForTag()) {
                return null;
            }
            return new ItemTag(entity.getLivingEntity().getEquipment().getItemInMainHand()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name item_in_hand
        // @input ItemTag
        // @description
        // Sets the item in the entity's hand.
        // The entity must be living.
        // @tags
        // <EntityTag.item_in_hand>
        // -->
        if (mechanism.matches("item_in_hand")) {
            entity.getLivingEntity().getEquipment().setItemInMainHand(mechanism.valueAsType(ItemTag.class).getItemStack());
        }
    }
}

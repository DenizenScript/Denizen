package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class EntityEquipment implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof LivingEntity;
    }

    public static EntityEquipment getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityEquipment((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "equipment"
    };

    public static final String[] handledMechs = new String[] {
            "equipment"
    };

    private EntityEquipment(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return entity.getEquipment().identify();
    }

    @Override
    public String getPropertyId() {
        return "equipment";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.equipment.boots>
        // @returns ItemTag
        // @group inventory
        // @description
        // Returns the item the entity is wearing as boots.
        // -->
        if (attribute.startsWith("equipment.boots")) {
            if (entity.getLivingEntity().getEquipment().getBoots() != null) {
                return new ItemTag(entity.getLivingEntity().getEquipment().getBoots())
                        .getObjectAttribute(attribute.fulfill(2));
            }
            else {
                return new ItemTag(Material.AIR)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.equipment.chestplate>
        // @returns ItemTag
        // @group inventory
        // @description
        // Returns the item the entity is wearing as a chestplate.
        // -->
        else if (attribute.startsWith("equipment.chestplate") ||
                attribute.startsWith("equipment.chest")) {
            if (entity.getLivingEntity().getEquipment().getChestplate() != null) {
                return new ItemTag(entity.getLivingEntity().getEquipment().getChestplate())
                        .getObjectAttribute(attribute.fulfill(2));
            }
            else {
                return new ItemTag(Material.AIR)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.equipment.helmet>
        // @returns ItemTag
        // @group inventory
        // @description
        // Returns the item the entity is wearing as a helmet.
        // -->
        else if (attribute.startsWith("equipment.helmet") ||
                attribute.startsWith("equipment.head")) {
            if (entity.getLivingEntity().getEquipment().getHelmet() != null) {
                return new ItemTag(entity.getLivingEntity().getEquipment().getHelmet())
                        .getObjectAttribute(attribute.fulfill(2));
            }
            else {
                return new ItemTag(Material.AIR)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.equipment.leggings>
        // @returns ItemTag
        // @group inventory
        // @description
        // Returns the item the entity is wearing as leggings.
        // -->
        else if (attribute.startsWith("equipment.leggings") ||
                attribute.startsWith("equipment.legs")) {
            if (entity.getLivingEntity().getEquipment().getLeggings() != null) {
                return new ItemTag(entity.getLivingEntity().getEquipment().getLeggings())
                        .getObjectAttribute(attribute.fulfill(2));
            }
            else {
                return new ItemTag(Material.AIR)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.equipment>
        // @returns ListTag
        // @mechanism EntityTag.equipment
        // @group inventory
        // @description
        // Returns a ListTag containing the entity's equipment.
        // Output list is boots|leggings|chestplate|helmet
        // -->
        else if (attribute.startsWith("equipment")) {
            return entity.getEquipment().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name equipment
        // @input ListTag(ItemTag)
        // @description
        // Sets the entity's worn equipment.
        // Input list is boots|leggings|chestplate|helmet
        // @tags
        // <EntityTag.equipment>
        // -->
        if (mechanism.matches("equipment")) {
            ListTag list = ListTag.valueOf(mechanism.getValue().asString(), mechanism.context);
            ItemStack[] stacks = new ItemStack[list.size()];
            for (int i = 0; i < list.size(); i++) {
                stacks[i] = ItemTag.valueOf(list.get(i), mechanism.context).getItemStack();
            }
            entity.getLivingEntity().getEquipment().setArmorContents(stacks);
        }
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class EntityEquipment implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof LivingEntity;
    }

    public static EntityEquipment getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityEquipment((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "equipment"
    };

    public static final String[] handledMechs = new String[] {
            "equipment"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityEquipment(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return entity.getEquipment().identify();
    }

    @Override
    public String getPropertyId() {
        return "equipment";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.equipment.boots>
        // @returns dItem
        // @group inventory
        // @description
        // Returns the item the entity is wearing as boots.
        // -->
        if (attribute.startsWith("equipment.boots")) {
            if (entity.getLivingEntity().getEquipment().getBoots() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getBoots())
                        .getAttribute(attribute.fulfill(2));
            }
            else {
                return new dItem(Material.AIR)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.chestplate>
        // @returns dItem
        // @group inventory
        // @description
        // Returns the item the entity is wearing as a chestplate.
        // -->
        else if (attribute.startsWith("equipment.chestplate") ||
                attribute.startsWith("equipment.chest")) {
            if (entity.getLivingEntity().getEquipment().getChestplate() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getChestplate())
                        .getAttribute(attribute.fulfill(2));
            }
            else {
                return new dItem(Material.AIR)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.helmet>
        // @returns dItem
        // @group inventory
        // @description
        // Returns the item the entity is wearing as a helmet.
        // -->
        else if (attribute.startsWith("equipment.helmet") ||
                attribute.startsWith("equipment.head")) {
            if (entity.getLivingEntity().getEquipment().getHelmet() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getHelmet())
                        .getAttribute(attribute.fulfill(2));
            }
            else {
                return new dItem(Material.AIR)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.leggings>
        // @returns dItem
        // @group inventory
        // @description
        // Returns the item the entity is wearing as leggings.
        // -->
        else if (attribute.startsWith("equipment.leggings") ||
                attribute.startsWith("equipment.legs")) {
            if (entity.getLivingEntity().getEquipment().getLeggings() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getLeggings())
                        .getAttribute(attribute.fulfill(2));
            }
            else {
                return new dItem(Material.AIR)
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment>
        // @returns ListTag
        // @group inventory
        // @description
        // Returns a ListTag containing the entity's equipment.
        // Output list is boots|leggings|chestplate|helmet
        // -->
        else if (attribute.startsWith("equipment")) {
            return entity.getEquipment().getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name equipment
        // @input ListTag(dItem)
        // @description
        // Sets the entity's worn equipment.
        // Input list is boots|leggings|chestplate|helmet
        // @tags
        // <e@entity.equipment>
        // -->
        if (mechanism.matches("equipment")) {
            ListTag list = ListTag.valueOf(mechanism.getValue().asString());
            ItemStack[] stacks = new ItemStack[list.size()];
            for (int i = 0; i < list.size(); i++) {
                stacks[i] = dItem.valueOf(list.get(i), mechanism.context).getItemStack();
            }
            entity.getLivingEntity().getEquipment().setArmorContents(stacks);
        }
    }
}

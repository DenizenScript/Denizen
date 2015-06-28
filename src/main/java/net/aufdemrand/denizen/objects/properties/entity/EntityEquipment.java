package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class EntityEquipment implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof LivingEntity;
    }

    public static EntityEquipment getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityEquipment((dEntity) entity);
    }


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
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.equipment.boots>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as boots, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.boots")) {
            if (entity.getLivingEntity().getEquipment().getBoots() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getBoots())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.chestplate>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as a chestplate, or null
        // if none.
        // -->
        else if (attribute.startsWith("equipment.chestplate") ||
                attribute.startsWith("equipment.chest")) {
            if (entity.getLivingEntity().getEquipment().getChestplate() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getChestplate())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.helmet>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as a helmet, or null
        // if none.
        // -->
        else if (attribute.startsWith("equipment.helmet") ||
                attribute.startsWith("equipment.head")) {
            if (entity.getLivingEntity().getEquipment().getHelmet() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getHelmet())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.leggings>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as leggings, or null
        // if none.
        // -->
        else if (attribute.startsWith("equipment.leggings") ||
                attribute.startsWith("equipment.legs")) {
            if (entity.getLivingEntity().getEquipment().getLeggings() != null) {
                return new dItem(entity.getLivingEntity().getEquipment().getLeggings())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment>
        // @returns dList
        // @group inventory
        // @description
        // returns a dList containing the entity's equipment.
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
        // @input dList(dItem)
        // @description
        // Sets the entity's worn equipment.
        // @tags
        // <e@entity.equipment>
        // -->
        if (mechanism.matches("equipment")) {
            dList list = dList.valueOf(mechanism.getValue().asString());
            ItemStack[] stacks = new ItemStack[list.size()];
            for (int i = 0; i < list.size(); i++) {
                stacks[i] = dItem.valueOf(list.get(i)).getItemStack();
            }
            entity.getLivingEntity().getEquipment().setArmorContents(stacks);
        }
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.trait.trait.Equipment;
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
            "equipment", "equipment_map"
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

        if (attribute.startsWith("equipment.boots")) {
            Deprecations.entityEquipmentSubtags.warn(attribute.context);
            if (entity.getLivingEntity().getEquipment().getBoots() != null) {
                return new ItemTag(entity.getLivingEntity().getEquipment().getBoots())
                        .getObjectAttribute(attribute.fulfill(2));
            }
            else {
                return new ItemTag(Material.AIR)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        }

        else if (attribute.startsWith("equipment.chestplate") ||
                attribute.startsWith("equipment.chest")) {
            Deprecations.entityEquipmentSubtags.warn(attribute.context);
            if (entity.getLivingEntity().getEquipment().getChestplate() != null) {
                return new ItemTag(entity.getLivingEntity().getEquipment().getChestplate())
                        .getObjectAttribute(attribute.fulfill(2));
            }
            else {
                return new ItemTag(Material.AIR)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        }

        else if (attribute.startsWith("equipment.helmet") ||
                attribute.startsWith("equipment.head")) {
            Deprecations.entityEquipmentSubtags.warn(attribute.context);
            if (entity.getLivingEntity().getEquipment().getHelmet() != null) {
                return new ItemTag(entity.getLivingEntity().getEquipment().getHelmet())
                        .getObjectAttribute(attribute.fulfill(2));
            }
            else {
                return new ItemTag(Material.AIR)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        }

        else if (attribute.startsWith("equipment.leggings") ||
                attribute.startsWith("equipment.legs")) {
            Deprecations.entityEquipmentSubtags.warn(attribute.context);
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
        // @returns ListTag(ItemTag)
        // @mechanism EntityTag.equipment
        // @group inventory
        // @description
        // Returns a ListTag containing the entity's equipment.
        // Output list is boots|leggings|chestplate|helmet
        // -->
        else if (attribute.startsWith("equipment")) {
            return entity.getEquipment().getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.equipment_map>
        // @returns MapTag
        // @mechanism EntityTag.equipment
        // @group inventory
        // @description
        // Returns a MapTag containing the entity's equipment.
        // Output keys are boots, leggings, chestplate, helmet.
        // Air items will be left out of the map.
        // -->
        else if (attribute.startsWith("equipment_map")) {
            MapTag output = new MapTag();
            org.bukkit.inventory.EntityEquipment equip = entity.getLivingEntity().getEquipment();
            InventoryTag.addToMapIfNonAir(output, "boots", equip.getBoots());
            InventoryTag.addToMapIfNonAir(output, "leggings", equip.getLeggings());
            InventoryTag.addToMapIfNonAir(output, "chestplate", equip.getChestplate());
            InventoryTag.addToMapIfNonAir(output, "helmet", equip.getHelmet());
            return output.getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name equipment
        // @input MapTag
        // @description
        // Sets the entity's worn equipment.
        // Input keys are boots, leggings, chestplate, and helmet.
        // @tags
        // <EntityTag.equipment>
        // <EntityTag.equipment_map>
        // -->
        if (mechanism.matches("equipment") && mechanism.hasValue()) {
            org.bukkit.inventory.EntityEquipment equip = entity.getLivingEntity().getEquipment();
            if (mechanism.value.canBeType(MapTag.class)) {
                MapTag map = mechanism.valueAsType(MapTag.class);
                ObjectTag boots = map.getObject("boots");
                if (boots != null) {
                    ItemStack bootsItem = boots.asType(ItemTag.class, mechanism.context).getItemStack();
                    if (entity.isCitizensNPC()) {
                        entity.getDenizenNPC().getEquipmentTrait().set(Equipment.EquipmentSlot.BOOTS, bootsItem);
                    }
                    else {
                        equip.setBoots(bootsItem);
                    }
                }
                ObjectTag leggings = map.getObject("leggings");
                if (leggings != null) {
                    ItemStack leggingsItem = leggings.asType(ItemTag.class, mechanism.context).getItemStack();
                    if (entity.isCitizensNPC()) {
                        entity.getDenizenNPC().getEquipmentTrait().set(Equipment.EquipmentSlot.LEGGINGS, leggingsItem);
                    }
                    else {
                        equip.setLeggings(leggingsItem);
                    }
                }
                ObjectTag chestplate = map.getObject("chestplate");
                if (chestplate != null) {
                    ItemStack chestplateItem = chestplate.asType(ItemTag.class, mechanism.context).getItemStack();
                    if (entity.isCitizensNPC()) {
                        entity.getDenizenNPC().getEquipmentTrait().set(Equipment.EquipmentSlot.CHESTPLATE, chestplateItem);
                    }
                    else {
                        equip.setChestplate(chestplateItem);
                    }
                }
                ObjectTag helmet = map.getObject("helmet");
                if (helmet != null) {
                    ItemStack helmetItem = helmet.asType(ItemTag.class, mechanism.context).getItemStack();
                    if (entity.isCitizensNPC()) {
                        entity.getDenizenNPC().getEquipmentTrait().set(Equipment.EquipmentSlot.HELMET, helmetItem);
                    }
                    else {
                        equip.setHelmet(helmetItem);
                    }
                }
            }
            else { // Soft-deprecate: no warn, but long term back-support
                ListTag list = mechanism.valueAsType(ListTag.class);
                ItemStack[] stacks = new ItemStack[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    stacks[i] = ItemTag.valueOf(list.get(i), mechanism.context).getItemStack();
                }
                equip.setArmorContents(stacks);
            }
        }
    }
}

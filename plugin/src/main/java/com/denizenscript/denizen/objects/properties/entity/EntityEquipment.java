package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EntityEquipment implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).isLivingEntity();
    }

    public static EntityEquipment getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityEquipment((EntityTag) entity);
        }
    }

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

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.equipment>
        // @returns ListTag(ItemTag)
        // @mechanism EntityTag.equipment
        // @group inventory
        // @description
        // Returns a ListTag containing the entity's equipment.
        // Output list is boots|leggings|chestplate|helmet
        // -->
        PropertyParser.<EntityEquipment, ObjectTag>registerTag(ObjectTag.class, "equipment", (attribute, object) -> {
            org.bukkit.inventory.EntityEquipment equipment = object.entity.getLivingEntity().getEquipment();
            if (attribute.startsWith("equipment.boots")) {
                Deprecations.entityEquipmentSubtags.warn(attribute.context);
                attribute.fulfill(1);
                ItemStack boots = equipment.getBoots();
                return new ItemTag(boots != null ? boots : new ItemStack(Material.AIR));
            }
            else if (attribute.startsWith("equipment.chestplate") || attribute.startsWith("equipment.chest")) {
                Deprecations.entityEquipmentSubtags.warn(attribute.context);
                attribute.fulfill(1);
                ItemStack chestplate = equipment.getChestplate();
                return new ItemTag(chestplate != null ? chestplate : new ItemStack(Material.AIR));
            }
            else if (attribute.startsWith("equipment.helmet") || attribute.startsWith("equipment.head")) {
                Deprecations.entityEquipmentSubtags.warn(attribute.context);
                attribute.fulfill(1);
                ItemStack helmet = equipment.getHelmet();
                return new ItemTag(helmet != null ? helmet : new ItemStack(Material.AIR));
            }
            else if (attribute.startsWith("equipment.leggings") || attribute.startsWith("equipment.legs")) {
                Deprecations.entityEquipmentSubtags.warn(attribute.context);
                attribute.fulfill(1);
                ItemStack leggings = equipment.getLeggings();
                return new ItemTag(leggings != null ? leggings : new ItemStack(Material.AIR));
            }
            return object.entity.getEquipment();
        });

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
        PropertyParser.<EntityEquipment, MapTag>registerTag(MapTag.class, "equipment_map", (attribute, object) -> {
            MapTag output = new MapTag();
            org.bukkit.inventory.EntityEquipment equip = object.entity.getLivingEntity().getEquipment();
            InventoryTag.addToMapIfNonAir(output, "boots", equip.getBoots());
            InventoryTag.addToMapIfNonAir(output, "leggings", equip.getLeggings());
            InventoryTag.addToMapIfNonAir(output, "chestplate", equip.getChestplate());
            InventoryTag.addToMapIfNonAir(output, "helmet", equip.getHelmet());
            return output;
        });
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

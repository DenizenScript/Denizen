package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.inventory.EntityEquipment;

public class EntityEquipmentDropChance extends EntityProperty<MapTag> {

    // <--[property]
    // @object EntityTag
    // @name equipment_drop_chance
    // @input MapTag
    // @description
    // Controls the chance of each
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getLivingEntity() instanceof EntityTag;
    }

    @Override
    public MapTag getPropertyValue() {
        EntityEquipment equipment = getLivingEntity().getEquipment();
        if (equipment == null) {
            return null;
        }
        MapTag map = new MapTag();
        if (equipment.getHelmet() != null) {
            map.putObject("helmet", new ElementTag(equipment.getHelmetDropChance()));
        }
        if (equipment.getChestplate() != null) {
            map.putObject("chestplate", new ElementTag(equipment.getChestplateDropChance()));
        }
        if (equipment.getLeggings() != null) {
            map.putObject("leggings", new ElementTag(equipment.getLeggingsDropChance()));
        }
        if (equipment.getBoots() != null) {
            map.putObject("boots", new ElementTag(equipment.getBootsDropChance()));
        }
        map.putObject("main_hand", new ElementTag(equipment.getItemInMainHandDropChance()));
        map.putObject("off_hand", new ElementTag(equipment.getItemInOffHandDropChance()));
        return map;
    }

    @Override
    public void setPropertyValue(MapTag map, Mechanism mechanism) {
        ElementTag helmet = map.getObject("helmet").asElement();
        ElementTag chestplate = map.getObject("chestplate").asElement();
        ElementTag leggings = map.getObject("leggings").asElement();
        ElementTag boots = map.getObject("boots").asElement();
        ElementTag main_hand = map.getObject("main_hand").asElement();
        ElementTag off_hand = map.getObject("off_hand").asElement();
        EntityEquipment equipment = getLivingEntity().getEquipment();
        if (equipment == null) {
            return;
        }
        if (helmet != null) {
            equipment.setHelmetDropChance(helmet.asFloat());
        }
        if (chestplate != null) {
            equipment.setChestplateDropChance(chestplate.asFloat());
        }
        if (leggings != null) {
            equipment.setLeggingsDropChance(leggings.asFloat());
        }
        if (boots != null) {
            equipment.setBootsDropChance(boots.asFloat());
        }
        if (main_hand != null) {
            equipment.setItemInMainHandDropChance(main_hand.asFloat());
        }
        if (off_hand != null) {
            equipment.setItemInOffHandDropChance(off_hand.asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "equipment_drop_chance";
    }

    public static void register() {
        autoRegister("equipment_drop_chance", EntityEquipmentDropChance.class, MapTag.class, false);
    }
}

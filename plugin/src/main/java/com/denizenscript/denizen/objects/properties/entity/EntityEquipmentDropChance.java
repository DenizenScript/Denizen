package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;

public class EntityEquipmentDropChance extends EntityProperty<MapTag> {

    // <--[property]
    // @object EntityTag
    // @name equipment_drop_chance
    // @input MapTag
    // @description
    // Controls the chance of each piece of equipment dropping when the entity dies.
    // A drop chance of 0 will prevent the item from dropping, a drop chance of 1 will always drop the item if killed by a player, and a drop chance of higher than 1 will always drop the item no matter what the entity was killed by.
    //
    // @Mechanism-example
    // # Use to prevent a zombie from dropping any of its equipped items, no matter what:
    // - adjust <[zombie]> equipment_drop_chance:[helmet=0;chestplate=0;leggings=0;boots=0;main_hand=0;off_hand=0]
    //
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Mob;
    }

    @Override
    public MapTag getPropertyValue() {
        EntityEquipment equipment = getLivingEntity().getEquipment();
        MapTag map = new MapTag();
        map.putObject("head", new ElementTag(equipment.getHelmetDropChance()));
        map.putObject("chest", new ElementTag(equipment.getChestplateDropChance()));
        map.putObject("legs", new ElementTag(equipment.getLeggingsDropChance()));
        map.putObject("feet", new ElementTag(equipment.getBootsDropChance()));
        map.putObject("hand", new ElementTag(equipment.getItemInMainHandDropChance()));
        map.putObject("off_hand", new ElementTag(equipment.getItemInOffHandDropChance()));
        return map;
    }

    @Override
    public void setPropertyValue(MapTag map, Mechanism mechanism) {
        EntityEquipment equipment = getLivingEntity().getEquipment();
        ElementTag helmet = map.getElement("helmet");
        ElementTag chestplate = map.getElement("chestplate");
        ElementTag leggings = map.getElement("leggings");
        ElementTag boots = map.getElement("boots");
        ElementTag mainHand = map.getElement("main_hand");
        ElementTag offHand = map.getElement("off_hand");
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
        if (mainHand != null) {
            equipment.setItemInMainHandDropChance(mainHand.asFloat());
        }
        if (offHand != null) {
            equipment.setItemInOffHandDropChance(offHand.asFloat());
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

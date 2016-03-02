package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_9_R1.EnumItemSlot;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Map;

public class EntityEquipment {

    public enum EquipmentSlots {
        HAND(EnumItemSlot.MAINHAND),
        MAIN_HAND(EnumItemSlot.MAINHAND),
        OFF_HAND(EnumItemSlot.OFFHAND),
        BOOTS(EnumItemSlot.FEET),
        LEGS(EnumItemSlot.LEGS),
        CHEST(EnumItemSlot.CHEST),
        HEAD(EnumItemSlot.HEAD);

        private EnumItemSlot slot;

        EquipmentSlots(EnumItemSlot slot) {
            this.slot = slot;
        }

        public EnumItemSlot getSlot() {
            return slot;
        }
    }

    private static final Field equipment_entityId, equipment_slot, equipment_itemstack;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutEntityEquipment.class);
        equipment_entityId = fields.get("a");
        equipment_slot = fields.get("b");
        equipment_itemstack = fields.get("c");
    }

    public static PacketPlayOutEntityEquipment getEquipmentPacket(LivingEntity entity, EnumItemSlot slot, ItemStack item) {
        PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment();
        try {
            equipment_entityId.set(equipmentPacket, entity.getEntityId());
            equipment_slot.set(equipmentPacket, slot);
            equipment_itemstack.set(equipmentPacket, CraftItemStack.asNMSCopy(item));
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return equipmentPacket;
    }

    public static void showEquipment(Player player, LivingEntity entity, EquipmentSlots slot, ItemStack item) {
        PacketPlayOutEntityEquipment equipmentPacket = getEquipmentPacket(entity, slot.getSlot(), item);
        PacketHelper.sendPacket(player, equipmentPacket);
    }

    public static void resetEquipment(Player player, LivingEntity entity) {
        org.bukkit.inventory.EntityEquipment equipment = entity.getEquipment();
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, EnumItemSlot.MAINHAND, equipment.getItemInMainHand()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, EnumItemSlot.OFFHAND, equipment.getItemInOffHand()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, EnumItemSlot.FEET, equipment.getBoots()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, EnumItemSlot.LEGS, equipment.getLeggings()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, EnumItemSlot.CHEST, equipment.getChestplate()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, EnumItemSlot.HEAD, equipment.getHelmet()));
    }
}

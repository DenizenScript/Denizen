package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Map;

public class EntityEquipment {

    public enum EquipmentSlots {
        HAND(0), BOOTS(1), LEGS(2), CHEST(3), HEAD(4);
        private int slot;
        EquipmentSlots(int slot) { this.slot = slot; }
        public int getSlot() { return slot; }
    }

    private static final Field equipment_entityId, equipment_slot, equipment_itemstack;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutEntityEquipment.class);
        equipment_entityId = fields.get("a");
        equipment_slot = fields.get("b");
        equipment_itemstack = fields.get("c");
    }

    public static PacketPlayOutEntityEquipment getEquipmentPacket(LivingEntity entity, int slot, ItemStack item) {
        PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment();
        try {
            equipment_entityId.set(equipmentPacket, entity.getEntityId());
            equipment_slot.set(equipmentPacket, slot);
            equipment_itemstack.set(equipmentPacket, CraftItemStack.asNMSCopy(item));
        } catch (Exception e) {
            dB.echoError(e);
        }
        return equipmentPacket;
    }

    public static void showEquipment(Player player, LivingEntity entity, EquipmentSlots slot, ItemStack item) {
        int slotNumber = entity.equals(player) ? slot.getSlot()-1 : slot.getSlot();
        if (slotNumber == -1) {
            dB.echoError("Cannot force a player to see themselves holding a different item.");
            return;
        }
        PacketPlayOutEntityEquipment equipmentPacket = getEquipmentPacket(entity, slotNumber, item);
        PacketHelper.sendPacket(player, equipmentPacket);
    }

    public static void resetEquipment(Player player, LivingEntity entity) {
        org.bukkit.inventory.EntityEquipment equipment = entity.getEquipment();
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, 0, equipment.getItemInHand()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, 1, equipment.getBoots()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, 2, equipment.getLeggings()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, 3, equipment.getChestplate()));
        PacketHelper.sendPacket(player, getEquipmentPacket(entity, 4, equipment.getHelmet()));
    }
}

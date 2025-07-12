package com.denizenscript.denizen.utilities.nbt;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.properties.entity.EntityDisabledSlots.Action;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.kyori.adventure.nbt.*;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomNBT {

    public static final String KEY_DENIZEN = "Denizen NBT";
    public static final String KEY_CAN_PLACE_ON = "CanPlaceOn";
    public static final String KEY_CAN_DESTROY = "CanDestroy";
    public static final String KEY_DISABLED_SLOTS = "DisabledSlots";

    private static final Map<EquipmentSlot, Integer> slotMap;

    static {
        slotMap = new HashMap<>();
        slotMap.put(EquipmentSlot.HAND, 0);
        slotMap.put(EquipmentSlot.FEET, 1);
        slotMap.put(EquipmentSlot.LEGS, 2);
        slotMap.put(EquipmentSlot.CHEST, 3);
        slotMap.put(EquipmentSlot.HEAD, 4);
        slotMap.put(EquipmentSlot.OFF_HAND, 5);
    }

    /*
     * Some static methods for dealing with Minecraft NBT data, which is used to store
     * custom NBT.
     */

    // TODO: once 1.20 is the minimum supported version, remove this
    public static List<Material> getNBTMaterials(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundBinaryTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        List<Material> materials = new ArrayList<>();
        if (compoundTag.keySet().contains(key)) {
            ListBinaryTag temp = compoundTag.getList(key, BinaryTagTypes.STRING);
            for (BinaryTag tag : temp) {
                materials.add(Material.matchMaterial(((StringBinaryTag) tag).value()));
            }
        }
        return materials;
    }

    // TODO: once 1.20 is the minimum supported version, remove this
    public static ItemStack setNBTMaterials(ItemStack itemStack, String key, List<Material> materials) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundBinaryTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        CompoundBinaryTag modifiedCompoundTag = compoundTag.remove(key);
        if (materials.isEmpty()) {
            return NMSHandler.itemHelper.setNbtData(itemStack, modifiedCompoundTag);
        }
        ListBinaryTag.Builder<StringBinaryTag> internalMaterials = ListBinaryTag.builder(BinaryTagTypes.STRING);
        for (Material material : materials) {
            internalMaterials.add(StringBinaryTag.stringBinaryTag(material.getKey().toString()));
        }
        modifiedCompoundTag = modifiedCompoundTag.put(key, internalMaterials.build());
        return NMSHandler.itemHelper.setNbtData(itemStack, modifiedCompoundTag);
    }

    public static ItemStack addCustomNBT(ItemStack itemStack, String key, String value, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundBinaryTag customData = ItemRawNBT.compoundOrEmpty(NMSHandler.itemHelper.getCustomData(itemStack));
        CompoundBinaryTag modifiedDenizenData = customData.getCompound(basekey).putString(CoreUtilities.toLowerCase(key), value);
        CompoundBinaryTag modifiedCustomData = customData.put(basekey, modifiedDenizenData);
        return NMSHandler.itemHelper.setCustomData(itemStack, modifiedCustomData);
    }

    // TODO: once 1.20 is the minimum supported version, remove this
    public static ItemStack clearNBT(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundBinaryTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        return NMSHandler.itemHelper.setNbtData(itemStack, compoundTag.remove(key));
    }

    public static ItemStack removeCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundBinaryTag customData = NMSHandler.itemHelper.getCustomData(itemStack);
        if (customData == null) {
            return itemStack;
        }
        CompoundBinaryTag denizenData = customData.getCompound(basekey, null);
        if (denizenData == null) {
            return itemStack;
        }
        CompoundBinaryTag modifiedDenizenData = denizenData.remove(CoreUtilities.toLowerCase(key));
        CompoundBinaryTag modifiedCustomData = modifiedDenizenData.isEmpty() ? customData.remove(basekey) : customData.put(basekey, modifiedDenizenData);
        return NMSHandler.itemHelper.setCustomData(itemStack, modifiedCustomData.isEmpty() ? null : modifiedCustomData);
    }

    public static boolean hasCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        CompoundBinaryTag customData = NMSHandler.itemHelper.getCustomData(itemStack);
        if (customData == null) {
            return false;
        }
        return customData.getCompound(basekey).keySet().contains(CoreUtilities.toLowerCase(key));
    }

    public static String getCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR || key == null) {
            return null;
        }
        CompoundBinaryTag customData = NMSHandler.itemHelper.getCustomData(itemStack);
        if (customData == null) {
            return null;
        }
        CompoundBinaryTag denizenData = customData.getCompound(basekey, null);
        if (denizenData == null) {
            return null;
        }
        return denizenData.getString(CoreUtilities.toLowerCase(key), null);
    }

    public static List<String> listNBT(ItemStack itemStack, String basekey) {
        List<String> nbt = new ArrayList<>();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return nbt;
        }
        CompoundBinaryTag customData = NMSHandler.itemHelper.getCustomData(itemStack);
        if (customData == null) {
            return nbt;
        }
        CompoundBinaryTag denizenData = customData.getCompound(basekey, null);
        if (denizenData == null) {
            return nbt;
        }
        nbt.addAll(denizenData.keySet());
        return nbt;
    }

    public static void addCustomNBT(Entity entity, String key, int value) {
        if (entity == null) {
            return;
        }
        CompoundBinaryTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        NMSHandler.entityHelper.setNbtData(entity, compoundTag.putInt(key, value));
    }

    public static void removeCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return;
        }
        CompoundBinaryTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        NMSHandler.entityHelper.setNbtData(entity, compoundTag.remove(key));
    }

    public static int getCustomIntNBT(Entity entity, String key) {
        if (entity == null) {
            return 0;
        }
        CompoundBinaryTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        // Return contents of the tag
        return compoundTag.getInt(key);
    }

    public static void setDisabledSlots(Entity entity, Map<EquipmentSlot, Set<Action>> map) {
        int sum = 0;
        for (Map.Entry<EquipmentSlot, Set<Action>> entry : map.entrySet()) {
            if (!slotMap.containsKey(entry.getKey())) {
                continue;
            }
            for (Action action : entry.getValue()) {
                sum += 1 << (slotMap.get(entry.getKey()) + action.getId());
            }
        }
        addCustomNBT(entity, KEY_DISABLED_SLOTS, sum);
    }

    public static Map<EquipmentSlot, Set<Action>> getDisabledSlots(Entity entity) {
        if (entity == null) {
            return null;
        }
        Map<EquipmentSlot, Set<Action>> map = new HashMap<>();
        CompoundBinaryTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        int disabledSlots = compoundTag.getInt(KEY_DISABLED_SLOTS);
        if (disabledSlots == 0) {
            return map;
        }
        slotLoop:
        for (EquipmentSlot slot : slotMap.keySet()) {
            for (Action action : Action.values()) {
                int matchedSlot = disabledSlots & 1 << slotMap.get(slot) + action.getId();
                if (matchedSlot != 0) {
                    Set<Action> set = map.computeIfAbsent(slot, k -> new HashSet<>());
                    set.add(action);
                    disabledSlots -= matchedSlot;
                    if (disabledSlots == 0) {
                        break slotLoop;
                    }
                }
            }
        }
        return map;
    }
}

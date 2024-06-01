package com.denizenscript.denizen.utilities.nbt;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.JNBTListTag;
import com.denizenscript.denizen.nms.util.jnbt.StringTag;
import com.denizenscript.denizen.objects.properties.entity.EntityDisabledSlots.Action;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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

    public static List<Material> getNBTMaterials(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        List<Material> materials = new ArrayList<>();
        if (compoundTag.getValue().containsKey(key)) {
            List<StringTag> temp = (List<StringTag>) compoundTag.getValue().get(key).getValue();
            for (StringTag tag : temp) {
                materials.add(Material.matchMaterial(tag.getValue()));
            }
        }
        return materials;
    }

    public static ItemStack setNBTMaterials(ItemStack itemStack, String key, List<Material> materials) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        compoundTag = compoundTag.createBuilder().remove(key).build();
        if (materials.isEmpty()) {
            return NMSHandler.itemHelper.setNbtData(itemStack, compoundTag);
        }
        List<StringTag> internalMaterials = new ArrayList<>();
        for (Material material : materials) {
            internalMaterials.add(new StringTag(material.getKey().toString()));
        }
        JNBTListTag lt = new JNBTListTag(StringTag.class, internalMaterials);
        compoundTag = compoundTag.createBuilder().put(key, lt).build();
        return NMSHandler.itemHelper.setNbtData(itemStack, compoundTag);
    }

    public static ItemStack addCustomNBT(ItemStack itemStack, String key, String value, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey(basekey)) {
            denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
        }
        else {
            denizenTag = NMSHandler.instance.createCompoundTag(new HashMap<>());
        }
        // Add custom NBT
        denizenTag = denizenTag.createBuilder().putString(CoreUtilities.toLowerCase(key), value).build();
        compoundTag = compoundTag.createBuilder().put(basekey, denizenTag).build();
        // Write tag back
        return NMSHandler.itemHelper.setNbtData(itemStack, compoundTag);
    }

    public static ItemStack clearNBT(ItemStack itemStack, String key) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        compoundTag = compoundTag.createBuilder().remove(key).build();
        // Write tag back
        return NMSHandler.itemHelper.setNbtData(itemStack, compoundTag);
    }

    public static ItemStack removeCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey(basekey)) {
            denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
        }
        else {
            return itemStack;
        }
        // Remove custom NBT
        denizenTag = denizenTag.createBuilder().remove(CoreUtilities.toLowerCase(key)).build();
        if (denizenTag.getValue().isEmpty()) {
            compoundTag = compoundTag.createBuilder().remove(basekey).build();
        }
        else {
            compoundTag = compoundTag.createBuilder().put(basekey, denizenTag).build();
        }
        // Write tag back
        return NMSHandler.itemHelper.setNbtData(itemStack, compoundTag);
    }

    public static boolean hasCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey(basekey)) {
            denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
        }
        else {
            return false;
        }
        return denizenTag.getValue().containsKey(CoreUtilities.toLowerCase(key));
    }

    public static String getCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null || itemStack.getType() == Material.AIR || key == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        if (compoundTag.getValue().containsKey(basekey)) {
            CompoundTag denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
            String lowerKey = CoreUtilities.toLowerCase(key);
            if (denizenTag.containsKey(lowerKey)) {
                return denizenTag.getString(lowerKey);
            }
        }
        return null;
    }

    public static List<String> listNBT(ItemStack itemStack, String basekey) {
        List<String> nbt = new ArrayList<>();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return nbt;
        }
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(itemStack);
        if (compoundTag.getValue().containsKey(basekey)) {
            CompoundTag denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
            nbt.addAll(denizenTag.getValue().keySet());
        }
        return nbt;
    }

    public static void addCustomNBT(Entity entity, String key, String value) {
        if (entity == null) {
            return;
        }
        CompoundTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        // Add custom NBT
        compoundTag = compoundTag.createBuilder().putString(key, value).build();
        // Write tag back
        NMSHandler.entityHelper.setNbtData(entity, compoundTag);
    }

    public static void addCustomNBT(Entity entity, String key, int value) {
        if (entity == null) {
            return;
        }
        CompoundTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        // Add custom NBT
        compoundTag = compoundTag.createBuilder().putInt(key, value).build();
        // Write tag back
        NMSHandler.entityHelper.setNbtData(entity, compoundTag);
    }

    public static void removeCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return;
        }
        CompoundTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        // Remove custom NBT
        compoundTag = compoundTag.createBuilder().remove(key).build();
        // Write tag back
        NMSHandler.entityHelper.setNbtData(entity, compoundTag);
    }

    public static boolean hasCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return false;
        }
        CompoundTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        // Check for key
        return compoundTag.getValue().containsKey(key);
    }

    public static String getCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
        // Return contents of the tag
        return compoundTag.getString(key);
    }

    public static int getCustomIntNBT(Entity entity, String key) {
        if (entity == null) {
            return 0;
        }
        CompoundTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
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
        CompoundTag compoundTag = NMSHandler.entityHelper.getNbtData(entity);
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

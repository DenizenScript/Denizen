package com.denizenscript.denizen.utilities.inventory;

import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Helper for player inventory slots.
 *
 * These values are based on indices within the inventory contents array of a player.
 *
 * These values are *NOT* equivalent to arbitrary Minecraft "slot ID" values, which are commonly mentioned online but have no connection to anything.
 *
 * All values are Minecraft indices (0-based).
 */
public class SlotHelper {

    // Player hotbar: 0-8 (9 items counting 0)
    // Player inventory 9-35 (3 rows of 9 items)

    public static final int BOOTS = 36; // MC ID: 100

    public static final int LEGGINGS = 37; // MC ID: 101

    public static final int CHESTPLATE = 38; // MC ID: 102

    public static final int HELMET = 39; // MC ID: 103

    public static final int OFFHAND = 40; // MC ID: -106 // wtf mojang? Negative?

    /**
     * Returns the slot an item in the player's inventory is at (for events like PlayerItemDamageEvent where the exact item is available).
     * Returns -1 if unknown.
     */
    public static int slotForItem(PlayerInventory inventory, ItemStack item) {
        if (item.equals(inventory.getChestplate())) {
            return CHESTPLATE;
        }
        if (item.equals(inventory.getLeggings())) {
            return LEGGINGS;
        }
        if (item.equals(inventory.getBoots())) {
            return BOOTS;
        }
        if (item.equals(inventory.getHelmet())) {
            return HELMET;
        }
        if (item.equals(inventory.getItemInMainHand())) {
            return inventory.getHeldItemSlot();
        }
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (item.equals(contents[i])) {
                return i;
            }
        }
        return -1;
    }

    // <--[language]
    // @name Slot Inputs
    // @group Useful Lists
    // @description
    // Whenever a script component requires a slot ID (such as the take command, when using '- take slot:#')
    // you can give the slot ID input as either a number of the 1-based index (where the first slot is 1, the second is 2, etc.)
    // If the slot given is 'hand', for a player the held item slot will be used, for any other entity the slot will be 1.
    // OR you can give the following names (valid for player inventories only):
    // BOOTS: equivalent to 37
    // LEGGINGS: equivalent to 38
    // CHESTPLATE: equivalent to 39
    // HELMET: equivalent to 40
    // OFFHAND: equivalent to 41
    //
    // Note that some common alternate spellings may be automatically accepted as well.
    // -->
    public static EquipmentSlot indexToEquipSlot(int index) {
        switch (index) {
            case BOOTS:
                return EquipmentSlot.FEET;
            case LEGGINGS:
                return EquipmentSlot.LEGS;
            case CHESTPLATE:
                return EquipmentSlot.CHEST;
            case HELMET:
                return EquipmentSlot.HEAD;
            case OFFHAND:
                return EquipmentSlot.OFF_HAND;
            default:
                return null;
        }
    }

    public static int equipSlotToIndex(EquipmentSlot slot) {
        switch (slot) {
            case FEET:
                return BOOTS;
            case LEGS:
                return LEGGINGS;
            case CHEST:
                return CHESTPLATE;
            case HEAD:
                return HELMET;
            case OFF_HAND:
                return OFFHAND;
            default:
                return -1;
        }
    }

    public static final HashMap<String, Integer> nameIndexMap = new HashMap<>();
    public static final List<String>[] indexNameMap = new List[50];

    public static void registerSlotName(String name, int index) {
        nameIndexMap.put(name, index);
        nameIndexMap.put(name + "s", index);
        List<String> list = indexNameMap[index];
        if (list == null) {
            list = new ArrayList<>();
            indexNameMap[index] = list;
        }
        list.add(name);
        list.add(name + "s");
    }

    static {
        registerSlotName("boot", BOOTS);
        registerSlotName("feet", BOOTS);
        registerSlotName("foot", BOOTS);
        registerSlotName("shoe", BOOTS);
        registerSlotName("leg", LEGGINGS);
        registerSlotName("legging", LEGGINGS);
        registerSlotName("chest", CHESTPLATE);
        registerSlotName("chestplate", CHESTPLATE);
        registerSlotName("helmet", HELMET);
        registerSlotName("head", HELMET);
        registerSlotName("offhand", OFFHAND);
    }

    public static int nameToIndexFor(String name, InventoryHolder holder) {
        return nameToIndex(name, holder instanceof Entity ? (Entity) holder : null);
    }

    /**
     * Converts a user given slot name (or number) to a valid internal slot index.
     * Will subtract 1 from a user-given number, per Denizen standard for user input (1-based slot index).
     */
    public static int nameToIndex(String name, Entity entity) {
        name = name.toLowerCase().replace("_", "");
        if (name.equals("hand")) {
            return entity instanceof Player ? ((Player) entity).getInventory().getHeldItemSlot() : 0;
        }
        Integer matched = nameIndexMap.get(name);
        if (matched != null) {
            return matched;
        }
        if (ArgumentHelper.matchesInteger(name)) {
            return Integer.parseInt(name) - 1;
        }
        return -1;
    }

    public static boolean doesMatch(String text, Entity entity, int slot) {
        if (slot >= 0 && slot < indexNameMap.length) {
            List<String> names = indexNameMap[slot];
            if (names != null) {
                for (String name : names) {
                    if (ScriptEvent.runGenericCheck(text, name)) {
                        return true;
                    }
                }
            }
        }
        if (entity instanceof Player && slot == ((Player) entity).getInventory().getHeldItemSlot()) {
            if (ScriptEvent.runGenericCheck(text, "hand")) {
                return true;
            }
        }
        if (ScriptEvent.runGenericCheck(text, String.valueOf(slot + 1))) {
            return true;
        }
        return false;
    }
}

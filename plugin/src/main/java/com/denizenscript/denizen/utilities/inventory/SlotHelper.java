package com.denizenscript.denizen.utilities.inventory;

import com.denizenscript.denizen.utilities.blocks.MaterialCompat;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

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
        Material type = item.getType();
        if (MaterialCompat.HELMET_MATERIALS.contains(type) && item.equals(inventory.getHelmet())) {
            return HELMET;
        }
        if (MaterialCompat.CHESTPLATE_MATERIALS.contains(type) && item.equals(inventory.getChestplate())) {
            return CHESTPLATE;
        }
        if (MaterialCompat.LEGGINGS_MATERIALS.contains(type) && item.equals(inventory.getLeggings())) {
            return LEGGINGS;
        }
        if (MaterialCompat.BOOTS_MATERIALS.contains(type) && item.equals(inventory.getBoots())) {
            return BOOTS;
        }
        if (item.equals(inventory.getItemInMainHand())) {
            return inventory.getHeldItemSlot() + 1;
        }
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (item.equals(contents[i])) {
                return i + 1;
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

    static {
        nameIndexMap.put("boot", BOOTS);
        nameIndexMap.put("feet", BOOTS);
        nameIndexMap.put("foot", BOOTS);
        nameIndexMap.put("shoe", BOOTS);
        nameIndexMap.put("leg", LEGGINGS);
        nameIndexMap.put("legging", LEGGINGS);
        nameIndexMap.put("chest", CHESTPLATE);
        nameIndexMap.put("chestplate", CHESTPLATE);
        nameIndexMap.put("helmet", HELMET);
        nameIndexMap.put("head", HELMET);
        nameIndexMap.put("offhand", OFFHAND);
    }

    /**
     * Converts a user given slot name (or number) to a valid internal slot index.
     * Will subtract 1 from a user-given number, per Denizen standard for user input (1-based slot index).
     */
    public static int nameToIndex(String name) {
        name = name.toLowerCase().replace("_", "");
        if (name.endsWith("s")) {
            name = name.substring(0, name.length() - 1);
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
}

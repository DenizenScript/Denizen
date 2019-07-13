package com.denizenscript.denizen.utilities.nbt;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map.Entry;

public class MapOfEnchantments extends HashMap<Enchantment, Integer> {

    private static final long serialVersionUID = -4078835287654565987L;

    public MapOfEnchantments(ItemStack item) {
        super();
        this.putAll(item.getEnchantments());
    }

    public String asDScriptList() {
        String dScriptList = "";
        if (this.isEmpty()) {
            return dScriptList;
        }
        for (Enchantment enchantment : this.keySet()) {
            dScriptList = dScriptList + enchantment.getName() + "|";
        }
        return dScriptList.substring(0, dScriptList.length() - 1);
    }

    public String asDScriptListWithLevels() {
        String dScriptList = "";
        if (this.isEmpty()) {
            return dScriptList;
        }
        for (Entry<Enchantment, Integer> enchantment : this.entrySet()) {
            dScriptList = dScriptList + enchantment.getKey().getName() + "|" + enchantment.getValue() + "|";
        }
        return dScriptList.substring(0, dScriptList.length() - 1);
    }

    public String asDScriptListLevelsOnly() {
        String dScriptList = "";
        if (this.isEmpty()) {
            return dScriptList;
        }
        for (Integer enchantment : this.values()) {
            dScriptList = dScriptList + enchantment + "|";
        }
        return dScriptList.substring(0, dScriptList.length() - 1);

    }
}

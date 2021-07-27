package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class EnchantmentHelper {

    public Enchantment registerFakeEnchantment(EnchantmentScriptContainer.EnchantmentReference script) {
        throw new UnsupportedOperationException();
    }

    public String getRarity(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public boolean isDiscoverable(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public boolean isTradable(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public boolean isCurse(Enchantment enchantment) {
        throw new UnsupportedOperationException();
    }

    public int getMinCost(Enchantment enchantment, int level) {
        throw new UnsupportedOperationException();
    }

    public int getMaxCost(Enchantment enchantment, int level) {
        throw new UnsupportedOperationException();
    }

    public String getFullName(Enchantment enchantment, int level) {
        throw new UnsupportedOperationException();
    }

    public float getDamageBonus(Enchantment enchantment, int level, String type) {
        throw new UnsupportedOperationException();
    }

    public int getDamageProtection(Enchantment enchantment, int level, EntityDamageEvent.DamageCause type, Entity attacker) {
        throw new UnsupportedOperationException();
    }
}

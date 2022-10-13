package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizen.nms.interfaces.EnchantmentHelper;
import com.denizenscript.denizen.nms.v1_17.Handler;
import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_17_R1.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

public class EnchantmentHelperImpl extends EnchantmentHelper {

    public static Map<NamespacedKey, Enchantment> ENCHANTMENTS_BY_KEY = ReflectionHelper.getFieldValue(org.bukkit.enchantments.Enchantment.class, "byKey", null);
    public static Map<String, org.bukkit.enchantments.Enchantment> ENCHANTMENTS_BY_NAME = ReflectionHelper.getFieldValue(org.bukkit.enchantments.Enchantment.class, "byName", null);

    @Override
    public org.bukkit.enchantments.Enchantment registerFakeEnchantment(EnchantmentScriptContainer.EnchantmentReference script) {
        try {
            EquipmentSlot[] slots = new EquipmentSlot[script.script.slots.size()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = EquipmentSlot.valueOf(script.script.slots.get(i).toUpperCase());
            }
            net.minecraft.world.item.enchantment.Enchantment nmsEnchant = new net.minecraft.world.item.enchantment.Enchantment(net.minecraft.world.item.enchantment.Enchantment.Rarity.valueOf(script.script.rarity), EnchantmentCategory.valueOf(script.script.category), slots) {
                @Override
                public int getMinLevel() {
                    return script.script.minLevel;
                }
                @Override
                public int getMaxLevel() {
                    return script.script.maxLevel;
                }
                @Override
                public int getMinCost(int level) {
                    return script.script.getMinCost(level);
                }
                @Override
                public int getMaxCost(int level) {
                    return script.script.getMaxCost(level);
                }
                @Override
                public int getDamageProtection(int level, DamageSource src) {
                    return script.script.getDamageProtection(level, src.msgId, src.getEntity() == null ? null : src.getEntity().getBukkitEntity());
                }
                @Override
                public float getDamageBonus(int level, MobType type) {
                    String typeName = "UNDEFINED";
                    if (type == MobType.ARTHROPOD) {
                        typeName = "ARTHROPOD";
                    }
                    else if (type == MobType.ILLAGER) {
                        typeName = "ILLAGER";
                    }
                    else if (type == MobType.UNDEAD) {
                        typeName = "UNDEAD";
                    }
                    else if (type == MobType.WATER) {
                        typeName = "WATER";
                    }
                    return script.script.getDamageBonus(level, typeName);
                }
                @Override
                protected boolean checkCompatibility(net.minecraft.world.item.enchantment.Enchantment nmsEnchantment) {
                    ResourceLocation nmsKey = Registry.ENCHANTMENT.getKey(nmsEnchantment);
                    NamespacedKey bukkitKey = CraftNamespacedKey.fromMinecraft(nmsKey);
                    org.bukkit.enchantments.Enchantment bukkitEnchant = CraftEnchantment.getByKey(bukkitKey);
                    return script.script.isCompatible(bukkitEnchant);
                }
                @Override
                protected String getOrCreateDescriptionId() {
                    return script.script.descriptionId;
                }
                @Override
                public String getDescriptionId() {
                    return script.script.descriptionId;
                }
                @Override
                public Component getFullname(int level) {
                    return Handler.componentToNMS(script.script.getFullName(level));
                }
                @Override
                public boolean canEnchant(net.minecraft.world.item.ItemStack var0) {
                    return super.canEnchant(var0) && script.script.canEnchant(CraftItemStack.asBukkitCopy(var0));
                }
                @Override
                public void doPostAttack(LivingEntity attacker, Entity victim, int level) {
                    script.script.doPostAttack(attacker.getBukkitEntity(), victim.getBukkitEntity(), level);
                }
                @Override
                public void doPostHurt(LivingEntity victim, Entity attacker, int level) {
                    script.script.doPostHurt(victim.getBukkitEntity(), attacker.getBukkitEntity(), level);
                }
                @Override
                public boolean isTreasureOnly() {
                    return script.script.isTreasureOnly;
                }
                @Override
                public boolean isCurse() {
                    return script.script.isCurse;
                }
                @Override
                public boolean isTradeable() {
                    return script.script.isTradable;
                }
                @Override
                public boolean isDiscoverable() {
                    return script.script.isDiscoverable;
                }
            };
            String enchName = script.script.id.toUpperCase();
            Registry.register(Registry.ENCHANTMENT, "denizen:" + script.script.id, nmsEnchant);
            CraftEnchantment ench = new CraftEnchantment(nmsEnchant) {
                @Override
                public String getName() {
                    return enchName;
                }
            };
            ENCHANTMENTS_BY_KEY.put(ench.getKey(), ench);
            ENCHANTMENTS_BY_NAME.put(enchName, ench);
            return ench;
        }
        catch (Throwable ex) {
            Debug.echoError("Failed to register enchantment " + script.script.id);
            Debug.echoError(ex);
            return null;
        }
    }

    @Override
    public String getRarity(Enchantment enchantment) {
        return ((CraftEnchantment) enchantment).getHandle().getRarity().name();
    }

    @Override
    public boolean isDiscoverable(Enchantment enchantment) {
        return ((CraftEnchantment) enchantment).getHandle().isDiscoverable();
    }

    @Override
    public boolean isTradable(Enchantment enchantment) {
        return ((CraftEnchantment) enchantment).getHandle().isTradeable();
    }

    @Override
    public boolean isCurse(Enchantment enchantment) {
        return ((CraftEnchantment) enchantment).getHandle().isCurse();
    }

    @Override
    public int getMinCost(Enchantment enchantment, int level) {
        return ((CraftEnchantment) enchantment).getHandle().getMinCost(level);
    }

    @Override
    public int getMaxCost(Enchantment enchantment, int level) {
        return ((CraftEnchantment) enchantment).getHandle().getMaxCost(level);
    }

    @Override
    public String getFullName(Enchantment enchantment, int level) {
        return FormattedTextHelper.stringify(Handler.componentToSpigot(((CraftEnchantment) enchantment).getHandle().getFullname(level)));
    }

    @Override
    public float getDamageBonus(Enchantment enchantment, int level, String type) {
        MobType mobType = MobType.UNDEFINED;
        switch (type) {
            case "illager":
                mobType = MobType.ILLAGER;
                break;
            case "undead":
                mobType = MobType.UNDEAD;
                break;
            case "water":
                mobType = MobType.WATER;
                break;
            case "arthropod":
                mobType = MobType.ARTHROPOD;
                break;
        }
        return ((CraftEnchantment) enchantment).getHandle().getDamageBonus(level, mobType);
    }

    @Override
    public int getDamageProtection(Enchantment enchantment, int level, EntityDamageEvent.DamageCause type, org.bukkit.entity.Entity attacker) {
        DamageSource src = EntityHelperImpl.getSourceFor(((CraftEntity) attacker).getHandle(), type);
        if (src instanceof EntityHelperImpl.FakeDamageSrc) {
            src = ((EntityHelperImpl.FakeDamageSrc) src).real;
        }
        return ((CraftEnchantment) enchantment).getHandle().getDamageProtection(level, src);
    }
}

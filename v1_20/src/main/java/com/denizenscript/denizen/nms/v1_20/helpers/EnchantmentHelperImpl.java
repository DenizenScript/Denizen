package com.denizenscript.denizen.nms.v1_20.helpers;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.interfaces.EnchantmentHelper;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R4.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

public class EnchantmentHelperImpl extends EnchantmentHelper {
    public static final Field REGISTRY_FROZEN = ReflectionHelper.getFields(MappedRegistry.class).get(ReflectionMappingsInfo.MappedRegistry_frozen, boolean.class);
    public static final Field REGISTRY_INTRUSIVE_HOLDERS = ReflectionHelper.getFields(MappedRegistry.class).get(ReflectionMappingsInfo.MappedRegistry_unregisteredIntrusiveHolders, Map.class);

    @Override
    public org.bukkit.enchantments.Enchantment registerFakeEnchantment(EnchantmentScriptContainer.EnchantmentReference script) {
        try {
            Map holders = (Map) REGISTRY_INTRUSIVE_HOLDERS.get(BuiltInRegistries.ENCHANTMENT);
            if (holders == null) {
                REGISTRY_INTRUSIVE_HOLDERS.set(BuiltInRegistries.ENCHANTMENT, new IdentityHashMap());
            }
            boolean wasFrozen = REGISTRY_FROZEN.getBoolean(BuiltInRegistries.ENCHANTMENT);
            REGISTRY_FROZEN.setBoolean(BuiltInRegistries.ENCHANTMENT, false);
            EquipmentSlot[] slots = new EquipmentSlot[script.script.slots.size()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = EquipmentSlot.valueOf(CoreUtilities.toUpperCase(script.script.slots.get(i)));
            }
            // TODO: 1.20.6: rarity is provided as an int, can make our own mirror enum; categories seemed to only over control #canEnchant(ItemStack), so can probably safely phase them out?
            // net.minecraft.world.item.enchantment.Enchantment.Rarity.valueOf(script.script.rarity), EnchantmentCategory.valueOf(script.script.category), slots
            net.minecraft.world.item.enchantment.Enchantment nmsEnchant = new net.minecraft.world.item.enchantment.Enchantment(null) {
                // TODO: 1.20.6: methods are final now and the values are provided by EnchantmentDefinition - would probably need to create a new one on reload and modify the existing enchantment
//                @Override
//                public int getMinLevel() {
//                    return script.script.minLevel;
//                }
//                @Override
//                public int getMaxLevel() {
//                    return script.script.maxLevel;
//                }
//                @Override
//                public int getMinCost(int level) {
//                    return script.script.getMinCost(level);
//                }
//                @Override
//                public int getMaxCost(int level) {
//                    return script.script.getMaxCost(level);
//                }
                @Override
                public int getDamageProtection(int level, DamageSource src) {
                    return script.script.getDamageProtection(level, src.getMsgId(), src.getEntity() == null ? null : src.getEntity().getBukkitEntity());
                }
                // TODO: 1.20.6: Takes an EntityType now, and MobType seems to have been removed in favor of vanilla tags - can probably use these to backsupport & properly pass the entity type
//                @Override
//                public float getDamageBonus(int level, EntityType type) {
//                    String typeName = "UNDEFINED";
//                    if (type == MobType.ARTHROPOD) {
//                        typeName = "ARTHROPOD";
//                    }
//                    else if (type == MobType.ILLAGER) {
//                        typeName = "ILLAGER";
//                    }
//                    else if (type == MobType.UNDEAD) {
//                        typeName = "UNDEAD";
//                    }
//                    else if (type == MobType.WATER) {
//                        typeName = "WATER";
//                    }
//                    return script.script.getDamageBonus(level, typeName);
//                }
                @Override
                protected boolean checkCompatibility(net.minecraft.world.item.enchantment.Enchantment nmsEnchantment) {
                    ResourceLocation nmsKey = BuiltInRegistries.ENCHANTMENT.getKey(nmsEnchantment);
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
            NamespacedKey enchantmentKey = new NamespacedKey(Denizen.getInstance(), script.script.id);
            Registry.register(BuiltInRegistries.ENCHANTMENT, enchantmentKey.toString(), nmsEnchant);
            String enchName = CoreUtilities.toUpperCase(script.script.id);
            CraftEnchantment ench = new CraftEnchantment(enchantmentKey, nmsEnchant) {
                @Override
                public String getName() {
                    return enchName;
                }
            };
            REGISTRY_INTRUSIVE_HOLDERS.set(BuiltInRegistries.ENCHANTMENT, holders);
            if (wasFrozen) {
                BuiltInRegistries.ENCHANTMENT.freeze();
            }
            return ench;
        }
        catch (Throwable ex) {
            Debug.echoError("Failed to register enchantment " + script.script.id);
            Debug.echoError(ex);
            return null;
        }
    }

    // TODO: 1.20.6: rarity is just an int now (weight), can deprecate & backsupport by estimating it based on the weight
//    @Override
//    public String getRarity(Enchantment enchantment) {
//        return ((CraftEnchantment) enchantment).getHandle().getRarity().name();
//    }

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

    // TODO: 1.20.6: MobType was removed in favor of using the entity type directly - deprecate + potentially backsupport with vanilla tags
//    @Override
//    public float getDamageBonus(Enchantment enchantment, int level, String type) {
//        MobType mobType = switch (type) {
//            case "illager" -> MobType.ILLAGER;
//            case "undead" -> MobType.UNDEAD;
//            case "water" -> MobType.WATER;
//            case "arthropod" -> MobType.ARTHROPOD;
//            default -> MobType.UNDEFINED;
//        };
//        return ((CraftEnchantment) enchantment).getHandle().getDamageBonus(level, mobType);
//    }

    @Override
    public int getDamageProtection(Enchantment enchantment, int level, EntityDamageEvent.DamageCause type, org.bukkit.entity.Entity attacker) {
        Entity nmsAttacker = attacker == null ? null : ((CraftEntity) attacker).getHandle();
        DamageSource src = EntityHelperImpl.getSourceFor(nmsAttacker, type, nmsAttacker);
        if (src instanceof EntityHelperImpl.FakeDamageSrc fakeDamageSrc) {
            src = fakeDamageSrc.real;
        }
        return ((CraftEnchantment) enchantment).getHandle().getDamageProtection(level, src);
    }
}

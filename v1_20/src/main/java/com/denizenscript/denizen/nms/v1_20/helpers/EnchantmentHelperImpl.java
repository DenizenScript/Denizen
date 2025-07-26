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
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R4.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class EnchantmentHelperImpl extends EnchantmentHelper {
    public static final Field REGISTRY_FROZEN = ReflectionHelper.getFields(MappedRegistry.class).get(ReflectionMappingsInfo.MappedRegistry_frozen, boolean.class);
    public static final Field REGISTRY_INTRUSIVE_HOLDERS = ReflectionHelper.getFields(MappedRegistry.class).get(ReflectionMappingsInfo.MappedRegistry_unregisteredIntrusiveHolders, Map.class);
    public static final TagKey<Item> NON_EXISTENT_ITEM_TAG = new TagKey<>(Registries.ITEM, new ResourceLocation("denizen", "aeg9jw0059ghg00ig3wiij"));

    public enum MobType {
        ILLAGER(EntityTypeTags.ILLAGER),
        UNDEAD(EntityTypeTags.UNDEAD),
        WATER(EntityTypeTags.AQUATIC),
        ARTHROPOD(EntityTypeTags.ARTHROPOD),
        UNDEFINED(null);

        final TagKey<EntityType<?>> nmsTagKey;

        MobType(TagKey<EntityType<?>> nmsTagKey) {
            this.nmsTagKey = nmsTagKey;
        }

        public TagKey<EntityType<?>> getNmsTagKey() {
            return nmsTagKey;
        }

        public static final MobType[] WITH_TAGS = {ILLAGER, UNDEAD, WATER, ARTHROPOD};

        public static MobType fromEntityType(EntityType<?> nmsEntityType) {
            for (MobType mobType : WITH_TAGS) {
                if (nmsEntityType.is(mobType.getNmsTagKey())) {
                    return mobType;
                }
            }
            return UNDEFINED;
        }
    }

    public enum EnchantmentCategory {
        ARMOR(ItemTags.ARMOR_ENCHANTABLE),
        ARMOR_FEET(ItemTags.FOOT_ARMOR_ENCHANTABLE),
        ARMOR_LEGS(ItemTags.LEG_ARMOR_ENCHANTABLE),
        ARMOR_CHEST(ItemTags.CHEST_ARMOR_ENCHANTABLE),
        ARMOR_HEAD(ItemTags.HEAD_ARMOR_ENCHANTABLE),
        WEAPON(ItemTags.WEAPON_ENCHANTABLE),
        DIGGER(ItemTags.MINING_ENCHANTABLE),
        FISHING_ROD(ItemTags.FISHING_ENCHANTABLE),
        TRIDENT(ItemTags.TRIDENT_ENCHANTABLE),
        BREAKABLE(ItemTags.DURABILITY_ENCHANTABLE),
        BOW(ItemTags.BOW_ENCHANTABLE),
        WEARABLE(ItemTags.EQUIPPABLE_ENCHANTABLE),
        CROSSBOW(ItemTags.CROSSBOW_ENCHANTABLE),
        VANISHABLE(ItemTags.VANISHING_ENCHANTABLE);

        final TagKey<Item> nmsTagKey;

        EnchantmentCategory(TagKey<Item> nmsTagKey) {
            this.nmsTagKey = nmsTagKey;
        }

        public TagKey<Item> getNmsTagKey() {
            return nmsTagKey;
        }
    }

    public static net.minecraft.world.item.enchantment.Enchantment.Cost tryParseCost(EnchantmentScriptContainer scriptContainer, String costTag, Int2IntFunction costMethod) {
        if (costTag.equals("<context.level>")) {
            return net.minecraft.world.item.enchantment.Enchantment.dynamicCost(1, 1);
        }
        if (costTag.startsWith("<context.level.mul[")) {
            String param = costTag.substring("<context.level.mul[".length(), costTag.lastIndexOf(']'));
            try {
                int multiplier = Integer.parseInt(param);
                return net.minecraft.world.item.enchantment.Enchantment.dynamicCost(multiplier, multiplier);
            }
            catch (NumberFormatException ignored) {}
        }
        int maxLevel = scriptContainer.maxLevel, minLevel = scriptContainer.minLevel;
        int multiplier = -1;
        for (int level = minLevel; level <= maxLevel; level++) {
            int cost = costMethod.get(level);
            int relation = cost / level;
            if (relation == multiplier || multiplier == -1) {
                multiplier = relation;
            }
            else {
                multiplier = -1;
                break;
            }
        }
        return multiplier != -1 ? net.minecraft.world.item.enchantment.Enchantment.dynamicCost(multiplier, multiplier) : net.minecraft.world.item.enchantment.Enchantment.constantCost((costMethod.get(minLevel) + costMethod.get(maxLevel)) / 2);
    }

    @Override
    public org.bukkit.enchantments.Enchantment registerFakeEnchantment(EnchantmentScriptContainer.EnchantmentReference script) {
        try {
            verifyEventsRegistered();
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
            Rarity rarity = Rarity.valueOf(script.script.rarity);
            net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition nmsEnchantDefinition = net.minecraft.world.item.enchantment.Enchantment.definition(
                    NON_EXISTENT_ITEM_TAG, EnchantmentCategory.valueOf(script.script.category).getNmsTagKey(), rarity.getWeight(), script.script.maxLevel,
                    tryParseCost(script.script, script.script.minCostTaggable, script.script::getMinCost),
                    tryParseCost(script.script, script.script.maxCostTaggable, script.script::getMaxCost),
                    rarity.getAnvilCost(), slots);
            net.minecraft.world.item.enchantment.Enchantment nmsEnchant = new net.minecraft.world.item.enchantment.Enchantment(nmsEnchantDefinition) {
                @Override
                public int getDamageProtection(int level, DamageSource src) {
                    return script.script.getDamageProtection(level, src.getMsgId(), src.getEntity() == null ? null : src.getEntity().getBukkitEntity());
                }
                @Override
                public float getDamageBonus(int level, EntityType type) {
                    return script.script.getDamageBonus(level, MobType.fromEntityType(type).name());
                }
                @Override
                protected boolean checkCompatibility(net.minecraft.world.item.enchantment.Enchantment nmsEnchantment) {
                    return script.script.isCompatible(CraftEnchantment.minecraftToBukkit(nmsEnchantment));
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
                    return script.script.canEnchant(CraftItemStack.asBukkitCopy(var0));
                }
                @Override
                public void doPostAttack(LivingEntity attacker, Entity victim, int level) {
                    script.script.doPostAttack(attacker.getBukkitEntity(), victim.getBukkitEntity(), level);
                }
                @Override
                public void doPostHurt(LivingEntity victim, Entity attacker, int level) {
                    script.script.doPostHurt(attacker.getBukkitEntity(), victim.getBukkitEntity(), level);
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

    @Override
    public String getRarity(Enchantment enchantment) {
        return Rarity.fromWeight(CraftEnchantment.bukkitToMinecraft(enchantment).getWeight()).name();
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
        EntityType<?> nmsEntityType = Optional.ofNullable(MobType.valueOf(CoreUtilities.toUpperCase(type)).getNmsTagKey())
                .flatMap(BuiltInRegistries.ENTITY_TYPE::getTag)
                .map(holders -> holders.get(0).value())
                .orElse((EntityType) EntityType.PIG);
        return ((CraftEnchantment) enchantment).getHandle().getDamageBonus(level, nmsEntityType);
    }

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

package com.denizenscript.denizen.nms.v1_21.helpers;

import com.denizenscript.denizen.nms.interfaces.EnchantmentHelper;
import com.denizenscript.denizen.nms.v1_21.Handler;
import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.bukkit.craftbukkit.v1_21_R5.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R5.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftNamespacedKey;
import org.bukkit.event.entity.EntityDamageEvent;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class EnchantmentHelperImpl extends EnchantmentHelper {

    public static class DynamicHolderSet<T> extends HolderSet.ListBacked<T> {

        final Registry<T> backing;
        final Predicate<T> filter;

        public DynamicHolderSet(Registry<T> backing, Predicate<T> filter) {
            this.backing = backing;
            this.filter = filter;
        }

        @Override
        protected List<Holder<T>> contents() {
            List<Holder<T>> contents = new ArrayList<>();
            backing.listElements().forEach(holder -> {
                if (filter.test(holder.value())) {
                    contents.add(holder);
                }
            });
            return contents;
        }

        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.right(contents());
        }

        @Override
        public boolean contains(Holder<T> holder) {
            T value = holder.value();
            return backing.getId(value) != -1 && filter.test(value);
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "DynamicHolderSet[backing=" + backing + ']';
        }
    }

    public static class DynamicContextAwareLevelValue implements LevelBasedValue, LootItemCondition {

        final BiFunction<Integer, LootContext, Float> calculator;
        LootContext currentContext;

        public DynamicContextAwareLevelValue(BiFunction<Integer, LootContext, Float> calculator) {
            this.calculator = calculator;
        }

        @Override
        public float calculate(int level) {
            if (currentContext == null) {
                throw new IllegalStateException("Missing current LootContext.");
            }
            float calculated = calculator.apply(level, currentContext);
            currentContext = null;
            return calculated;
        }

        @Override
        public MapCodec<? extends LevelBasedValue> codec() {
            throw new UnsupportedOperationException("Tried getting codec.");
        }

        @Override
        public LootItemConditionType getType() {
            throw new UnsupportedOperationException("Tried getting loot item condition type.");
        }

        @Override
        public boolean test(LootContext lootContext) {
            currentContext = lootContext;
            return true;
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

        public HolderSet<Item> getItems() {
            return BuiltInRegistries.ITEM.get(nmsTagKey).orElseThrow();
        }
    }

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

    public static final RegistrationInfo NO_NETWORKING_REGISTRATION_INFO = new RegistrationInfo(Optional.of(BuiltInPackSource.CORE_PACK_INFO), Lifecycle.stable());
    public static final Field REGISTRY_FROZEN = ReflectionHelper.getFields(MappedRegistry.class).get(ReflectionMappingsInfo.MappedRegistry_frozen, boolean.class);
    public static final Field REGISTRY_INTRUSIVE_HOLDERS = ReflectionHelper.getFields(MappedRegistry.class).get(ReflectionMappingsInfo.MappedRegistry_unregisteredIntrusiveHolders, Map.class);
    public static final Field REGISTRY_ALL_TAGS = ReflectionHelper.getFields(MappedRegistry.class).get("allTags");
    public static final MethodHandle MAPPED_REGISTRY_TAG_SET_UNBOUND = ReflectionHelper.getMethodHandle(REGISTRY_ALL_TAGS.getType(), "unbound");

    public static Enchantment.Cost tryParseCost(EnchantmentScriptContainer scriptContainer, String costTag, Int2IntFunction costMethod) {
        if (costTag.equals("<context.level>")) {
            return Enchantment.dynamicCost(1, 1);
        }
        if (costTag.startsWith("<context.level.mul[")) {
            String param = costTag.substring("<context.level.mul[".length(), costTag.lastIndexOf(']'));
            try {
                int multiplier = Integer.parseInt(param);
                return Enchantment.dynamicCost(multiplier, multiplier);
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
        return multiplier != -1 ? Enchantment.dynamicCost(multiplier, multiplier) : Enchantment.constantCost((costMethod.get(minLevel) + costMethod.get(maxLevel)) / 2);
    }

    @Override
    public org.bukkit.enchantments.Enchantment registerFakeEnchantment(EnchantmentScriptContainer.EnchantmentReference script) {
        try {
            verifyNetworkingInitialized();
            verifyEventsRegistered();
            MappedRegistry<Enchantment> nmsEnchantmentRegistry = (MappedRegistry<Enchantment>) CraftRegistry.getMinecraftRegistry(Registries.ENCHANTMENT);
            // Slot groups
            List<EquipmentSlotGroup> nmsSlotGroups = new ArrayList<>(script.script.slots.size());
            for (String slot : script.script.slots) {
                nmsSlotGroups.add(EquipmentSlotGroup.valueOf(CoreUtilities.toUpperCase(slot)));
            }
            // Rarity
            Rarity rarity = Rarity.valueOf(script.script.rarity);
            Enchantment.EnchantmentDefinition nmsEnchantmentDefinition = new Enchantment.EnchantmentDefinition(
                    new DynamicHolderSet<>(BuiltInRegistries.ITEM, nmsItem -> script.script.canEnchant(CraftItemStack.asNewCraftStack(nmsItem))),
                    Optional.of(EnchantmentCategory.valueOf(script.script.category).getItems()),
                    rarity.getWeight(), script.script.maxLevel,
                    tryParseCost(script.script, script.script.minCostTaggable, script.script::getMinCost),
                    tryParseCost(script.script, script.script.maxCostTaggable, script.script::getMaxCost),
                    rarity.getAnvilCost(), nmsSlotGroups);
            // Damage bonus
            DataComponentMap.Builder nmsEnchantmentEffectsBuilder = DataComponentMap.builder();
            DynamicContextAwareLevelValue damageModifier = new DynamicContextAwareLevelValue((level, lootContext) ->
                    script.script.getDamageBonus(level, MobType.fromEntityType(lootContext.getParameter(LootContextParams.THIS_ENTITY).getType()).name())
            );
            addEnchantmentEffect(nmsEnchantmentEffectsBuilder, EnchantmentEffectComponents.DAMAGE, damageModifier);
            // Damage protection
            DynamicContextAwareLevelValue damageProtectionModifier = new DynamicContextAwareLevelValue((level, lootContext) -> {
                DamageSource nmsDamageSource = lootContext.getParameter(LootContextParams.DAMAGE_SOURCE);
                return (float) script.script.getDamageProtection(level, nmsDamageSource.getMsgId(), nmsDamageSource.getEntity() != null ? nmsDamageSource.getEntity().getBukkitEntity() : null);
            });
            addEnchantmentEffect(nmsEnchantmentEffectsBuilder, EnchantmentEffectComponents.DAMAGE_PROTECTION, damageProtectionModifier);
            // Full name
            String taggedNoLevel = TagManager.tag(script.script.fullNameTaggable.replace(" <context.level>", "").replace("<context.level>", ""), DenizenCore.implementation.getTagContext(script.script));
            Component nmsFullName = Handler.componentToNMS(FormattedTextHelper.parse(taggedNoLevel, ChatColor.GRAY));
            // Compatible enchantments
            HolderSet<Enchantment> nmsIncompatibleEnchants = new DynamicHolderSet<>(nmsEnchantmentRegistry, nmsEnchantment -> !script.script.isCompatible(CraftEnchantment.minecraftToBukkit(nmsEnchantment)));
            // Registration
            Enchantment nmsEnchantment = new Enchantment(nmsFullName, nmsEnchantmentDefinition, nmsIncompatibleEnchants, nmsEnchantmentEffectsBuilder.build());
            ResourceLocation nmsEnchantmentKey = CraftNamespacedKey.toMinecraft(script.script.getKey());
            Holder.Reference<Enchantment> nmsExistingEnchantment = nmsEnchantmentRegistry.get(nmsEnchantmentKey).orElse(null);
            boolean wasFrozen = REGISTRY_FROZEN.getBoolean(nmsEnchantmentRegistry);
            REGISTRY_FROZEN.setBoolean(nmsEnchantmentRegistry, false);
            if (nmsExistingEnchantment == null) {
                Map<?, ?> nmsRegistryHolders = (Map<?, ?>) REGISTRY_INTRUSIVE_HOLDERS.get(nmsEnchantmentRegistry);
                if (nmsRegistryHolders == null) {
                    REGISTRY_INTRUSIVE_HOLDERS.set(nmsEnchantmentRegistry, new IdentityHashMap<>());
                }
                nmsEnchantmentRegistry.createIntrusiveHolder(nmsEnchantment);
                nmsEnchantmentRegistry.register(ResourceKey.create(Registries.ENCHANTMENT, nmsEnchantmentKey), nmsEnchantment, NO_NETWORKING_REGISTRATION_INFO);
                REGISTRY_INTRUSIVE_HOLDERS.set(nmsEnchantmentRegistry, nmsRegistryHolders);
            }
            else {
                replaceRegistryValue(nmsEnchantmentRegistry, nmsEnchantment, nmsExistingEnchantment, nmsEnchantmentKey);
            }
            if (wasFrozen) {
                Object nmsTagSet = REGISTRY_ALL_TAGS.get(nmsEnchantmentRegistry);
                REGISTRY_ALL_TAGS.set(nmsEnchantmentRegistry, MAPPED_REGISTRY_TAG_SET_UNBOUND.invoke());
                nmsEnchantmentRegistry.freeze();
                REGISTRY_ALL_TAGS.set(nmsEnchantmentRegistry, nmsTagSet);
            }
            return CraftEnchantment.minecraftToBukkit(nmsEnchantment);
        }
        catch (Throwable e) {
            Debug.echoError(e);
            return null;
        }
    }

    private static void addEnchantmentEffect(DataComponentMap.Builder nmsEffects, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> nmsEffectType, DynamicContextAwareLevelValue value) {
        nmsEffects.set(nmsEffectType, List.of(new ConditionalEffect<>(new AddValue(value), Optional.of(value))));
    }

    public static final Field MAPPED_REGISTRY_BY_VALUE = ReflectionHelper.getFields(MappedRegistry.class).get("byValue");
    public static final Field MAPPED_REGISTRY_TO_ID = ReflectionHelper.getFields(MappedRegistry.class).get("toId");
    public static final Field MAPPED_REGISTRY_REGISTRATION_INFOS = ReflectionHelper.getFields(MappedRegistry.class).get("registrationInfos");
    public static final Field HOLDER_REFERENCE_VALUE = ReflectionHelper.getFields(Holder.Reference.class).get("value");

    public void replaceRegistryValue(MappedRegistry<Enchantment> nmsRegistry, Enchantment nmsNewValue, Holder.Reference<Enchantment> nmsExistingHolder, ResourceLocation nmsKey) throws IllegalAccessException {
        Enchantment nmsOldValue = nmsExistingHolder.value();
        HOLDER_REFERENCE_VALUE.set(nmsExistingHolder, nmsNewValue);
        Map<Enchantment, Holder.Reference<Enchantment>> byValue = (Map<Enchantment, Holder.Reference<Enchantment>>) MAPPED_REGISTRY_BY_VALUE.get(nmsRegistry);
        byValue.remove(nmsOldValue);
        byValue.put(nmsNewValue, nmsExistingHolder);
        Reference2IntMap<Enchantment> toId = (Reference2IntMap<Enchantment>) MAPPED_REGISTRY_TO_ID.get(nmsRegistry);
        toId.put(nmsNewValue, toId.removeInt(nmsOldValue));
        ResourceKey<Enchantment> nmsResourceKey = ResourceKey.create(Registries.ENCHANTMENT, nmsKey);
        ((Map<ResourceKey<Enchantment>, RegistrationInfo>) MAPPED_REGISTRY_REGISTRATION_INFOS.get(nmsRegistry)).put(nmsResourceKey, NO_NETWORKING_REGISTRATION_INFO);
    }

    @Override
    public String getRarity(org.bukkit.enchantments.Enchantment enchantment) {
        return Rarity.fromWeight(CraftEnchantment.bukkitToMinecraft(enchantment).getWeight()).name();
    }

    @Override
    public boolean isDiscoverable(org.bukkit.enchantments.Enchantment enchantment) {
        return CraftEnchantment.bukkitToMinecraftHolder(enchantment).is(EnchantmentTags.IN_ENCHANTING_TABLE);
    }

    @Override
    public boolean isTradable(org.bukkit.enchantments.Enchantment enchantment) {
        return CraftEnchantment.bukkitToMinecraftHolder(enchantment).is(EnchantmentTags.TRADEABLE);
    }

    @Override
    public boolean isCurse(org.bukkit.enchantments.Enchantment enchantment) {
        return CraftEnchantment.bukkitToMinecraftHolder(enchantment).is(EnchantmentTags.CURSE);
    }

    @Override
    public int getMinCost(org.bukkit.enchantments.Enchantment enchantment, int level) {
        return CraftEnchantment.bukkitToMinecraft(enchantment).getMinCost(level);
    }

    @Override
    public int getMaxCost(org.bukkit.enchantments.Enchantment enchantment, int level) {
        return CraftEnchantment.bukkitToMinecraft(enchantment).getMaxCost(level);
    }

    @Override
    public String getFullName(org.bukkit.enchantments.Enchantment enchantment, int level) {
        return FormattedTextHelper.stringify(Handler.componentToSpigot(Enchantment.getFullname(CraftEnchantment.bukkitToMinecraftHolder(enchantment), level)));
    }

    @Override
    public float getDamageBonus(org.bukkit.enchantments.Enchantment enchantment, int level, String type) {
        TagKey<EntityType<?>> nmsEntityTag = MobType.valueOf(CoreUtilities.toUpperCase(type)).getNmsTagKey();
        EntityType<?> nmsEntityType = Optional.ofNullable(nmsEntityTag)
                .flatMap(BuiltInRegistries.ENTITY_TYPE::get)
                .map(holders -> holders.get(0).value())
                .orElse((EntityType) EntityType.PIG);
        ServerLevel nmsWorld = MinecraftServer.getServer().overworld();
        MutableFloat damage = new MutableFloat(2);
        CraftEnchantment.bukkitToMinecraft(enchantment).modifyDamage(nmsWorld, level, new ItemStack(Items.AIR),
                mockEntity(nmsEntityType, nmsWorld), nmsWorld.damageSources().generic(), damage);
        return Math.max(damage.floatValue() - 2, 0);
    }

    @Override
    public int getDamageProtection(org.bukkit.enchantments.Enchantment enchantment, int level, EntityDamageEvent.DamageCause type, org.bukkit.entity.Entity attacker) {
        Entity nmsAttacker = attacker == null ? null : ((CraftEntity) attacker).getHandle();
        DamageSource nmsDamageSource = EntityHelperImpl.getSourceFor(nmsAttacker, type, nmsAttacker);
        if (nmsDamageSource instanceof EntityHelperImpl.FakeDamageSrc fakeDamageSrc) {
            nmsDamageSource = fakeDamageSrc.real;
        }
        ServerLevel nmsWorld = MinecraftServer.getServer().overworld();
        MutableFloat damageProtection = new MutableFloat(0);
        CraftEnchantment.bukkitToMinecraft(enchantment).modifyDamageProtection(nmsWorld, level, new ItemStack(Items.AIR),
                nmsAttacker != null ? nmsAttacker : mockEntity(EntityType.ZOMBIE, nmsWorld), nmsDamageSource, damageProtection);
        return Math.max(Math.round(damageProtection.floatValue()), 0);
    }

    private static <T extends Entity> T mockEntity(EntityType<T> nmsEntityType, ServerLevel nmsWorld) {
        return nmsEntityType.create(nmsWorld, EntitySpawnReason.COMMAND);
    }

    //////////////////
    /// Networking ///
    //////////////////

    public static final Class<?> CHANNEL_INITIALIZE_LISTENER_CLASS = ReflectionHelper.getClassOrThrow("io.papermc.paper.network.ChannelInitializeListener");
    public static final MethodHandle ADD_CHANNEL_INITIALIZE_LISTENER = ReflectionHelper.getMethodHandle(
            ReflectionHelper.getClassOrThrow("io.papermc.paper.network.ChannelInitializeListenerHolder"),
            "addListener",
            ReflectionHelper.getClassOrThrow("net.kyori.adventure.key.Key"), CHANNEL_INITIALIZE_LISTENER_CLASS
    );

    public static boolean initialized = false;

    public static void verifyNetworkingInitialized() {
        if (initialized) {
            return;
        }
        Object handler = ReflectionHelper.getStaticLambda(CHANNEL_INITIALIZE_LISTENER_CLASS, "afterInitChannel", EnchantmentExcludingChannelHandler.class, "injectSelf");
        try {
            ADD_CHANNEL_INITIALIZE_LISTENER.invoke(null, handler);
        }
        catch (Throwable e) {
            Debug.echoError(e);
        }
        initialized = true;
    }

    public static class EnchantmentExcludingChannelHandler extends ChannelOutboundHandlerAdapter {

        public static void injectSelf(Channel channel) {
            channel.pipeline().addBefore("packet_handler", "denizen_enchant_excluder", new EnchantmentExcludingChannelHandler());
        }

        // NOTE: This runs on Netty IO threads (i.e. async)
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof ClientboundRegistryDataPacket registryDataPacket && registryDataPacket.registry() == Registries.ENCHANTMENT) {
                List<RegistrySynchronization.PackedRegistryEntry> registryEntries = new ArrayList<>(registryDataPacket.entries().size());
                boolean anyRemoved = false;
                for (RegistrySynchronization.PackedRegistryEntry registryEntry : registryDataPacket.entries()) {
                    if (registryEntry.id().getNamespace().equals("denizen") && EnchantmentScriptContainer.registeredEnchantmentContainers.containsKey(registryEntry.id().getPath())) {
                        anyRemoved = true;
                        continue;
                    }
                    registryEntries.add(registryEntry);
                }
                if (anyRemoved) {
                    msg = new ClientboundRegistryDataPacket(registryDataPacket.registry(), registryEntries);
                }
            }
            super.write(ctx, msg, promise);
        }
    }
}

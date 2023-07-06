package com.denizenscript.denizen.nms.v1_20.helpers;

import com.denizenscript.denizen.nms.interfaces.FishingHelper;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class FishingHelperImpl implements FishingHelper {

    @Override
    public org.bukkit.inventory.ItemStack getResult(FishHook fishHook, CatchType catchType) {
        FishingHook nmsHook = ((CraftFishHook) fishHook).getHandle();
        ItemStack result = switch (catchType) {
            case DEFAULT -> {
                float f = ((CraftWorld) fishHook.getWorld()).getHandle().random.nextFloat();
                int i = EnchantmentHelper.getMobLooting(nmsHook.getPlayerOwner());
                int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.FISHING_LUCK, nmsHook.getPlayerOwner());
                float f1 = 0.1F - (float) i * 0.025F - (float) j * 0.01F;
                float f2 = 0.05F + (float) i * 0.01F - (float) j * 0.01F;

                f1 = Mth.clamp(f1, 0.0F, 1.0F);
                f2 = Mth.clamp(f2, 0.0F, 1.0F);
                if (f < f1) {
                    yield catchRandomJunk(nmsHook);
                }
                else {
                    f -= f1;
                    if (f < f2) {
                        yield catchRandomTreasure(nmsHook);
                    }
                    else {
                        yield catchRandomFish(nmsHook);
                    }
                }
            }
            case JUNK -> catchRandomJunk(nmsHook);
            case TREASURE -> catchRandomTreasure(nmsHook);
            case FISH -> catchRandomFish(nmsHook);
            default -> null;
        };
        return result != null ? CraftItemStack.asBukkitCopy(result) : null;
    }

    public ItemStack getRandomReward(FishingHook hook, ResourceLocation key) {
        ServerLevel worldServer = (ServerLevel) hook.level();
        Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
        params.put(LootContextParams.ORIGIN, new Vec3(hook.getX(), hook.getY(), hook.getZ()));
        params.put(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD));
        LootParams playerFishEvent2 = new LootParams(worldServer, params, Maps.newHashMap(), 0);
        LootDataManager registry = worldServer.getServer().getLootData();
        List<ItemStack> itemStacks = registry.getLootTable(key).getRandomItems(playerFishEvent2);
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }

    @Override
    public FishHook spawnHook(Location location, Player player) {
        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        FishingHook hook = new FishingHook(((CraftPlayer) player).getHandle(), nmsWorld, 0, 0);
        nmsWorld.addFreshEntity(hook, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (FishHook) hook.getBukkitEntity();
    }

    private ItemStack catchRandomJunk(FishingHook fishHook) {
        return getRandomReward(fishHook, BuiltInLootTables.FISHING_JUNK);
    }

    private ItemStack catchRandomTreasure(FishingHook fishHook) {
        return getRandomReward(fishHook, BuiltInLootTables.FISHING_TREASURE);
    }

    private ItemStack catchRandomFish(FishingHook fishHook) {
        return getRandomReward(fishHook, BuiltInLootTables.FISHING_FISH);
    }

    public static final Field FISHING_HOOK_NIBBLE_SETTER = ReflectionHelper.getFields(FishingHook.class).get(ReflectionMappingsInfo.FishingHook_nibble, int.class);
    public static final Field FISHING_HOOK_LURE_TIME_SETTER = ReflectionHelper.getFields(FishingHook.class).get(ReflectionMappingsInfo.FishingHook_timeUntilLured, int.class);
    public static final Field FISHING_HOOK_HOOK_TIME_SETTER = ReflectionHelper.getFields(FishingHook.class).get(ReflectionMappingsInfo.FishingHook_timeUntilHooked, int.class);

    @Override
    public FishHook getHookFrom(Player player) {
        FishingHook hook = ((CraftPlayer) player).getHandle().fishing;
        if (hook == null) {
            return null;
        }
        return (FishHook) hook.getBukkitEntity();
    }

    @Override
    public void setNibble(FishHook hook, int ticks) {
        FishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
        try {
            FISHING_HOOK_NIBBLE_SETTER.setInt(nmsEntity, ticks);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void setHookTime(FishHook hook, int ticks) {
        FishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
        try {
            FISHING_HOOK_HOOK_TIME_SETTER.setInt(nmsEntity, ticks);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public int getLureTime(FishHook hook) {
        FishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
        try {
            return FISHING_HOOK_LURE_TIME_SETTER.getInt(nmsEntity);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return -1;
    }

    @Override
    public void setLureTime(FishHook hook, int ticks) {
        FishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
        try {
            FISHING_HOOK_LURE_TIME_SETTER.setInt(nmsEntity, ticks);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}

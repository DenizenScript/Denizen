package com.denizenscript.denizen.nms.v1_19.helpers;

import com.denizenscript.denizen.nms.interfaces.FishingHelper;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.List;

public class FishingHelperImpl implements FishingHelper {

    @Override
    public org.bukkit.inventory.ItemStack getResult(FishHook fishHook, CatchType catchType) {
        ItemStack result = null;
        FishingHook nmsHook = ((CraftFishHook) fishHook).getHandle();
        if (catchType == CatchType.DEFAULT) {
            float f = ((CraftWorld) fishHook.getWorld()).getHandle().random.nextFloat();
            int i = EnchantmentHelper.getMobLooting(nmsHook.getPlayerOwner());
            int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.FISHING_LUCK, nmsHook.getPlayerOwner());
            float f1 = 0.1F - (float) i * 0.025F - (float) j * 0.01F;
            float f2 = 0.05F + (float) i * 0.01F - (float) j * 0.01F;

            f1 = Mth.clamp(f1, 0.0F, 1.0F);
            f2 = Mth.clamp(f2, 0.0F, 1.0F);
            if (f < f1) {
                result = catchRandomJunk(nmsHook);
            }
            else {
                f -= f1;
                if (f < f2) {
                    result = catchRandomTreasure(nmsHook);
                }
                else {
                    result = catchRandomFish(nmsHook);
                }
            }
        }
        else if (catchType == CatchType.JUNK) {
            result = catchRandomJunk(nmsHook);
        }
        else if (catchType == CatchType.TREASURE) {
            result = catchRandomTreasure(nmsHook);
        }
        else if (catchType == CatchType.FISH) {
            result = catchRandomFish(nmsHook);
        }
        if (result != null) {
            return CraftItemStack.asBukkitCopy(result);
        }
        else {
            return null;
        }
    }

    public ItemStack getRandomReward(FishingHook hook, ResourceLocation key) {
        ServerLevel worldServer = (ServerLevel) hook.level;
        LootContext.Builder playerFishEvent2 = new LootContext.Builder(worldServer);
        LootTables registry = ((ServerLevel) hook.level).getServer().getLootTables();
        // registry.getLootTable(key).getLootContextParameterSet()
        LootContext info = playerFishEvent2.withOptionalParameter(LootContextParams.ORIGIN, new Vec3(hook.getX(), hook.getY(), hook.getZ()))
                .withOptionalParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD)).create(LootContextParamSets.FISHING);
        List<ItemStack> itemStacks = registry.get(key).getRandomItems(info);
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

    public static Field FISHING_HOOK_NIBBLE_SETTER = ReflectionHelper.getFields(FishingHook.class).get(ReflectionMappingsInfo.FishingHook_nibble, int.class);
    public static Field FISHING_HOOK_LURE_TIME_SETTER = ReflectionHelper.getFields(FishingHook.class).get(ReflectionMappingsInfo.FishingHook_timeUntilLured, int.class);
    public static Field FISHING_HOOK_HOOK_TIME_SETTER = ReflectionHelper.getFields(FishingHook.class).get(ReflectionMappingsInfo.FishingHook_timeUntilHooked, int.class);

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

package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.interfaces.FishingHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

public class FishingHelperImpl implements FishingHelper {

    @Override
    public org.bukkit.inventory.ItemStack getResult(FishHook fishHook, CatchType catchType) {
        ItemStack result = null;
        EntityFishingHook nmsHook = ((CraftFishHook) fishHook).getHandle();
        if (catchType == CatchType.DEFAULT) {
            float f = ((CraftWorld) fishHook.getWorld()).getHandle().random.nextFloat();
            int i = EnchantmentManager.g((EntityHuman) nmsHook.getShooter());
            int j = EnchantmentManager.a(Enchantments.LURE, (EntityHuman) nmsHook.getShooter());
            float f1 = 0.1F - (float) i * 0.025F - (float) j * 0.01F;
            float f2 = 0.05F + (float) i * 0.01F - (float) j * 0.01F;

            f1 = MathHelper.a(f1, 0.0F, 1.0F);
            f2 = MathHelper.a(f2, 0.0F, 1.0F);
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

    public ItemStack getRandomReward(EntityFishingHook hook, MinecraftKey key) {
        WorldServer worldServer = (WorldServer) hook.getWorld();
        LootTableInfo.Builder playerFishEvent2 = new LootTableInfo.Builder(worldServer);
        LootTableRegistry registry = hook.getWorld().getMinecraftServer().getLootTableRegistry();
        // registry.getLootTable(key).getLootContextParameterSet()
        LootTableInfo info = playerFishEvent2.set(LootContextParameters.ORIGIN, new Vec3D(hook.locX(), hook.locY(), hook.locZ()))
                .set(LootContextParameters.TOOL, new ItemStack(Items.FISHING_ROD)).build(LootContextParameterSets.FISHING);
        List<ItemStack> itemStacks = registry.getLootTable(key).populateLoot(info);
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }

    @Override
    public FishHook spawnHook(Location location, Player player) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityFishingHook hook = new EntityFishingHook(((CraftPlayer) player).getHandle(), nmsWorld, 0, 0);
        nmsWorld.addEntity(hook);
        return (FishHook) hook.getBukkitEntity();
    }

    private ItemStack catchRandomJunk(EntityFishingHook fishHook) {
        return getRandomReward(fishHook, LootTables.ah);
    }

    private ItemStack catchRandomTreasure(EntityFishingHook fishHook) {
        return getRandomReward(fishHook, LootTables.ai);
    }

    private ItemStack catchRandomFish(EntityFishingHook fishHook) {
        return getRandomReward(fishHook, LootTables.aj);
    }

    public static Field FISHING_HOOK_NIBBLE_SETTER = ReflectionHelper.getFields(EntityFishingHook.class).get("ag");
    public static Field FISHING_HOOK_LURE_TIME_SETTER = ReflectionHelper.getFields(EntityFishingHook.class).get("waitTime");
    public static Field FISHING_HOOK_HOOK_TIME_SETTER = ReflectionHelper.getFields(EntityFishingHook.class).get("ai");

    @Override
    public FishHook getHookFrom(Player player) {
        EntityFishingHook hook = ((CraftPlayer) player).getHandle().hookedFish;
        if (hook == null) {
            return null;
        }
        return (FishHook) hook.getBukkitEntity();
    }

    @Override
    public void setNibble(FishHook hook, int ticks) {
        EntityFishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
        try {
            FISHING_HOOK_NIBBLE_SETTER.setInt(nmsEntity, ticks);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void setHookTime(FishHook hook, int ticks) {
        EntityFishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
        try {
            FISHING_HOOK_HOOK_TIME_SETTER.setInt(nmsEntity, ticks);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public int getLureTime(FishHook hook) {
        EntityFishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
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
        EntityFishingHook nmsEntity = ((CraftFishHook) hook).getHandle();
        try {
            FISHING_HOOK_LURE_TIME_SETTER.setInt(nmsEntity, ticks);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}

package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.FishingHelper;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftFish;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.FishHook;

import java.util.List;

public class FishingHelper_v1_9_R2 implements FishingHelper {

    @Override
    public org.bukkit.inventory.ItemStack getResult(FishHook fishHook, CatchType catchType) {
        ItemStack result = null;
        EntityFishingHook nmsHook = ((CraftFish) fishHook).getHandle();
        if (catchType == CatchType.DEFAULT) {
            float f = ((CraftWorld) fishHook.getWorld()).getHandle().random.nextFloat();
            int i = EnchantmentManager.g(nmsHook.owner);
            int j = EnchantmentManager.h(nmsHook.owner);
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

    private ItemStack catchRandomJunk(EntityFishingHook fishHook) {
        fishHook.owner.a(StatisticList.A, 1);
        WorldServer worldServer = (WorldServer) fishHook.getWorld();
        LootTableInfo.a playerFishEvent2 = new LootTableInfo.a(worldServer);
        playerFishEvent2.a((float) EnchantmentManager.f(fishHook.owner) + fishHook.owner.dc());
        List<ItemStack> itemStacks = fishHook.getWorld().ak().a(LootTables.am).a(worldServer.random, playerFishEvent2.a());
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }

    private ItemStack catchRandomTreasure(EntityFishingHook fishHook) {
        fishHook.owner.a(StatisticList.B, 1);
        WorldServer worldServer = (WorldServer) fishHook.getWorld();
        LootTableInfo.a playerFishEvent2 = new LootTableInfo.a((WorldServer)fishHook.getWorld());
        playerFishEvent2.a((float)EnchantmentManager.f(fishHook.owner) + fishHook.owner.dc());
        List<ItemStack> itemStacks = fishHook.getWorld().ak().a(LootTables.an).a(worldServer.random, playerFishEvent2.a());
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }

    private ItemStack catchRandomFish(EntityFishingHook fishHook) {
        //float f3 = f - f2;
        fishHook.owner.a(StatisticList.z, 1);
        WorldServer worldServer = (WorldServer) fishHook.getWorld();
        LootTableInfo.a playerFishEvent2 = new LootTableInfo.a((WorldServer)fishHook.getWorld());
        playerFishEvent2.a((float)EnchantmentManager.f(fishHook.owner) + fishHook.owner.dc());
        List<ItemStack> itemStacks = fishHook.getWorld().ak().a(LootTables.ao).a(worldServer.random, playerFishEvent2.a());
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }
}

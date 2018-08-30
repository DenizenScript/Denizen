package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.FishingHelper;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.List;

public class FishingHelper_v1_13_R2 implements FishingHelper {

    @Override
    public org.bukkit.inventory.ItemStack getResult(FishHook fishHook, CatchType catchType) {
        ItemStack result = null;
        EntityFishingHook nmsHook = ((CraftFishHook) fishHook).getHandle();
        if (catchType == CatchType.DEFAULT) {
            float f = ((CraftWorld) fishHook.getWorld()).getHandle().random.nextFloat();
            int i = EnchantmentManager.g(nmsHook.owner);
            int j = EnchantmentManager.a(Enchantments.LURE, nmsHook.owner);
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

    @Override
    public FishHook spawnHook(Location location, Player player) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        EntityFishingHook hook = new EntityFishingHook(nmsWorld, ((CraftPlayer) player).getHandle());
        nmsWorld.addEntity(hook);
        return (FishHook) hook.getBukkitEntity();
    }

    private ItemStack catchRandomJunk(EntityFishingHook fishHook) {
        //fishHook.owner.a(StatisticList.F, 1); -- removed stat as of 1.12
        WorldServer worldServer = (WorldServer) fishHook.getWorld();
        LootTableInfo.Builder playerFishEvent2 = new LootTableInfo.Builder(worldServer);
        playerFishEvent2.luck((float) EnchantmentManager.a(Enchantments.LUCK, fishHook.owner) + fishHook.owner.dJ());
        List<ItemStack> itemStacks = fishHook.getWorld().getMinecraftServer().getLootTableRegistry().getLootTable(LootTables.aP).a(worldServer.random, playerFishEvent2.build());
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }

    private ItemStack catchRandomTreasure(EntityFishingHook fishHook) {
        //fishHook.owner.a(StatisticList.G, 1); -- removed stat as of 1.12
        WorldServer worldServer = (WorldServer) fishHook.getWorld();
        LootTableInfo.Builder playerFishEvent2 = new LootTableInfo.Builder((WorldServer) fishHook.getWorld());
        playerFishEvent2.luck((float) EnchantmentManager.a(Enchantments.LUCK, fishHook.owner) + fishHook.owner.dJ());
        List<ItemStack> itemStacks = fishHook.getWorld().getMinecraftServer().getLootTableRegistry().getLootTable(LootTables.aQ).a(worldServer.random, playerFishEvent2.build());
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }

    private ItemStack catchRandomFish(EntityFishingHook fishHook) {
        //float f3 = f - f2;
        fishHook.owner.a(StatisticList.FISH_CAUGHT, 1);
        WorldServer worldServer = (WorldServer) fishHook.getWorld();
        LootTableInfo.Builder playerFishEvent2 = new LootTableInfo.Builder((WorldServer) fishHook.getWorld());
        playerFishEvent2.luck((float) EnchantmentManager.a(Enchantments.LUCK, fishHook.owner) + fishHook.owner.dJ());
        List<ItemStack> itemStacks = fishHook.getWorld().getMinecraftServer().getLootTableRegistry().getLootTable(LootTables.aR).a(worldServer.random, playerFishEvent2.build());
        return itemStacks.get(worldServer.random.nextInt(itemStacks.size()));
    }
}

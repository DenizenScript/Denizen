package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.FishingHelper;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFish;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.FishHook;

import java.util.Arrays;
import java.util.List;

public class FishingHelper_v1_8_R3 implements FishingHelper {

    private static final List junkResults = Arrays.asList(
            new PossibleFishingResult(new ItemStack(Items.LEATHER_BOOTS), 10).a(0.9F),
            new PossibleFishingResult(new ItemStack(Items.LEATHER), 10),
            new PossibleFishingResult(new ItemStack(Items.BONE), 10),
            new PossibleFishingResult(new ItemStack(Items.POTION), 10),
            new PossibleFishingResult(new ItemStack(Items.STRING), 5),
            (new PossibleFishingResult(new ItemStack(Items.FISHING_ROD), 2)).a(0.9F),
            new PossibleFishingResult(new ItemStack(Items.BOWL), 10),
            new PossibleFishingResult(new ItemStack(Items.STICK), 5),
            new PossibleFishingResult(new ItemStack(Items.DYE, 10, 0), 1),
            new PossibleFishingResult(new ItemStack(Blocks.TRIPWIRE_HOOK), 10),
            new PossibleFishingResult(new ItemStack(Items.ROTTEN_FLESH), 10));
    private static final List treasureResults = Arrays.asList(
            new PossibleFishingResult(new ItemStack(Blocks.WATERLILY), 1),
            new PossibleFishingResult(new ItemStack(Items.NAME_TAG), 1),
            new PossibleFishingResult(new ItemStack(Items.SADDLE), 1),
            (new PossibleFishingResult(new ItemStack(Items.BOW), 1)).a(0.25F).a(),
            (new PossibleFishingResult(new ItemStack(Items.FISHING_ROD), 1)).a(0.25F).a(),
            (new PossibleFishingResult(new ItemStack(Items.BOOK), 1)).a());
    private static final List fishResults = Arrays.asList(
            new PossibleFishingResult(new ItemStack(Items.FISH, 1, ItemFish.EnumFish.COD.a()), 60),
            new PossibleFishingResult(new ItemStack(Items.FISH, 1, ItemFish.EnumFish.SALMON.a()), 25),
            new PossibleFishingResult(new ItemStack(Items.FISH, 1, ItemFish.EnumFish.CLOWNFISH.a()), 2),
            new PossibleFishingResult(new ItemStack(Items.FISH, 1, ItemFish.EnumFish.PUFFERFISH.a()), 13));

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
        return ((PossibleFishingResult) WeightedRandom.a(fishHook.world.random, junkResults)).a(fishHook.world.random);
    }

    private ItemStack catchRandomTreasure(EntityFishingHook fishHook) {
        fishHook.owner.a(StatisticList.B, 1);
        return ((PossibleFishingResult) WeightedRandom.a(fishHook.world.random, treasureResults)).a(fishHook.world.random);
    }

    private ItemStack catchRandomFish(EntityFishingHook fishHook) {
        //float f3 = f - f2;
        fishHook.owner.a(StatisticList.z, 1);
        return ((PossibleFishingResult) WeightedRandom.a(fishHook.world.random, fishResults)).a(fishHook.world.random);
    }
}

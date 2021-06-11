package com.denizenscript.denizen.nms.v1_16.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutTradeList;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.TradeOffer;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_16_R3.MerchantRecipe;
import net.minecraft.server.v1_16_R3.MerchantRecipeList;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenWindowMerchant;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketOutTradeListImpl implements PacketOutTradeList {

    private PacketPlayOutOpenWindowMerchant internal;
    private int container;
    private List<TradeOffer> tradeOffers;

    public PacketOutTradeListImpl(PacketPlayOutOpenWindowMerchant internal) {
        this.internal = internal;
        try {
            container = (int) CONTAINER.get(internal);
            MerchantRecipeList list = (MerchantRecipeList) RECIPE_LIST.get(internal);
            tradeOffers = new ArrayList<>();
            for (MerchantRecipe recipe : list) {
                tradeOffers.add(new TradeOffer(CraftItemStack.asBukkitCopy(recipe.sellingItem),
                        CraftItemStack.asBukkitCopy(recipe.buyingItem1),
                        CraftItemStack.asBukkitCopy(recipe.buyingItem2),
                        recipe.isFullyUsed(), recipe.uses, recipe.maxUses,
                        recipe.rewardExp, recipe.xp, recipe.priceMultiplier));
            }
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
    }

    @Override
    public List<TradeOffer> getTradeOffers() {
        return tradeOffers;
    }

    @Override
    public void setTradeOffers(List<TradeOffer> tradeOffers) {
        MerchantRecipeList list = new MerchantRecipeList();
        for (TradeOffer offer : tradeOffers) {
            MerchantRecipe recipe = new MerchantRecipe(CraftItemStack.asNMSCopy(offer.getFirstCost()),
                    CraftItemStack.asNMSCopy(offer.getSecondCost()),
                    CraftItemStack.asNMSCopy(offer.getProduct()),
                    offer.getCurrentUses(), offer.getMaxUses(), offer.xp, offer.priceMultiplier);
            recipe.rewardExp = offer.rewardExp;
            list.add(recipe);
        }
        try {
            RECIPE_LIST.set(internal, list);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
    }

    private static final Map<String, Field> FIELDS = ReflectionHelper.getFields(PacketPlayOutOpenWindowMerchant.class);
    private static final Field CONTAINER = FIELDS.get("a");
    private static final Field RECIPE_LIST = FIELDS.get("b");
}

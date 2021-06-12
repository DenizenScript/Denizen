package com.denizenscript.denizen.nms.v1_17.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutTradeList;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.TradeOffer;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketOutTradeListImpl implements PacketOutTradeList {

    private ClientboundMerchantOffersPacket internal;
    private int container;
    private List<TradeOffer> tradeOffers;

    public PacketOutTradeListImpl(ClientboundMerchantOffersPacket internal) {
        this.internal = internal;
        try {
            container = (int) CONTAINER.get(internal);
            MerchantOffers list = (MerchantOffers) RECIPE_LIST.get(internal);
            tradeOffers = new ArrayList<>();
            for (MerchantOffer recipe : list) {
                tradeOffers.add(new TradeOffer(CraftItemStack.asBukkitCopy(recipe.result),
                        CraftItemStack.asBukkitCopy(recipe.baseCostA),
                        CraftItemStack.asBukkitCopy(recipe.costB),
                        recipe.isOutOfStock(), recipe.uses, recipe.maxUses,
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
        MerchantOffers list = new MerchantOffers();
        for (TradeOffer offer : tradeOffers) {
            MerchantOffer recipe = new MerchantOffer(CraftItemStack.asNMSCopy(offer.getFirstCost()),
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

    private static final Map<String, Field> FIELDS = ReflectionHelper.getFields(ClientboundMerchantOffersPacket.class);
    private static final Field CONTAINER = FIELDS.get("a");
    private static final Field RECIPE_LIST = FIELDS.get("b");
}

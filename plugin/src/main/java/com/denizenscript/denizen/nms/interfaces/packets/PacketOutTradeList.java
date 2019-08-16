package com.denizenscript.denizen.nms.interfaces.packets;

import com.denizenscript.denizen.nms.util.TradeOffer;

import java.util.List;

public interface PacketOutTradeList {

    List<TradeOffer> getTradeOffers();

    void setTradeOffers(List<TradeOffer> tradeOffers);
}

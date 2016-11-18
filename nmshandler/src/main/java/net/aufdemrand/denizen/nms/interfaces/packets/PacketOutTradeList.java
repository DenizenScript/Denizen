package net.aufdemrand.denizen.nms.interfaces.packets;

import net.aufdemrand.denizen.nms.util.TradeOffer;

import java.util.List;

public interface PacketOutTradeList {

    List<TradeOffer> getTradeOffers();

    void setTradeOffers(List<TradeOffer> tradeOffers);
}

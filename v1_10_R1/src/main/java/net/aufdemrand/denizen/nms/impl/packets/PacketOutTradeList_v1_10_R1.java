package net.aufdemrand.denizen.nms.impl.packets;

import io.netty.buffer.Unpooled;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutTradeList;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizen.nms.util.TradeOffer;
import net.minecraft.server.v1_10_R1.PacketDataSerializer;
import net.minecraft.server.v1_10_R1.PacketPlayOutCustomPayload;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketOutTradeList_v1_10_R1 implements PacketOutTradeList {

    private PacketPlayOutCustomPayload internal;
    private int container;
    private List<TradeOffer> tradeOffers;

    public PacketOutTradeList_v1_10_R1(PacketPlayOutCustomPayload internal) {
        this.internal = internal;
        try {
            PacketDataSerializer serializer = (PacketDataSerializer) SERIALIZER.get(internal);
            container = serializer.readInt();
            tradeOffers = new ArrayList<TradeOffer>();
            byte tradeCount = serializer.readByte();
            for (byte i = 0; i < tradeCount; i++) {
                ItemStack firstCost = CraftItemStack.asBukkitCopy(serializer.k());
                ItemStack product = CraftItemStack.asBukkitCopy(serializer.k());
                boolean hasSecondCost = serializer.readBoolean();
                ItemStack secondCost = hasSecondCost ? CraftItemStack.asBukkitCopy(serializer.k()) : null;
                boolean usedMaxTimes = serializer.readBoolean();
                int currentUses = serializer.readInt();
                int maxUses = serializer.readInt();
                tradeOffers.add(new TradeOffer(product, firstCost, secondCost, usedMaxTimes, currentUses, maxUses));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<TradeOffer> getTradeOffers() {
        return tradeOffers;
    }

    @Override
    public void setTradeOffers(List<TradeOffer> tradeOffers) {
        try {
            PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
            serializer.writeInt(container);
            serializer.writeByte(tradeOffers.size());
            for (TradeOffer tradeOffer : tradeOffers) {
                serializer.a(CraftItemStack.asNMSCopy(tradeOffer.getFirstCost()));
                serializer.a(CraftItemStack.asNMSCopy(tradeOffer.getProduct()));
                boolean hasSecondCost = tradeOffer.hasSecondCost();
                serializer.writeBoolean(hasSecondCost);
                if (hasSecondCost) {
                    serializer.a(CraftItemStack.asNMSCopy(tradeOffer.getSecondCost()));
                }
                serializer.writeBoolean(tradeOffer.isUsedMaxTimes());
                serializer.writeInt(tradeOffer.getCurrentUses());
                serializer.writeInt(tradeOffer.getMaxUses());
            }
            SERIALIZER.set(internal, serializer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Field SERIALIZER;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutCustomPayload.class);
        SERIALIZER = fields.get("b");
    }
}

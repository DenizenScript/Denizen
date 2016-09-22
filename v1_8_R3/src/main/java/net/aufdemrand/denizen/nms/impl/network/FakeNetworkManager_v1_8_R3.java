package net.aufdemrand.denizen.nms.impl.network;

import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;

import java.lang.reflect.Field;
import java.net.SocketAddress;

public class FakeNetworkManager_v1_8_R3 extends NetworkManager {

    private static final Field networkChannelField;
    private static final Field networkAddressField;

    static {
        Field chan = null;
        Field addr = null;
        try {
            chan = NetworkManager.class.getDeclaredField("channel");
            chan.setAccessible(true);
            addr = NetworkManager.class.getDeclaredField("l");
            addr.setAccessible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        networkChannelField = chan;
        networkAddressField = addr;
    }

    public FakeNetworkManager_v1_8_R3(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        try {
            networkChannelField.set(this, new FakeChannel_v1_8_R3(null));
            networkAddressField.set(this, new SocketAddress() {
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

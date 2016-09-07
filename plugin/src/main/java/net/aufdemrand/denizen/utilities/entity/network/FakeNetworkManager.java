package net.aufdemrand.denizen.utilities.entity.network;

import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_10_R1.EnumProtocolDirection;
import net.minecraft.server.v1_10_R1.NetworkManager;

import java.lang.reflect.Field;
import java.net.SocketAddress;

public class FakeNetworkManager extends NetworkManager {

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
            dB.echoError(e);
        }
        networkChannelField = chan;
        networkAddressField = addr;
    }

    public FakeNetworkManager(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        try {
            networkChannelField.set(this, new FakeChannel(null));
            networkAddressField.set(this, new SocketAddress() {
            });
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }
}

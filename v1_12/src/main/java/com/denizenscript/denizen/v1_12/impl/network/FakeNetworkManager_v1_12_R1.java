package com.denizenscript.denizen.v1_12.impl.network;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.NetworkManager;

import java.lang.reflect.Field;
import java.net.SocketAddress;

public class FakeNetworkManager_v1_12_R1 extends NetworkManager {

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
            Debug.echoError(e);
        }
        networkChannelField = chan;
        networkAddressField = addr;
    }

    public FakeNetworkManager_v1_12_R1(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        try {
            networkChannelField.set(this, new FakeChannel_v1_12_R1(null));
            networkAddressField.set(this, new SocketAddress() {
            });
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }
}

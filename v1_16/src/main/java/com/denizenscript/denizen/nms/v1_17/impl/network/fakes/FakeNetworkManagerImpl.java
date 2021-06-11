package com.denizenscript.denizen.nms.v1_17.impl.network.fakes;

import net.minecraft.server.v1_16_R3.EnumProtocolDirection;
import net.minecraft.server.v1_16_R3.NetworkManager;

import java.net.SocketAddress;

public class FakeNetworkManagerImpl extends NetworkManager {

    public FakeNetworkManagerImpl(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannelImpl(null);
        socketAddress = new SocketAddress() {
        };
    }
}

package com.denizenscript.denizen.nms.v1_16.impl.network.fakes;

import net.minecraft.server.v1_17_R1.EnumProtocolDirection;
import net.minecraft.server.v1_17_R1.NetworkManager;

import java.net.SocketAddress;

public class FakeNetworkManagerImpl extends NetworkManager {

    public FakeNetworkManagerImpl(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannelImpl(null);
        socketAddress = new SocketAddress() {
        };
    }
}

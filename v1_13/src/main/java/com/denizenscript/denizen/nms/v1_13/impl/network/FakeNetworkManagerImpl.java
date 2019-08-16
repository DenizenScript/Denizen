package com.denizenscript.denizen.nms.v1_13.impl.network;

import net.minecraft.server.v1_13_R2.EnumProtocolDirection;
import net.minecraft.server.v1_13_R2.NetworkManager;

import java.net.SocketAddress;

public class FakeNetworkManagerImpl extends NetworkManager {

    public FakeNetworkManagerImpl(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannelImpl(null);
        socketAddress = new SocketAddress() {};
    }
}

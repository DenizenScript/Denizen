package com.denizenscript.denizen.nms.v1_20.impl.network.fakes;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

import java.net.SocketAddress;

public class FakeNetworkManagerImpl extends Connection {

    public FakeNetworkManagerImpl(PacketFlow enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannelImpl(null);
        address = new SocketAddress() {
        };
    }
}

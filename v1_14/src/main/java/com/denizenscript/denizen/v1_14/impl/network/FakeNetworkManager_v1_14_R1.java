package com.denizenscript.denizen.v1_14.impl.network;

import net.minecraft.server.v1_14_R1.EnumProtocolDirection;
import net.minecraft.server.v1_14_R1.NetworkManager;

import java.net.SocketAddress;

public class FakeNetworkManager_v1_14_R1 extends NetworkManager {

    public FakeNetworkManager_v1_14_R1(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannel_v1_14_R1(null);
        socketAddress = new SocketAddress() {
        };
    }
}

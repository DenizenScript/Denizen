package com.denizenscript.denizen.nms.impl.network;

import net.minecraft.server.v1_13_R2.EnumProtocolDirection;
import net.minecraft.server.v1_13_R2.NetworkManager;

import java.net.SocketAddress;

public class FakeNetworkManager_v1_13_R2 extends NetworkManager {

    public FakeNetworkManager_v1_13_R2(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannel_v1_13_R2(null);
        socketAddress = new SocketAddress() {};
    }
}

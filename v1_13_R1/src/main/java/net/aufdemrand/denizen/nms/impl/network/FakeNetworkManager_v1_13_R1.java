package net.aufdemrand.denizen.nms.impl.network;

import net.minecraft.server.v1_13_R1.EnumProtocolDirection;
import net.minecraft.server.v1_13_R1.NetworkManager;

import java.net.SocketAddress;

public class FakeNetworkManager_v1_13_R1 extends NetworkManager {

    public FakeNetworkManager_v1_13_R1(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannel_v1_13_R1(null);
        l = new SocketAddress() {};
    }
}

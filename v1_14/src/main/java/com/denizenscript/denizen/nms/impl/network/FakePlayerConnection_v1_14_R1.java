package com.denizenscript.denizen.nms.impl.network;

import net.minecraft.server.v1_14_R1.*;

public class FakePlayerConnection_v1_14_R1 extends PlayerConnection {

    public FakePlayerConnection_v1_14_R1(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {
        // Do nothing
    }
}

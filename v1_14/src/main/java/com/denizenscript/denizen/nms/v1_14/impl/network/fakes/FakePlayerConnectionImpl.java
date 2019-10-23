package com.denizenscript.denizen.nms.v1_14.impl.network.fakes;

import net.minecraft.server.v1_14_R1.*;

public class FakePlayerConnectionImpl extends PlayerConnection {

    public FakePlayerConnectionImpl(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {
        // Do nothing
    }
}

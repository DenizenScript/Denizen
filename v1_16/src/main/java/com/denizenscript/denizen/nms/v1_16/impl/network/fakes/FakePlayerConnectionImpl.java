package com.denizenscript.denizen.nms.v1_16.impl.network.fakes;

import net.minecraft.server.v1_16_R2.*;

public class FakePlayerConnectionImpl extends PlayerConnection {

    public FakePlayerConnectionImpl(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {
        // Do nothing
    }
}

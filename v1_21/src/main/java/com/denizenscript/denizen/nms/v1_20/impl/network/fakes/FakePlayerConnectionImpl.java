package com.denizenscript.denizen.nms.v1_20.impl.network.fakes;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class FakePlayerConnectionImpl extends ServerGamePacketListenerImpl {

    public FakePlayerConnectionImpl(MinecraftServer minecraftserver, Connection networkmanager, ServerPlayer entityplayer, CommonListenerCookie cookie) {
        super(minecraftserver, networkmanager, entityplayer, cookie);
    }

    @Override
    public void send(Packet packet) {
        // Do nothing
    }
}

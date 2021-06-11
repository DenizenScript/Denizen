package com.denizenscript.denizen.nms.v1_17.impl.network.fakes;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class FakePlayerConnectionImplImpl extends ServerGamePacketListenerImpl {

    public FakePlayerConnectionImplImpl(MinecraftServer minecraftserver, Connection networkmanager, ServerPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void send(Packet packet) {
        // Do nothing
    }
}

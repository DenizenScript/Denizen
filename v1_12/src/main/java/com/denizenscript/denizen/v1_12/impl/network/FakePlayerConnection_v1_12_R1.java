package com.denizenscript.denizen.v1_12.impl.network;

import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PlayerConnection;

public class FakePlayerConnection_v1_12_R1 extends PlayerConnection {

    public FakePlayerConnection_v1_12_R1(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {
        // Do nothing
    }
}

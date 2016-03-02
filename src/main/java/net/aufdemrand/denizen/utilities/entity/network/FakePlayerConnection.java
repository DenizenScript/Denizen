package net.aufdemrand.denizen.utilities.entity.network;

import net.minecraft.server.v1_9_R1.*;

public class FakePlayerConnection extends PlayerConnection {

    public FakePlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {
        // Do nothing
    }
}

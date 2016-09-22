package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketInSteerVehicle;
import net.minecraft.server.v1_8_R3.PacketPlayInSteerVehicle;

public class PacketInSteerVehicle_v1_8_R3 implements PacketInSteerVehicle {

    private PacketPlayInSteerVehicle internal;

    public PacketInSteerVehicle_v1_8_R3(PacketPlayInSteerVehicle internal) {
        this.internal = internal;
    }

    @Override
    public float getLeftwardInput() {
        return internal.a();
    }

    @Override
    public float getForwardInput() {
        return internal.b();
    }

    @Override
    public boolean getJumpInput() {
        return internal.c();
    }

    @Override
    public boolean getDismountInput() {
        return internal.d();
    }
}

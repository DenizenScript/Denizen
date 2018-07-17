package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketInSteerVehicle;
import net.minecraft.server.v1_13_R1.PacketPlayInSteerVehicle;

public class PacketInSteerVehicle_v1_13_R1 implements PacketInSteerVehicle {

    private PacketPlayInSteerVehicle internal;

    public PacketInSteerVehicle_v1_13_R1(PacketPlayInSteerVehicle internal) {
        this.internal = internal;
    }

    @Override
    public float getLeftwardInput() {
        return internal.b();
    }

    @Override
    public float getForwardInput() {
        return internal.c();
    }

    @Override
    public boolean getJumpInput() {
        return internal.d();
    }

    @Override
    public boolean getDismountInput() {
        return internal.e();
    }
}

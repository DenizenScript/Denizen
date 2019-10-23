package com.denizenscript.denizen.nms.v1_14.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInSteerVehicle;
import net.minecraft.server.v1_14_R1.PacketPlayInSteerVehicle;

public class PacketInSteerVehicleImpl implements PacketInSteerVehicle {

    private PacketPlayInSteerVehicle internal;

    public PacketInSteerVehicleImpl(PacketPlayInSteerVehicle internal) {
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

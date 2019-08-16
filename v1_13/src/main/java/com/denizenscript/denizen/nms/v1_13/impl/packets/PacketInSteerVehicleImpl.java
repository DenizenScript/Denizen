package com.denizenscript.denizen.nms.v1_13.impl.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInSteerVehicle;
import net.minecraft.server.v1_13_R2.PacketPlayInSteerVehicle;

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

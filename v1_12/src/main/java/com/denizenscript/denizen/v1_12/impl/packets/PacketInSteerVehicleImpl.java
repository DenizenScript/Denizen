package com.denizenscript.denizen.v1_12.impl.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInSteerVehicle;
import net.minecraft.server.v1_12_R1.PacketPlayInSteerVehicle;

public class PacketInSteerVehicleImpl implements PacketInSteerVehicle {

    private PacketPlayInSteerVehicle internal;

    public PacketInSteerVehicleImpl(PacketPlayInSteerVehicle internal) {
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

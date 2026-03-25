package com.denizenscript.denizen.nms.v1_21.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInSteerVehicle;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;

public class PacketInSteerVehicleImpl implements PacketInSteerVehicle {

    private ServerboundPlayerInputPacket internal;

    public PacketInSteerVehicleImpl(ServerboundPlayerInputPacket internal) {
        this.internal = internal;
    }

    @Override
    public float getLeftwardInput() {
        return internal.input().left() ? 1 : 0;
    }

    @Override
    public float getForwardInput() {
        return internal.input().forward() ? 1 : 0;
    }

    @Override
    public boolean getJumpInput() {
        return internal.input().jump();
    }

    @Override
    public boolean getDismountInput() {
        return internal.input().shift();
    }
}
